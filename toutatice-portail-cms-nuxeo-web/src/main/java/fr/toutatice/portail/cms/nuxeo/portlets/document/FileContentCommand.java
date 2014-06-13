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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.spi.StreamedSession;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.nuxeo.ecm.automation.client.model.StreamBlob;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


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

	
	public Object execute( Session session)	throws Exception {

		if (document == null) {
			document = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*")
					.set("value", docPath).execute();
		}

		PropertyMap map = document.getProperties().getMap(fieldName);

		String pathFile = map.getString("data");
		
		if( streamingSupport) {
		    
		    StreamBlob blob = (StreamBlob) ((StreamedSession) session).getStreamedFile(pathFile);
		    
		    CMSBinaryContent content = new CMSBinaryContent();
            
            String fileName = blob.getFileName();
            if( fileName == null || "null".equals(fileName)){
                
                // Pb. sur l'upload, on prend le nom du document
                fileName = document.getTitle();
            }

            content.setName(fileName);
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
			
			OutputStream out = new FileOutputStream(tempFile);
			

			try {
				byte[] b = new byte[1000000];
				int i = -1;
				while ((i = in.read(b)) != -1) {
					out.write(b, 0, i);
				}
				out.flush();
			} finally {
				in.close();
				out.close();
			}

			blob.getFile().delete();
			
			CMSBinaryContent content = new CMSBinaryContent();
			
			// JSS v 1.0.10 : traitement nom fichier à null
			
			String fileName = blob.getFileName();
			if( fileName == null || "null".equals(fileName)){
				
				// Pb. sur l'upload, on prend le nom du document
				fileName = document.getTitle();
			}

			content.setName(fileName);
			content.setFile(tempFile);
			content.setMimeType(blob.getMimeType());

			return content;
	
	};		
	

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }


    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
		String id = "FileContentCommand";
		if(document == null){
			id += docPath;
		}else{
			id += document;
		}

        if (timestamp != null) {
            id += timestamp;
        }

		return id += "/"+fieldName;
	};		


}
