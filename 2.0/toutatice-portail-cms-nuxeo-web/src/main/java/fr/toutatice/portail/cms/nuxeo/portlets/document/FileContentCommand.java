package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.FileBlob;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


public class FileContentCommand implements INuxeoCommand {

	Document document;
	String docPath;
	String fieldName;
	
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
	
	public Object execute( Session session)	throws Exception {

		if (document == null) {
			document = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*")
					.set("value", docPath).execute();
		}

		PropertyMap map = document.getProperties().getMap(fieldName);

		String pathFile = map.getString("data");

		// download the file from its remote location
		FileBlob blob = (FileBlob) session.getFile(pathFile);
	     
	 	/* Construction résultat */

			InputStream in = new FileInputStream(blob.getFile());

			File tempFile = File.createTempFile("tempFile1", ".tmp");
			OutputStream out = new FileOutputStream(tempFile);

			try {
				byte[] b = new byte[4096];
				int i = -1;
				while ((i = in.read(b)) != -1) {
					out.write(b, 0, i);
				}
				out.flush();
			} finally {
				in.close();
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
	
	public String getId() {
		String id = "FileContentCommand";
		if(document == null){
			id += docPath;
		}else{
			id += document;
		}
		return id += "/"+fieldName;
	};		


}
