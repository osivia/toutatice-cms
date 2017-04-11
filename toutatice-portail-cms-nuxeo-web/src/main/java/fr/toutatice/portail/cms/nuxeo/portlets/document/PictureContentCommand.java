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
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;

public class PictureContentCommand implements INuxeoCommand {

    Document image;
    String content;

    public PictureContentCommand(Document image, String content) {
        super();
        this.image = image;
        this.content = content;
    }

    @Override
    public Object execute(Session session) throws Exception {

        PropertyList views = image.getProperties().getList("picture:views");

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

                        /* Construction résultat */

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

    @Override
    public String getId() {
        return "CMSBinaryContentCommand" + image + "/" + content;
    };


}
