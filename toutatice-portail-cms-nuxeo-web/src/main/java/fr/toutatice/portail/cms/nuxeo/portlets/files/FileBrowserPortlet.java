package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.path.PortletPathItem;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSPublicationInfos;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DeleteDocumentCommand;
import fr.toutatice.portail.core.nuxeo.DocTypeDefinition;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class FileBrowserPortlet extends CMSPortlet {

    public static final String DISPLAY_MODE_NORMAL = "normal";
    public static final String DISPLAY_MODE_DETAILED = "detailed";

    public static boolean isNavigable(Document doc) {

        return "Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType()) || "Workspace".equals(doc.getType())
                || "WorkspaceRoot".equals(doc.getType()) || "PortalSite".equals(doc.getType()) || "Section".equals(doc.getType())
                || "SectionRoot".equals(doc.getType());

    }

    public static boolean isOrdered(Document doc) {

        return "Workspace".equals(doc.getType()) || "WorkspaceRoot".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())
                || "PortalSite".equals(doc.getType());

    }

   public static boolean isFolderish (Document doc)	{
		return "PictureBook".equals(doc.getType()) || "DocumentUrlContainer".equals(doc.getType()) || isNavigable(doc);
	
	}

	
	
	//2.0.9 : Ajout tri sur folder / libelle 
	// Doit être mis à jour en parallele avec le MenuPortlet.createComparator
	
	public static Comparator<Document> createComparator( final Document parentDoc){
	
		Comparator<Document> comparator = new Comparator<Document>() {

			public int compare(Document e1, Document e2) {
				
				//2.0.9 : ecm:pos jamais renseigné

//				if (isOrdered(parentDoc)) {
//
//					long pos1 = e1.getProperties().getLong("ecm:pos", new Long(1000));
//					long pos2 = e1.getProperties().getLong("ecm:pos", new Long(1000));
//
//					return pos1 > pos2 ? 1 : -1;
//
//				} else {
					if (isFolderish(e1)) {
						if (isFolderish(e2)) {
							return e1.getTitle().toUpperCase().compareTo(e2.getTitle().toUpperCase());
						} else
							return -1;

					} else {
						if (isFolderish(e2)) {
							return 1;
						} else {
							return e1.getTitle().toUpperCase().compareTo(e2.getTitle().toUpperCase());
						}

					}
				//}
			}
	 };
		
		return comparator;
	};
	
	// v2.1 WORKSPACE
	   public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
	    throws PortletException, IOException {

	        try {
	            // Redirection sur lien contextuel
	            
	            if ("fileActions".equals(resourceRequest.getParameter("type"))) {

                NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());

                String id = resourceRequest.getResourceID();

                CMSPublicationInfos pubInfos = ctx.getCMSService().getPublicationInfos(ctx.getCMSCtx(), id);
                StringBuffer sb = new StringBuffer();


                sb.append("<div>");
                int nbItems = 0;
                if (pubInfos.isEditableByUser()) {
                    
                    Document doc = ctx.fetchDocument(id);

                    Map<String, DocTypeDefinition> managedTypes = ctx.getDocTypeDefinitions();
                    DocTypeDefinition docTypeDef = managedTypes.get(doc.getType());

                    if (docTypeDef != null && docTypeDef.isSupportingPortalForm()) {
                        
                        String divId = (String) resourceRequest.getAttribute("osivia.window.ID");
                        
                        // Force to reload portlet
                        PortletURL portletURL = resourceResponse.createRenderURL();
                        portletURL.setParameter("reloadDatas", ""+ System.currentTimeMillis());

                        sb.append("<a class=\"fancyframe_refresh\" onClick=\"setCallbackParams('"+divId+"', '"+portletURL.toString()+"')\" href=\"" + ctx.getNuxeoPublicBaseUri() + "/nxpath/default" + pubInfos.getDocumentPath()
                                + "@toutatice_edit\">Modifier</a>");
                        nbItems++;
                        
                    }
                    if( nbItems > 0)
                        sb.append("<br/>");
                    
                     sb.append("<a target=\"nuxeo\" href=\"" + ctx.getNuxeoPublicBaseUri() + "/nxdoc/default/"  +  pubInfos.getLiveId() + "/view_documents\">Gérer</a>");
                     nbItems++;
                     
                }
                
                //Modif-FILEBROWSER-begin
				if (pubInfos.isDeletableByUser()) {
					sb.append("<br/>");
					sb.append("<a class=\"fancybox_inline\" href=\"#div_delete_file-item\"");
					sb.append("onclick=\"document.getElementById('currentFileItemId').value='");
					sb.append(id);
					sb.append("';\">Supprimer</a>");
					nbItems++;
				}
               
                
                if( nbItems ==0)
                    sb.append("<b>Aucune action<br/>disponible</b>");
                //Modif-FILEBROWSER-end
                    
                sb.append("<div>");
                resourceResponse.getPortletOutputStream().write(sb.toString().getBytes());
                resourceResponse.getPortletOutputStream().close();

	            }   else
	                super.serveResource(resourceRequest, resourceResponse);

	        } catch (NuxeoException e) {
	            serveResourceException(resourceRequest, resourceResponse, e);
	        } catch (Exception e) {
	            throw new PortletException(e);

	        }
	    }

	
	
	
    public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

        logger.debug("processAction ");

        if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

            PortalWindow window = WindowFactory.getWindow(req);
            window.setProperty("osivia.nuxeoPath", req.getParameter("nuxeoPath"));

            if (req.getParameter("displayLiveVersion") != null && req.getParameter("displayLiveVersion").length() > 0)
                window.setProperty("osivia.cms.displayLiveVersion", req.getParameter("displayLiveVersion"));
            else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
                window.setProperty("osivia.cms.displayLiveVersion", null);

            // v2.0.5
            if (req.getParameter("forceContextualization") != null && req.getParameter("forceContextualization").length() > 0)
                window.setProperty("osivia.cms.forceContextualization", req.getParameter("forceContextualization"));
            else if (window.getProperty("osivia.cms.forceContextualization") != null)
                window.setProperty("osivia.cms.forceContextualization", null);

            if (req.getParameter("changeDisplayMode") != null && req.getParameter("changeDisplayMode").length() > 0)
                window.setProperty("osivia.cms.changeDisplayMode", req.getParameter("changeDisplayMode"));
            else if (window.getProperty("osivia.cms.changeDisplayMode") != null)
                window.setProperty("osivia.cms.changeDisplayMode", null);


              if (req.getParameter("displayLiveVersion") != null && req.getParameter("displayLiveVersion").length() > 0)
                window.setProperty("osivia.cms.displayLiveVersion", req.getParameter("displayLiveVersion"));
            else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
                window.setProperty("osivia.cms.displayLiveVersion", null);


            if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
                window.setProperty("osivia.cms.scope", req.getParameter("scope"));
            else if (window.getProperty("osivia.cms.scope") != null)
                window.setProperty("osivia.cms.scope", null);

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }
        
        //Modif-FILEBROWSER-begin
        if(req.getParameter("deleteFileItem") != null){
        	String itemId = (String) req.getParameter("fileItemId");
        	NuxeoController ctrl = new NuxeoController(req, res, getPortletContext());
        	try {
				ctrl.executeNuxeoCommand(new DeleteDocumentCommand(itemId));
			} catch (Exception e) {
				if (!(e instanceof PortletException))
					throw new PortletException(e);
			}
        }
        //Modif-FILEBROWSER-end
    }

    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

        res.setContentType("text/html");

        NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

        PortletRequestDispatcher rd = null;

        PortalWindow window = WindowFactory.getWindow(req);

        String nuxeoPath = window.getProperty("osivia.nuxeoPath");
        if (nuxeoPath == null)
            nuxeoPath = "";
        req.setAttribute("nuxeoPath", nuxeoPath);


        String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
        req.setAttribute("displayLiveVersion", displayLiveVersion);

        // v2.0.5
        req.setAttribute("changeDisplayMode", window.getProperty("osivia.cms.changeDisplayMode"));
        req.setAttribute("forceContextualization", window.getProperty("osivia.cms.forceContextualization"));

   
        String scope = window.getProperty("osivia.cms.scope");
        req.setAttribute("scope", scope);

        req.setAttribute("ctx", ctx);


        rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/admin.jsp");
        rd.include(req, res);

    }


    private void addPathItem(List<PortletPathItem> portletPath, Document curDoc, String displayMode, String prefixName) {
        Map<String, String> renderParams = new Hashtable<String, String>();
        renderParams.put("folderPath", curDoc.getPath());

        if (displayMode != null)
            renderParams.put("displayMode", displayMode);

        String title = curDoc.getTitle();

        if (prefixName != null) {
            title = prefixName + title;

        }

        PortletPathItem pathItem = new PortletPathItem(renderParams, title);

        portletPath.add(0, pathItem);
    }


    @SuppressWarnings("unchecked")
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {

        logger.debug("doView");

        try {

            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);


            /* On détermine l'uid et le scope */

            String nuxeoPath = null;

            // portal window parameter (appels dynamiques depuis le portail)
            nuxeoPath = window.getProperty("osivia.cms.uri");

            // logger.debug("doView "+ uid);
            if (nuxeoPath == null) {
                nuxeoPath = window.getProperty("osivia.nuxeoPath");
            }

            String displayMode = request.getParameter("displayMode");


            if (nuxeoPath != null) {
                NuxeoController ctx = new NuxeoController(request, response, getPortletContext());


                nuxeoPath = ctx.getComputedPath(nuxeoPath);


                String folderPath = nuxeoPath;

                if (request.getParameter("folderPath") != null) {
                    folderPath = request.getParameter("folderPath");
                }


                /* Folder courant */

                // Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx.executeNuxeoCommand(new DocumentFetchCommand(folderPath));
                Document doc = ctx.fetchDocument(folderPath);


                // Document doc = (Document) ctx.executeNuxeoCommand(new DocumentFetchCommand(folderPath));

                /* Récupération des fils */


                // v2.0.5 : liens contextualisés par paramétrage
                // On rend la vue cohérente avec le menu de navigation, en filtrant les folders non 'show in menu'
                //20130627 : Supprimé par JSS (vu avec Olivier)
				boolean filterByNav = false;
              if ("1".equals(window.getProperty("osivia.portletContextualizedInPage")) || "1".equals(window.getProperty("osivia.cms.forceContextualization"))) {
                    request.setAttribute("cmsLink", "1");
              }

                CMSPublicationInfos pubInfos = ctx.getCMSService().getPublicationInfos(ctx.getCMSCtx(), folderPath);
                
 
                Documents docs = (Documents) ctx.executeNuxeoCommand(new FolderGetFilesCommand(pubInfos.getDocumentPath(), pubInfos.getLiveId(), ctx
                        .isDisplayingLiveVersion()));


                // Tri pour affichage
                
                //TODO : pas de clone ????
                //List<Document> sortedDocs = (ArrayList<Document>) docs.clone();
                
                List<Document> sortedDocs = (ArrayList<Document>) docs.list();
                Collections.sort(sortedDocs, createComparator(doc));
                request.setAttribute("docs", sortedDocs);

                /* Récupération des parents (pour le path) */

                List<PortletPathItem> portletPath = new ArrayList<PortletPathItem>();


                // delete .proxy extension
                String curPath = ctx.getLivePath(doc.getPath());
                Document curDoc = doc;

                while (!curPath.equals(nuxeoPath) && curPath.startsWith(nuxeoPath)) {

                    addPathItem(portletPath, curDoc, displayMode, null);

                    curPath = ctx.getParentPath(curPath);
                    curDoc = (Document) ctx.fetchDocument(curPath);

                }

 // Les liens doivent apparitre sur tous les folders                
 //               if (curPath.equals(nuxeoPath)) {


                        List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");


                        // v2.1 WORKSPACE
                        Map<String, String> subTypes = pubInfos.getSubTypes();
                        
                        List<SubType> portalDocsToCreate = new ArrayList<SubType>();
                        Map<String, DocTypeDefinition> managedTypes = ctx.getDocTypeDefinitions();
                        for (String docType : subTypes.keySet()) {
                            
                            // is this type managed at portal level ?
                            
                            DocTypeDefinition docTypeDef =  managedTypes.get(docType);
                            
                            if( docTypeDef != null && docTypeDef.isSupportingPortalForm())  {
                            
                                SubType subType = new SubType();
                            
                                subType.setDocType(docType);
                                subType.setName(subTypes.get(docType));
                                subType.setUrl(ctx.getNuxeoPublicBaseUri() + "/nxpath/default" + curPath + "@toutatice_create?type=" + docType);
                                portalDocsToCreate.add(subType);
                             }
                            
                        }

                       
				/* Lien de création */

				String fancyID = null;
				if (portalDocsToCreate.size() > 0)
					fancyID = "_PORTAL_CREATE";

				if (fancyID != null) {

					// Force to reload portlet
					PortletURL portletURL = response.createRenderURL();
					portletURL.setParameter("reloadDatas", "" + System.currentTimeMillis());

					String divId = (String) request.getAttribute("osivia.window.ID");

					String onClick = "setCallbackParams('" + divId + "', '" + portletURL.toString() + "')";

					// Modif-FILEBROWSER-begin
					MenubarItem item = new MenubarItem("EDIT", "Ajouter ", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1,
							"#" + response.getNamespace() + fancyID, onClick,
							"fancybox_inline fancybox-no-title portlet-menuitem-nuxeo-add", "nuxeo");
					// Modif-FILEBROWSER-begin
					item.setAjaxDisabled(true);

					menuBar.add(item);

				}

				if (portalDocsToCreate.size() > 0)
					request.setAttribute("portalDocsToCreate", portalDocsToCreate);

                            // v2.1 WORKSPACE : simplification mode max
//                    String portletName = null;
//                    if (WindowState.MAXIMIZED.equals(request.getWindowState())) {
//                        portletName = "Explorateur ";
//                        addPathItem(portletPath, curDoc, displayMode, portletName);
                        
                        // Insert standard menu bar for content item
                        ctx.setCurrentDoc(doc);
                        ctx.insertContentMenuBarItems();


//                        String nuxeoFolderLink = ctx.getLink(curDoc, "file-browser-menu-workspace").getUrl();
//
//                        if (nuxeoFolderLink != null) {
//
//                            MenubarItem item = new MenubarItem("EDIT", "Ouvrir dans Nuxeo", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, nuxeoFolderLink, null,
//                                    "portlet-menuitem-nuxeo-edit", "nuxeo");
//                            item.setAjaxDisabled(true);
//                            menuBar.add(item);
//                        }
//                    }
 //               }
                
                
                
                
                


                // Injection du path vers le portail
                request.setAttribute("osivia.portletPath", portletPath);
                // response.setProperty("osivia.emptyResponse", "1");

                /* attributs de la JSP */

                request.setAttribute("portletPath", portletPath);

                request.setAttribute("basePath", nuxeoPath);
                request.setAttribute("folderPath", folderPath);

                // Pas d'affichage détaillé en mode normal
                if (DISPLAY_MODE_DETAILED.equals(displayMode) && !WindowState.MAXIMIZED.equals(request.getWindowState()))
                    displayMode = DISPLAY_MODE_NORMAL;

                request.setAttribute("displayMode", displayMode);


                request.setAttribute("changeDisplayMode", window.getProperty("osivia.cms.changeDisplayMode"));


                request.setAttribute("ctx", ctx);


                if (DISPLAY_MODE_DETAILED.equals(displayMode))
                    getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/view-detailed.jsp").include(request, response);
                else
                    getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/view-normal.jsp").include(request, response);

            } else {
                response.setContentType("text/html");
                response.getWriter().print("<h2>Document non défini</h2>");
                response.getWriter().close();
                return;
            }

        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        }

        catch (Exception e) {
            if (!(e instanceof PortletException))
                throw new PortletException(e);
        }

        logger.debug("doView end");

    }

}
