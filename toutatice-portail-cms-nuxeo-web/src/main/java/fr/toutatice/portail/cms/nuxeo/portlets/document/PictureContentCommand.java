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
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;

public class PictureContentCommand implements INuxeoCommand {

	String path;
	String content;
	
	public PictureContentCommand(String path, String content) {
		super();
		this.path = path;
		this.content = content;
	}
	
	public Object execute( Session session)	throws Exception {
		
		Document doc = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set(
				"value", path).execute();
		
		 PropertyList views = doc.getProperties().getList("picture:views");
		 
		 if( views != null)	{
			 for( Object viewObject: views.list())	{
				 if( viewObject instanceof PropertyMap){
					 PropertyMap view = (PropertyMap) viewObject;
					 String title = view.getString("title");
					 if( content.equals(title))	{
						 
						 PropertyMap fileContent = view.getMap("content");
						 
					     String pathFile = fileContent.getString("data");

					     // download the file from its remote location
					     FileBlob   blob = (FileBlob) session.getFile(pathFile);
					     
					 	/* Construction résultat */

							InputStream in = new FileInputStream(blob.getFile());

							File tempFile = File.createTempFile("tempFile2", ".tmp");
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

							content.setName(blob.getFileName());
							content.setFile(tempFile);
							content.setMimeType(blob.getMimeType());

							return content;
						 
						 
					 }
				 }
				 
			 }
		 }



		throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
	
	};		
	
	public String getId() {
		return "CMSBinaryContentCommand"+path+"/"+content;
	};		


}
