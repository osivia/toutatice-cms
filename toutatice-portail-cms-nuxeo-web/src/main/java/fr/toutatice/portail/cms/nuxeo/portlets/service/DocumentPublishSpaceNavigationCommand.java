package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;



/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class DocumentPublishSpaceNavigationCommand implements INuxeoCommand {


	CMSItem publishSpaceConfig;
	
	/** Récupérer aussi les versions en cours de travail */
    private boolean forceLiveVersion;
    
	public final static String basicNavigationSchemas = "dublincore,common, toutatice";
	
    public DocumentPublishSpaceNavigationCommand(CMSItem publishSpaceConfig, boolean forceLiveVersion) {
		super();

		this.publishSpaceConfig = publishSpaceConfig;
        this.forceLiveVersion = forceLiveVersion;
	}

	public static String  computeNavPath(String path){
		String result = path;
		if( path.endsWith(".proxy"))
			result = result.substring(0, result.length() - 6);
		return result;
	}
	
	public Object execute(Session session) throws Exception {
		
		OperationRequest request;

		request = session.newRequest("Document.Query");

		// TODO : gerer le PortalVirtualPage de maniere générique
		
        boolean live = "1".equals(publishSpaceConfig.getProperties().get("displayLiveVersion"));

        if (forceLiveVersion)
        // XXX temp
            live = true;
		
		String uuid =  ((Document)publishSpaceConfig.getNativeItem()).getId();
		
		/*
		String spacePath = path;
		if( !live)
			spacePath += ".proxy";
		*/
		
		String path = publishSpaceConfig.getPath();
		
		//String nuxeoRequest = "( ecm:path = '" + spacePath + "' OR ecm:path STARTSWITH '" + path + "')  AND (  ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1 OR ecm:primaryType = 'PortalVirtualPage')";
		
		// Modif JSS 20130130 : vu avec oliver
		//  1 - filtre uniquement sur ttc:showInMenu
		//  2 - le 'ecm:path =' pose des problemes de perfs quand il est compibné avec un OR ecm:path startswith -> fetch specifique pour récuperer la racine
		//  3 - suppression cas particulier PortalVirtualPage
		
		String nuxeoRequest = "( ecm:path STARTSWITH '" + path + "'  AND  (ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1)  )";
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, live,  InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
	
		request.set("query", "SELECT * FROM Document WHERE " + filteredRequest + " ORDER BY ecm:pos");
		
		//test sans proxy ok
		//String nuxeoRequest = "ecm:parentId = 'a984744a-838c-4f89-9627-50acec8df78b'";
		//request.set("query", "SELECT * FROM Document WHERE " + nuxeoRequest + " ORDER BY ecm:pos");
		
		String navigationSchemas = basicNavigationSchemas;
		
		String extraNavigationSchemas = System.getProperty("nuxeo.navigationSchemas");
		
		if( extraNavigationSchemas != null)
			navigationSchemas += "," + extraNavigationSchemas;

		request.setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas);

		//request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
		// Build navItems
		Map<String, NavigationItem> navItems = new HashMap<String, NavigationItem>();

		Documents children = (Documents) request.execute();
		
		
		/* Make children list */
		
		
		List<Document>  concatDocuments = new ArrayList<Document>();
		
		// Add root document
		// oblige de sortir de la requete principale pour des problemes de perfs
		org.nuxeo.ecm.automation.client.model.Document doc = (org.nuxeo.ecm.automation.client.model.Document) session
		.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas).set("value", uuid).execute();

		concatDocuments.add(doc);

		for (Document child : children) {
			concatDocuments.add(child);
		
		}
		
		// Iterate over childrens to update hierarchy

		for (Document child : concatDocuments) {

			NavigationItem navItem;

			/* Update current Item */
			String navPath = computeNavPath(child.getPath());
			
			navItem = navItems.get(navPath);
			if (navItem == null) {

				navItem = new NavigationItem();
				navItems.put(navPath, navItem);
			}
			navItem.setMainDoc(child);

			/* Update parent children */

			String parentPath = navPath.substring(0, navPath.lastIndexOf('/'));
			if( parentPath.contains(path))	{
				navItem = navItems.get(parentPath);
			if (navItem == null) {
				navItem = new NavigationItem();
				navItems.put(parentPath, navItem);
			}
			navItem.getChildren().add(child);
			}
		}


		return navItems;

	}

	public String getId() {
        String sLive = "false";
        if (forceLiveVersion)
            sLive = "true";
        return "PublishSpaceNavigationCommandT2/" + sLive + "/" + publishSpaceConfig.getPath();
	};

}
