package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	public Object execute( Session session)	throws Exception {
		
		 PropertyList views = image.getProperties().getList("picture:views");
		 
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
								out.close();
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
		return "CMSBinaryContentCommand"+image+"/"+content;
	};		


}
