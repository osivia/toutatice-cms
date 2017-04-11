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
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.spi.StreamedSession;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.nuxeo.ecm.automation.client.model.StreamBlob;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;

/**
 * File content command.
 *
 * @see INuxeoCommand
 */
public class FileContentCommand implements INuxeoCommand {

    Document document;
    String docPath;
    String fieldName;

    /** special resources such as avatar need to be reloaded without cache. Add a timestamp to force reload in nuxeo. */
    String timestamp;
    boolean streamingSupport = false;

    public FileContentCommand(Document document, String fieldName) {
        super();
        this.document = document;
        this.docPath = null;
        this.fieldName = fieldName;
    }

    public FileContentCommand(String docPath, String fieldName) {
        super();
        this.document = null;
        this.docPath = docPath;
        this.fieldName = fieldName;
    }

    public void setStreamingSupport(boolean streamingSupport) {
        this.streamingSupport = streamingSupport;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        if (this.document == null) {
            this.document = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", this.docPath).execute();
        }

        String tokens[] = this.fieldName.split("/");

        PropertyMap map = this.document.getProperties().getMap(tokens[0]);

        for(int i=1; i<tokens.length; i++){
            map = map.getMap(tokens[i]);
        }



        String pathFile = map.getString("data");

        if (this.streamingSupport) {
            String url = null;

            if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_60)) {
                url = pathFile;
            } else {
                url = session.getClient().getBaseUrl() + pathFile;
            }


            StreamBlob blob = (StreamBlob) ((StreamedSession) session).getStreamedFile(url);

            CMSBinaryContent content = new CMSBinaryContent();

            String fileName = blob.getFileName();
            if ((fileName == null) || "null".equals(fileName)) {

                // Pb. sur l'upload, on prend le nom du document
                fileName = this.document.getTitle();
            }


            // File size
            Long fileSize = this.document.getProperties().getLong("common:size");

            content.setName(fileName);
            content.setFileSize(fileSize);
            content.setMimeType(blob.getMimeType());
            content.setStream(blob.getStream());
            content.setLongLiveSession(session);

            return content;
        }

        // download the file from its remote location
        FileBlob blob = (FileBlob) session.getFile(pathFile);


        /* Construction résultat */

        InputStream in = new FileInputStream(blob.getFile());

        File tempFile = File.createTempFile("tempFile", ".tmp");
        tempFile.deleteOnExit();

        CountingOutputStream cout = new CountingOutputStream(new FileOutputStream(tempFile));

        try {
            byte[] b = new byte[1000000];
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

        // JSS v 1.0.10 : traitement nom fichier à null

        String fileName = blob.getFileName();
        if ((fileName == null) || "null".equals(fileName)) {

            // Pb. sur l'upload, on prend le nom du document
            fileName = this.document.getTitle();
        }

        content.setName(fileName);
        content.setFile(tempFile);
        content.setMimeType(blob.getMimeType());
        content.setFileSize(cout.getByteCount());

        return content;

    };


    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return this.timestamp;
    }


    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getId() {
        String id = "FileContentCommand";
        if (this.document == null) {
            id += this.docPath;
        } else {
            id += this.document;
        }

        if (this.timestamp != null) {
            id += this.timestamp;
        }

        return id += "/" + this.fieldName;
    };


}
