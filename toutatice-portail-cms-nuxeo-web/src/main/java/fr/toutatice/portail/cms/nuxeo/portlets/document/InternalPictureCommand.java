package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Blob;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
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
	
	public Object execute( Session session)	throws Exception {
		
		Blob blob = null;
		
		try	{
			blob = (Blob) session.newRequest("Blob.Get").setInput(containerDoc).set("xpath",
				"ttc:images/item[" + pictureIndex + "]/file").execute();
		} catch( Exception e){
			// Le not found n'est pas traité pour les blob
			// On le positionne par défaut pour l'utilisateur
			throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
		}
		


		InputStream in = blob.getStream();

		File tempFile = File.createTempFile("tempFile4", ".tmp");
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

		CMSBinaryContent content = new CMSBinaryContent();

		content.setName(blob.getFileName());
		content.setFile(tempFile);
		content.setMimeType(blob.getMimeType());

		return content;
		

	
	};		
	
	public String getId() {
		return "InternalPictureCommand"+containerDoc+"/"+pictureIndex;
	};		

	
}
