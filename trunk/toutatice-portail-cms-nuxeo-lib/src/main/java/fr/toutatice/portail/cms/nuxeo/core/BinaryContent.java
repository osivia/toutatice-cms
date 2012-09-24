package fr.toutatice.portail.cms.nuxeo.core;

import java.io.File;

import fr.toutatice.portail.api.cache.services.ICacheDataListener;

public class BinaryContent implements ICacheDataListener {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}


	private String mimeType;
	
	private File file;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/* Explicitly removed from cache : new cache has replaced old value */
	public void remove() {
		if( file != null)	{
			file.delete();
			file = null;
		}
		
	}
	
	/* Derefrenced files : ie session closed */
	protected void finalize() throws Throwable	{
		if( file != null)	{
			file.delete();
			file = null;
		}
		
	}
	

}
