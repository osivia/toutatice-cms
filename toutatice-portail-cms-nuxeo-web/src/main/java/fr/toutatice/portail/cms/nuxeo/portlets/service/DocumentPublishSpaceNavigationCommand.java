package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;
import fr.toutatice.portail.core.cms.NavigationItem;


/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class DocumentPublishSpaceNavigationCommand implements INuxeoCommand {

	String path;
	boolean live;
	
	public DocumentPublishSpaceNavigationCommand(  String path, boolean live) {
		super();
		this.path = path;
		this.live = live;
	}

	public static String  computeNavPath(String path){
		String result = path;
		if( path.endsWith(".proxy"))
			result = result.substring(0, result.length() - 6);
		return result;
	}
	
	public Object execute(Session session) throws Exception {

		/*
		org.nuxeo.ecm.automation.client.jaxrs.model.Document publishSpace = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
				.newRequest("Document.FetchPublishSpace").setHeader(Constants.HEADER_NX_SCHEMAS, "*")
				.set("value", path).execute();
*/
		
		
		OperationRequest request;

		request = session.newRequest("Document.Query");

		// TODO : gerer le PortalVirtualPage de maniere générique
		
		String spacePath = path;
		if( !live)
			spacePath += ".proxy";
		
		String nuxeoRequest = "( ecm:path = '" + spacePath + "' OR ecm:path STARTSWITH '" + path + "')  AND (  ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1 OR ecm:primaryType = 'PortalVirtualPage')";
		
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, live);
	
		request.set("query", "SELECT * FROM Document WHERE " + filteredRequest + " ORDER BY ecm:pos");
		
		//test sans proxy ok
		//String nuxeoRequest = "ecm:parentId = 'a984744a-838c-4f89-9627-50acec8df78b'";
		//request.set("query", "SELECT * FROM Document WHERE " + nuxeoRequest + " ORDER BY ecm:pos");
		


		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice, wcm_navigation");

		//request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
		// Build navItems
		Map<String, NavigationItem> navItems = new HashMap<String, NavigationItem>();

		Documents children = (Documents) request.execute();

		for (Document child : children) {

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

		/* Sort children */
		
		/*

		for (Entry<String, NavigationItem> item : navItems.entrySet()) {
			
			
			List<Document> childrens = item.getValue().getChildren();

			Collections.sort(childrens, new Comparator<Document>() {

				public int compare(Document e1, Document e2) {
					int pos1 = 0;
					int pos2 = 0;

					try {
						String sPos1 = (String) e1.getProperties().get("ecm:pos");
						if (sPos1 != null)
							pos1 = Integer.parseInt(sPos1);

						String sPos2 = (String) e2.getProperties().get("ecm:pos");
						if (sPos2 != null)
							pos2 = Integer.parseInt(sPos2);
					} catch (Exception e) {

					}
					if (pos1 < pos2)
						return -1;
					else
						return 1;
				}
			});

		}
		*/

		return navItems;

	}

	public String getId() {
		return "PublishSpaceNavigationCommand/" + path;
	};

}
