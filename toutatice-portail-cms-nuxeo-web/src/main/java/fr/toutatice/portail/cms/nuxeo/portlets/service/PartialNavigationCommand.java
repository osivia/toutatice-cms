package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class PartialNavigationCommand implements INuxeoCommand {


	CMSItem publishSpaceConfig;
	Map<String, NavigationItem> navItems;
	List<String> docIds;
	public final static String basicNavigationSchemas = "dublincore,common, toutatice";
	boolean fetchRoot = false;
	String commandPath;
	
	public PartialNavigationCommand(  CMSItem publishSpaceConfig, Map<String, NavigationItem> navItems, List<String> docIds, boolean fetchRoot, String commandPath) {
		super();

		this.publishSpaceConfig = publishSpaceConfig;
		this.navItems = navItems;
		this.docIds = docIds;
		this.fetchRoot = fetchRoot;
		this.commandPath = commandPath;
	}


	
	public Object execute(Session session) throws Exception {
		
		String path = publishSpaceConfig.getPath();

				
		OperationRequest request;

		request = session.newRequest("Document.Query");

		
		String itemRequest = "";
		
	    if(fetchRoot){
            itemRequest = "ecm:uuid ='"+ ((Document) publishSpaceConfig.getNativeItem()).getId()+"'";
        }
        		
	
		
        for(String docId : docIds){
            if( itemRequest.length() >  0)
                itemRequest += " OR";
            // Modif-PictureBook-begin
            itemRequest += " ecm:parentId = '" + docId + "'";
            //itemRequest += " ecm:uuid = '" + docId + "'";
            // Modif-PictureBook-end
        }
        
		String nuxeoRequest = "( " + itemRequest + ")  AND  (ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1) ";
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, true, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
	
		request.set("query", "SELECT * FROM Document WHERE " + filteredRequest + " ORDER BY ecm:pos");
		
			
		String navigationSchemas = basicNavigationSchemas;
		
		String extraNavigationSchemas = System.getProperty("nuxeo.navigationSchemas");
		
		if( extraNavigationSchemas != null)
			navigationSchemas += "," + extraNavigationSchemas;

		request.setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas);

		//request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
		// Build navItems

		Documents children = (Documents) request.execute();
		
		
		
		// Iterate over childrens to update hierarchy

		for (Document child : children) {

			NavigationItem navItem;

			/* Update current Item */
			String navPath = child.getPath();
			
			navItem = navItems.get(child.getPath());
			if (navItem == null) {

				navItem = new NavigationItem();
				navItem.setUnfetchedChildren(true);
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
			
			//v2.0-SP1 : éviter les doublons d'enfants
			
			boolean isAlreadyAChild = false;
			
			for( Object iChild: navItem.getChildren())	{
				Document childDoc = (Document) iChild;
				if( childDoc.getPath().equals(child.getPath()))
						isAlreadyAChild = true;	
			}
			
			if( !isAlreadyAChild)
				navItem.getChildren().add(child);
			}
		}
		
		
		
		/* Mark children as fetched */
		
		
		for(Entry<String, NavigationItem> navItemEntry: navItems.entrySet()){
			NavigationItem navItem = navItemEntry.getValue();
			if( navItem.isUnfetchedChildren()){
				Document doc = (Document) navItem.getMainDoc();
				if( doc != null)
					if( docIds.contains(doc.getId()))
							navItem.setUnfetchedChildren(false);
			}
		}
		



		return navItems;

	}

	public String getId() {
		return "PartialNavigationCommand/" + commandPath;
	};

}
