/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;



/**
 * Return all the navigation items
 *
 * @author jeanseb
 *
 */
public class DocumentPublishSpaceNavigationCommand implements INuxeoCommand {
    
    /** Logger. */
    protected static final Log logger = LogFactory.getLog(CMSService.class);


	CMSItem publishSpaceConfig;

	/** Récupérer aussi les versions en cours de travail */
    private boolean forceLiveVersion;
    
    /** Possibility to use ElasticSearch (available from Nuxeo 6.0) */
    private boolean useES = false;

    public final static String basicNavigationSchemas = "dublincore,common, toutatice";

    public DocumentPublishSpaceNavigationCommand(CMSItem publishSpaceConfig, boolean forceLiveVersion) {
		super();

		this.publishSpaceConfig = publishSpaceConfig;
        this.forceLiveVersion = forceLiveVersion;
        this.useES = NuxeoCompatibility.canUseES();
	}

	@Override
    public Object execute(Session session) throws Exception {
	    
	    boolean canUseES = useES && BooleanUtils.toBoolean(this.publishSpaceConfig.getProperties().get("useES"));

		OperationRequest request;
		
		if(canUseES){
		    request = session.newRequest("Document.QueryES");
		} else {
		    request = session.newRequest("Document.Query");
		}
		

		// TODO : gerer le PortalVirtualPage de maniere générique

        boolean live = "1".equals(this.publishSpaceConfig.getProperties().get("displayLiveVersion"));

        if (this.forceLiveVersion)
        // XXX temp
            live = true;

		String uuid =  ((Document)this.publishSpaceConfig.getNativeItem()).getId();
		String path = this.publishSpaceConfig.getPath();
		
		String parentCriteria = "ecm:path STARTSWITH";
//		if(canUseES){
//		    parentCriteria = "ecm:ancestorId = ";
//		    path = uuid;
//		}
		

		//String nuxeoRequest = "( ecm:path = '" + spacePath + "' OR ecm:path STARTSWITH '" + path + "')  AND (  ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1 OR ecm:primaryType = 'PortalVirtualPage')";

		// Modif JSS 20130130 : vu avec oliver
		//  1 - filtre uniquement sur ttc:showInMenu
		//  2 - le 'ecm:path =' pose des problemes de perfs quand il est compibné avec un OR ecm:path startswith -> fetch specifique pour récuperer la racine
		//  3 - suppression cas particulier PortalVirtualPage

		String nuxeoRequest = "( " + parentCriteria  + " '" + path + "'  AND  (ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1)  )";


        NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext(live? NuxeoQueryFilterContext.STATE_LIVE: NuxeoQueryFilterContext.STATE_DEFAULT, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER );

		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, nuxeoRequest);
		

		request.set("query", "SELECT * FROM Document WHERE " + filteredRequest + " ORDER BY ecm:pos");

		//test sans proxy ok
		//String nuxeoRequest = "ecm:parentId = 'a984744a-838c-4f89-9627-50acec8df78b'";
		//request.set("query", "SELECT * FROM Document WHERE " + nuxeoRequest + " ORDER BY ecm:pos");

		String navigationSchemas = basicNavigationSchemas;

		String extraNavigationSchemas = System.getProperty("nuxeo.navigationSchemas");

		if( extraNavigationSchemas != null)
            navigationSchemas += "," + extraNavigationSchemas;

		if(canUseES){
		    request.set(Constants.HEADER_NX_SCHEMAS, navigationSchemas);
		} else {
		    request.setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas);
		}
		
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
			String navPath = DocumentHelper.computeNavPath(child.getPath());

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

		/*
		if( this.forceLiveVersion){
		    for( NavigationItem item : navItems.values()){
		        logger.info("DocumentPublishSpaceNavigationCommand" + ((Document) item.getMainDoc()).getPath() + ":" + ((Document) item.getMainDoc()).getProperties().getString("ttc:theme"));
		    }
		}
		*/
		

		return navItems;

	}

	@Override
    public String getId() {
        String sLive = "false";
        if (this.forceLiveVersion)
            sLive = "true";
        return "PublishSpaceNavigationCommandT2/" + sLive + "/" + this.publishSpaceConfig.getPath();
	};

}
