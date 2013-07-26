package fr.toutatice.portail.cms.nuxeo.core;

import org.osivia.portal.api.cache.services.IServiceInvoker;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;




public class ResourceCacheInvoker implements IServiceInvoker {
	

	private static final long serialVersionUID = 7370842800678586226L;
	

	private String path;
	private String fileIndex;
	private NuxeoController ctx;

	

	public ResourceCacheInvoker(  NuxeoController ctx, String path, String fileIndex  ) {
		super();
		this.ctx = ctx;
		this.path = path;		
		this.fileIndex = fileIndex;

	}
	

	public Object invoke() throws Exception {
		return ResourceUtil.getCMSBinaryContent(ctx, path, fileIndex);
	}

	
	
	
	

}
