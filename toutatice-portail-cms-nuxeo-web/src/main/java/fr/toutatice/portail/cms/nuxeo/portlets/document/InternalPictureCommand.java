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
 *
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;


public class InternalPictureCommand implements INuxeoCommand {

	Document containerDoc;
	String pictureIndex;

	public InternalPictureCommand(Document containerDoc, String pictureIndex) {
		super();
		this.containerDoc = containerDoc;
		this.pictureIndex = pictureIndex;
	}

	@Override
    public Object execute( Session session)	throws Exception {

		FileBlob blob = null;

		try	{
			blob = (FileBlob) session.newRequest("Blob.Get").setInput(containerDoc).set("xpath",
				"ttc:images/item[" + pictureIndex + "]/file").execute();
		} catch( Exception e){
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

			if(blob != null & blob.getFile() != null) {
				blob.getFile().delete();
			}

		}

		CMSBinaryContent content = new CMSBinaryContent();

		content.setName(blob.getFileName());
		content.setFile(tempFile);
		content.setMimeType(blob.getMimeType());
        content.setFileSize(cout.getByteCount());
        content.setFileSize(cout.getByteCount());


		return content;



	};

	@Override
    public String getId() {
		return "InternalPictureCommand"+containerDoc+"/"+pictureIndex;
	};


}
