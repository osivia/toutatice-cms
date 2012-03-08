package fr.toutatice.portail.cms.nuxeo.core;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.RemoteException;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Blob;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.FileBlob;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;


/**
 * @author cap2j
 */
public class ResourceUtil {

	private ResourceUtil() {
		super();
	}

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
			in.close();
		}
	}

	public static String getString(InputStream in, String charSet) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out, 4096);
		return new String(out.toByteArray(), charSet);
	}

	
	
	private static class BinaryContentCommand implements INuxeoCommand {
		
		String path;
		String fileIndex;
		
		public BinaryContentCommand(String path, String fileIndex) {
			super();
			this.path = path;
			this.fileIndex = fileIndex;
		}
		
		public Object execute( Session session)	throws Exception {
			
			Document doc = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set(
					"value", path).execute();
			
			Blob blob = null;
			
			try	{
				blob = (Blob) session.newRequest("Blob.Get").setInput(doc).set("xpath",
					"files:files/item[" + fileIndex + "]/file").execute();
			} catch( Exception e){
				// Le not found n'est pas traité pour les blob
				// On le positionne par défaut pour l'utilisateur
				throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
			}
			
	


			InputStream in = blob.getStream();

			File tempFile = File.createTempFile("tempFile", ".tmp");
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

			BinaryContent content = new BinaryContent();

			content.setName(blob.getFileName());
			content.setFile(tempFile);
			content.setMimeType(blob.getMimeType());

			return content;
			

		
		};		
		
		public String getId() {
			return "BinaryContentCommand"+path+"/"+fileIndex;
		};		

		
	}
	
	
	
	public static BinaryContent getBinaryContent(NuxeoController ctx, String path, String fileIndex) throws Exception {

		return (BinaryContent) ctx.executeNuxeoCommand(new BinaryContentCommand(path,fileIndex));
		



	}
	
	private static class FileContentCommand implements INuxeoCommand {
		
		String documentPath;
		String fieldName;
		
		public FileContentCommand(String documentPath, String fieldName) {
			super();
			this.documentPath = documentPath;
			this.fieldName = fieldName;
		}
		
		public Object execute( Session session)	throws Exception {
			
			Document doc = (Document) session.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set(
					"value", documentPath).execute();
			
			 PropertyMap map = doc.getProperties().getMap(fieldName);

		     String pathFile = map.getString("data");

		     // download the file from its remote location
		     FileBlob   blob = (FileBlob) session.getFile(pathFile);
		     
		 	/* Construction résultat */

				InputStream in = new FileInputStream(blob.getFile());

				File tempFile = File.createTempFile("tempFile", ".tmp");
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
				
				BinaryContent content = new BinaryContent();

				content.setName(blob.getFileName());
				content.setFile(tempFile);
				content.setMimeType(blob.getMimeType());

				return content;
		
		};		
		
		public String getId() {
			return "FileContentCommand"+documentPath+"/"+fieldName;
		};		

		
	}
	
	public static BinaryContent getFileContent(NuxeoController ctx, String documentPath, String fieldName) throws Exception {
		return(BinaryContent) ctx.executeNuxeoCommand(new FileContentCommand(documentPath,fieldName));
	}	

	
      private static class PictureContentCommand implements INuxeoCommand {
		
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

								File tempFile = File.createTempFile("tempFile", ".tmp");
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
								
								BinaryContent content = new BinaryContent();

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
			return "BinaryContentCommand"+path+"/"+content;
		};		

		
	}
	
	
	
	public static BinaryContent getPictureContent(NuxeoController ctx, String path, String content) throws Exception {

		return (BinaryContent) ctx.executeNuxeoCommand(new PictureContentCommand(path,content));
		



	}
	
	

}