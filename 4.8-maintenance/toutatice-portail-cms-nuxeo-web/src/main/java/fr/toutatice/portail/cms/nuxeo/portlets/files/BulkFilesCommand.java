package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;

/**
 * returns zip archive of supplied files paths
 * 
 * @author Dorian Licois
 *
 */
public class BulkFilesCommand implements INuxeoCommand {

    /**
     * fileName deduplication pattern
     */
    private static final Pattern DEDUPED_BASENAME_PATTERN = Pattern.compile("^(.*__)(\\d{1,3})(\\.?.*)$");

    /** FILE_FIELD_NAME */
    private static final String FILE_FIELD_NAME = "file:content";

    /** SIZE_FIELD */
    private static final String SIZE_FIELD = "common:size";

    /** DEFAULT_FILENAME */
    private static final String DEFAULT_FILENAME = "export.zip";

    /** ZIP_MIMETYPE */
    private static final String ZIP_MIMETYPE = "application/zip";

    /** MAX_SIZE */
    private static final long MAX_SIZE = 100000000L; // 100MB

    /** paths */
    private String[] paths;

    /** nuxeoController */
    private NuxeoController nuxeoController;

    public BulkFilesCommand(NuxeoController nuxeoController, String[] paths) {
        this.nuxeoController = nuxeoController;
        this.paths = paths;
    }

    @Override
    public Object execute(Session nuxeoSession) throws Exception {

        // check all docs type file and sum < MAX_SIZE
        NuxeoDocumentContext documentContext;
        long sizeSum = 0;
        for (String path : this.paths) {
            documentContext = nuxeoController.getDocumentContext(path);
            Long fileSize = documentContext.getDocument().getLong(SIZE_FIELD);
            if (fileSize == null) {
                throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);
            }
            sizeSum += fileSize;
            if (sizeSum > MAX_SIZE) {
                throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
            }
        }

        nuxeoController.setStreamingSupport(true);
        // try to retrieve all files from cache or fetch
        List<CMSBinaryContent> contents = new ArrayList<CMSBinaryContent>(this.paths.length);
        for (String path : this.paths) {
            CMSBinaryContent content = nuxeoController.fetchFileContent(path, FILE_FIELD_NAME);
            contents.add(content);
        }

        // build zip of every files
        File tempFile = File.createTempFile("tempFile", ".tmp");
        tempFile.deleteOnExit();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempFile));
        zout.setMethod(ZipOutputStream.STORED);
        zout.setLevel(Deflater.NO_COMPRESSION);
        CountingOutputStream cout = new CountingOutputStream(zout);

        ZipEntry zipEntry;
        CheckedInputStream ckin;
        InputStream in;
        HashSet<String> fileNames = new HashSet<String>();
        try {
            for (CMSBinaryContent cmsBinaryContent : contents) {
                if (cmsBinaryContent.getFile() != null || cmsBinaryContent.getStream() != null) {
                    String filename = deduplicateFileName(fileNames, cmsBinaryContent.getName());
                    zipEntry = new ZipEntry(filename);
                    zipEntry.setSize(cmsBinaryContent.getFileSize());
                    zipEntry.setCompressedSize(-1);

                    byte[] b = new byte[1000000];
                    if (cmsBinaryContent.getFile() != null) {
                        ckin = new CheckedInputStream(new FileInputStream(cmsBinaryContent.getFile()), new CRC32());
                        zipEntry.setTime(cmsBinaryContent.getFile().lastModified());
                        // calculate crc
                        try {
                            while (ckin.read(b) >= 0) {
                            }
                            zipEntry.setCrc(ckin.getChecksum().getValue());

                            zout.putNextEntry(zipEntry);
                        } finally {
                            IOUtils.closeQuietly(ckin);
                        }

                        in = new FileInputStream(cmsBinaryContent.getFile());

                        // write to zip
                        try {
                            int i = -1;
                            while ((i = in.read(b)) != -1) {
                                cout.write(b, 0, i);
                            }
                            cout.flush();
                        } finally {
                            IOUtils.closeQuietly(in);
                        }
                    } else {
                        ckin = new CheckedInputStream(cmsBinaryContent.getStream(), new CRC32());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        // calculate crc
                        try {
                            int i = -1;
                            while ((i = ckin.read(b)) != -1) {
                                baos.write(b, 0, i);
                            }
                            zipEntry.setCrc(ckin.getChecksum().getValue());
                            zout.putNextEntry(zipEntry);
                        } finally {
                            IOUtils.closeQuietly(ckin);
                        }

                        // write to zip
                        try {
                            baos.writeTo(cout);
                            cout.flush();
                        } finally {
                            IOUtils.closeQuietly(baos);
                        }
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(cout);
        }

        CMSBinaryContent content = new CMSBinaryContent();
        content.setName(DEFAULT_FILENAME);
        content.setFile(tempFile);
        content.setMimeType(ZIP_MIMETYPE);
        content.setFileSize(cout.getByteCount());

        return content;
    }

    private String deduplicateFileName(HashSet<String> fileNames, String fileName) {
        if (fileNames.contains(fileName)) {
            // is a duplicate
            while (fileNames.contains(fileName)) {
                // build a suffixed filename till unicity

                Matcher dedupeMatcher = DEDUPED_BASENAME_PATTERN.matcher(fileName);
                StringBuilder fileNameBuilder = new StringBuilder();
                if (dedupeMatcher.matches()) {
                    // of another duplicate
                    fileNameBuilder.append(dedupeMatcher.group(1));
                    Integer increment = Integer.valueOf(dedupeMatcher.group(2)) + 1;
                    fileNameBuilder.append(increment);
                    fileNameBuilder.append(dedupeMatcher.group(3));
                } else {
                    // first duplicate
                    String[] splitedFileName = StringUtils.split(fileName, '.');

                    for (int i = 0; i < splitedFileName.length; i++) {
                        if (i == splitedFileName.length - 1) {
                            fileNameBuilder.append("__");
                            fileNameBuilder.append("1");
                            fileNameBuilder.append(".");
                        }
                        fileNameBuilder.append(splitedFileName[i]);
                    }
                }
                fileName = fileNameBuilder.toString();
            }
        }

        fileNames.add(fileName);
        return fileName;
    }

    @Override
    public String getId() {
        return "BulkFilesCommand/" + StringUtils.join(paths, ',');
    }

}
