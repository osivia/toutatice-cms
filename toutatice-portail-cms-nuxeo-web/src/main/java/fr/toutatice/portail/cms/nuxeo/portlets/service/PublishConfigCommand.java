package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class PublishConfigCommand implements INuxeoCommand {
	
	private String path;
	private CMSServiceCtx cmsCtx;
	private CMSService cmsService;

	public PublishConfigCommand(CMSService cmsService, CMSServiceCtx cmsCtx, String path) {
		this.path = path;
		this.cmsCtx = cmsCtx;
		this.cmsService = cmsService;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		 Document configDoc = (Document) nuxeoSession.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
				.set("value", path).execute();
		
		String livePath = DocumentPublishSpaceNavigationCommand.computeNavPath(configDoc.getPath());
		CMSItem configItem = cmsService.createItem(cmsCtx, configDoc.getPath(), livePath, configDoc);
		CMSItem publishSpaceItem = new CMSItem(livePath, null, configDoc);
		cmsService.getCustomizer().getNavigationItemAdaptor().adaptPublishSpaceNavigationItem(configItem, publishSpaceItem);
		return configItem;
	}

	public String getId() {
		return "PublishConfigCommand-" + path;
	}

}
