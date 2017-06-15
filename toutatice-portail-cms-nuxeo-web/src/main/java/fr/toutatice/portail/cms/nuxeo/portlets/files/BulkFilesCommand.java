package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;

public class BulkFilesCommand implements INuxeoCommand{

    private static final String FILE_FIELD_NAME = "file:content";

    private static final String SIZE_FIELD = "common:size";

    private static final String DEFAULT_FILENAME = "export.zip";

    private static final String ZIP_MIMETYPE = "application/zip";

    private static final long MAX_SIZE = 100000000L; // 100MB

    private String[] paths;

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
            Long fileSize = documentContext.getDoc().getLong(SIZE_FIELD);
            if (fileSize == null) {
                throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);
            }
            sizeSum += fileSize;
            if(sizeSum>MAX_SIZE){
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
        try {
            for (CMSBinaryContent cmsBinaryContent : contents) {
                if (cmsBinaryContent.getFile() != null) {
                    zipEntry = new ZipEntry(cmsBinaryContent.getName());
                    zipEntry.setTime(cmsBinaryContent.getFile().lastModified());
                    zipEntry.setSize(cmsBinaryContent.getFileSize());
                    zipEntry.setCompressedSize(-1);

                    ckin = new CheckedInputStream(new FileInputStream(cmsBinaryContent.getFile()), new CRC32());
                    byte[] b = new byte[1000000];
                    // calculate crc
                    try {
                        while (ckin.read(b) >= 0) {
                        }
                        zipEntry.setCrc(ckin.getChecksum().getValue());

                        zout.putNextEntry(zipEntry);
                    } finally {
                        IOUtils.closeQuietly(ckin);
                    }

                    // write to zip
                    in = new FileInputStream(cmsBinaryContent.getFile());
                    try {
                        int i = -1;
                        while ((i = in.read(b)) != -1) {
                            cout.write(b, 0, i);
                        }
                        cout.flush();
                    } finally {
                        IOUtils.closeQuietly(in);
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

    @Override
    public String getId() {
        return "BulkFilesCommand/" + StringUtils.join(paths, ',');
    }

}
