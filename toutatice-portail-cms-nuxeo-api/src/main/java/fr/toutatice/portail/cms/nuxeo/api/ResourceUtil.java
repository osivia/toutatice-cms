/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.api;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSBinaryContent;


/**
 * The Class ResourceUtil.
 * 
 * Manipulates blobs
 */
public class ResourceUtil {

    /**
     * Instantiates a new resource util.
     */
    private ResourceUtil() {
        super();
    }

    /**
     * Copy.
     * 
     * @param inputStream the input stream
     * @param outputStream the output stream
     * @param bufSize the buf size
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, int bufSize) throws IOException {
        InputStream in = inputStream;
        BufferedOutputStream out = new BufferedOutputStream(outputStream, bufSize);
        try {
            byte[] b = new byte[4096];
            int i = -1;
            while ((i = in.read(b)) != -1) {
                out.write(b, 0, i);
            }
            out.flush();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Gets the string.
     * 
     * @param in the in
     * @param charSet the char set
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String getString(InputStream in, String charSet) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out, 4096);
        return new String(out.toByteArray(), charSet);
    }


    /**
     * The Class CMSBinaryContentCommand.
     */
    private static class CMSBinaryContentCommand implements INuxeoCommand {

        /** The path. */
        String path;

        /** The file index. */
        String fileIndex;

        /**
         * Instantiates a new CMS binary content command.
         * 
         * @param path the path
         * @param fileIndex the file index
         */
        public CMSBinaryContentCommand(String path, String fileIndex) {
            super();
            this.path = path;
            this.fileIndex = fileIndex;
        }

        /*
         * (non-Javadoc)
         * 
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
         */
        public Object execute(Session session) throws Exception {

            Document doc = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path).execute();

            Blob blob = null;

            try {
                blob = (Blob) session.newRequest("Blob.Get").setInput(doc).set("xpath", "files:files/item[" + fileIndex + "]/file").execute();
            } catch (Exception e) {
                // Le not found n'est pas traité pour les blob
                // On le positionne par défaut pour l'utilisateur
                throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }


            InputStream in = blob.getStream();

            File tempFile = File.createTempFile("tempFile3", ".tmp");
            CountingOutputStream cout = new CountingOutputStream(new FileOutputStream(tempFile));

            try {
                byte[] b = new byte[4096];
                int i = -1;
                while ((i = in.read(b)) != -1) {
                    cout.write(b, 0, i);
                }
                cout.flush();
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(cout);
            }

            CMSBinaryContent content = new CMSBinaryContent();

            content.setName(blob.getFileName());
            content.setFile(tempFile);
            content.setMimeType(blob.getMimeType());
            content.setFileSize(cout.getByteCount());

            return content;


        };

        /*
         * (non-Javadoc)
         * 
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
         */
        public String getId() {
            return "CMSBinaryContentCommand" + path + "/" + fileIndex;
        };


    }


    /**
     * Gets the CMS binary content.
     * 
     * @param ctx the ctx
     * @param path the path
     * @param fileIndex the file index
     * @return the CMS binary content
     * @throws Exception the exception
     */
    public static CMSBinaryContent getCMSBinaryContent(NuxeoController ctx, String path, String fileIndex) throws Exception {

        return (CMSBinaryContent) ctx.executeNuxeoCommand(new CMSBinaryContentCommand(path, fileIndex));

    }


    /**
     * The Class PictureContentCommand.
     */
    private static class PictureContentCommand implements INuxeoCommand {

        /** The path. */
        String path;

        /** The content. */
        String content;

        /**
         * Instantiates a new picture content command.
         * 
         * @param path the path
         * @param content the content
         */
        public PictureContentCommand(String path, String content) {
            super();
            this.path = path;
            this.content = content;
        }

        /*
         * (non-Javadoc)
         * 
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
         */
        public Object execute(Session session) throws Exception {

            Document doc = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path).execute();

            PropertyList views = doc.getProperties().getList("picture:views");

            if (views != null) {
                for (Object viewObject : views.list()) {
                    if (viewObject instanceof PropertyMap) {
                        PropertyMap view = (PropertyMap) viewObject;
                        String title = view.getString("title");
                        if (content.equals(title)) {

                            PropertyMap fileContent = view.getMap("content");

                            String pathFile = fileContent.getString("data");

                            // download the file from its remote location
                            FileBlob blob = (FileBlob) session.getFile(pathFile);

                            /* Builds the result */

                            InputStream in = new FileInputStream(blob.getFile());

                            File tempFile = File.createTempFile("tempFile2", ".tmp");
                            CountingOutputStream cout = new CountingOutputStream(new FileOutputStream(tempFile));

                            try {
                                byte[] b = new byte[4096];
                                int i = -1;
                                while ((i = in.read(b)) != -1) {
                                    cout.write(b, 0, i);
                                }
                                cout.flush();
                            } finally {
                                IOUtils.closeQuietly(in);
                                IOUtils.closeQuietly(cout);
                            }

                            blob.getFile().delete();

                            CMSBinaryContent content = new CMSBinaryContent();

                            content.setName(blob.getFileName());
                            content.setFile(tempFile);
                            content.setMimeType(blob.getMimeType());
                            content.setFileSize(cout.getByteCount());

                            return content;


                        }
                    }

                }
            }


            throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);

        };

        /*
         * (non-Javadoc)
         * 
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
         */
        public String getId() {
            return "CMSBinaryContentCommand" + path + "/" + content;
        };


    }


    /**
     * Gets the picture content.
     * 
     * @param ctx the ctx
     * @param path the path
     * @param content the content
     * @return the picture content
     * @throws Exception the exception
     */
    public static CMSBinaryContent getPictureContent(NuxeoController ctx, String path, String content) throws Exception {

        return (CMSBinaryContent) ctx.executeNuxeoCommand(new PictureContentCommand(path, content));


    }


    /**
     * The Class BlobHolderCommand.
     */
    private static class BlobHolderCommand implements INuxeoCommand {

        /** The path. */
        String path;

        /** The blob index. */
        String blobIndex;

        /**
         * Instantiates a new blob holder command.
         * 
         * @param path the path
         * @param blobIndex the blob index
         */
        public BlobHolderCommand(String path, String blobIndex) {
            super();
            this.path = path;
            this.blobIndex = blobIndex;
        }

        /*
         * (non-Javadoc)
         * 
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
         */
        public Object execute(Session session) throws Exception {

            Document doc = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path).execute();

            Blob blob = null;

            try {
                blob = (Blob) session.newRequest("Blob.Get").setInput(doc).set("xpath", "/file:content").execute();
            } catch (Exception e) {
                // Le not found n'est pas traité pour les blob
                // On le positionne par défaut pour l'utilisateur
                throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }


            InputStream in = blob.getStream();

            File tempFile = File.createTempFile("tempFile4", ".tmp");
            CountingOutputStream cout = new CountingOutputStream(new FileOutputStream(tempFile));

            try {
                byte[] b = new byte[4096];
                int i = -1;
                while ((i = in.read(b)) != -1) {
                    cout.write(b, 0, i);
                }
                cout.flush();
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(cout);
            }

            CMSBinaryContent content = new CMSBinaryContent();

            content.setName(blob.getFileName());
            content.setFile(tempFile);
            content.setMimeType(blob.getMimeType());
            content.setFileSize(cout.getByteCount());

            return content;


        };

        /*
         * (non-Javadoc)
         * 
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
         */
        public String getId() {
            return "BlobHolderCommand" + path + "/" + blobIndex;
        };


    }

    /**
     * Gets the blob holder content.
     * 
     * @param ctx the ctx
     * @param path the path
     * @param fileIndex the file index
     * @return the blob holder content
     * @throws Exception the exception
     */
    public static CMSBinaryContent getBlobHolderContent(NuxeoController ctx, String path, String fileIndex) throws Exception {

        return (CMSBinaryContent) ctx.executeNuxeoCommand(new BlobHolderCommand(path, fileIndex));

    }


}