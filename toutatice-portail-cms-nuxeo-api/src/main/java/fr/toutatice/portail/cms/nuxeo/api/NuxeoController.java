package fr.toutatice.portail.cms.nuxeo.api;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.osivia.portal.core.formatters.IFormatter;
import org.osivia.portal.core.profils.IProfilManager;
import org.osivia.portal.core.profils.ProfilBean;
import org.osivia.portal.core.security.CmsPermissionHelper;
import org.osivia.portal.core.security.CmsPermissionHelper.Level;

import fr.toutatice.portail.cms.nuxeo.api.services.DocTypeDefinition;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;

public class NuxeoController {

    private static Log log = LogFactory.getLog(NuxeoController.class);



    PortletRequest request;
    PortletResponse response;
    PortletContext portletCtx;
    IPortalUrlFactory urlFactory;
    INuxeoService nuxeoCMSService;
    String pageId;
    URI nuxeoBaseURI;
    NuxeoConnectionProperties nuxeoConnection;
    IProfilManager profilManager;
    IFormatter formatter;
    String scope;
    String displayLiveVersion;
    String basePath;
    String navigationPath;
    String itemNavigationPath;

    String contentPath;
    String spacePath;

    String forcePublicationInfosScope;

    String menuRootPath;


    public String getMenuRootPath() {
        return this.menuRootPath;
    }

    String hideMetaDatas;
    String displayContext;
    String navigationScope = null;
    CMSItem navItem;
    CMSServiceCtx cmsCtx;



    //v 1.0.11 : pb. des pices jointes dans le proxy
    Document currentDoc;
    PortalControllerContext portalCtx;

    private static ICMSServiceLocator cmsServiceLocator ;

    public Document getCurrentDoc() {
        return this.currentDoc;
    }
    public void setCurrentDoc(Document currentDoc) {
        this.currentDoc = currentDoc;
    }



    public String getSpacePath() {
        return this.spacePath;
    }
    public void setSpacePath(String spacePath) {
        this.spacePath = spacePath;
    }

    public String getBasePath() {

        return this.basePath;
    }

    public String getNavigationPath() {
        return this.navigationPath;
    }

    /**
     * path reel de contenu
     * @return
     */
    public String getContentPath() {
        return this.contentPath;
    }


    /**
     * path de navigation du contenu
     * @return
     */
    public String getItemNavigationPath() {
        return this.itemNavigationPath;
    }




    public String getDisplayContext() {
        return this.displayContext;
    }

    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }



    public String getHideMetaDatas() {
        return this.hideMetaDatas;
    }

    public void setHideMetaDatas(String hideMetaDatas) {
        this.hideMetaDatas = hideMetaDatas;
    }

    String pageMarker;

    public void setPageMarker(String pageMarker) {
        this.pageMarker = pageMarker;
    }

    public String getDisplayLiveVersion() {
        return this.displayLiveVersion;
    }

    public void setDisplayLiveVersion(String displayLiveVersion) {
        this.displayLiveVersion = displayLiveVersion;
    }

    public boolean isDisplayingLiveVersion(){
        boolean fDisplayLiveVersion = false;
        if( "1".equals(this.displayLiveVersion)){
            // Il faut récupérer les proxys
            fDisplayLiveVersion = true;
        }
        return fDisplayLiveVersion;
    }

    public String getForcePublicationInfosScope() {
        return forcePublicationInfosScope;
    }

    public void setForcePublicationInfosScope(String forcePublicationInfosScope) {
        this.forcePublicationInfosScope = forcePublicationInfosScope;
    }


    public String getScope() {
        return this.scope;
    }

    int authType = NuxeoCommandContext.AUTH_TYPE_USER;
    private ProfilBean scopeProfil = null;


    int cacheType = CacheInfo.CACHE_SCOPE_NONE;


    public int getCacheType() {
        return this.cacheType;
    }

    public void setCacheType(int cacheType) {
        this.cacheType = cacheType;
    }

    private ProfilBean getScopeProfil() {
        return this.scopeProfil;
    }

    private void setScopeProfil(ProfilBean scopeProfil) {
        this.scopeProfil = scopeProfil;
    }

    INuxeoCommandService nuxeoCommandService;
    private long cacheTimeOut = -1;

    public boolean asynchronousUpdates = false;

    public boolean isAsynchronousUpdates() {
        return this.asynchronousUpdates;
    }

    public void setAsynchronousUpdates(boolean asynchronousUpdates) {
        this.asynchronousUpdates = asynchronousUpdates;
    }

    public long getCacheTimeOut() {
        return this.cacheTimeOut;
    }

    public void setCacheTimeOut(long cacheTimeOut) {
        this.cacheTimeOut = cacheTimeOut;
    }



    /**
     * Peuplement à partir de l'interface
     *
     * @param scope
     * @throws Exception
     */
    public void setScope(String scope) throws Exception {

        // Par défaut
        this.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
        this.setCacheType( CacheInfo.CACHE_SCOPE_NONE);

        if ("anonymous".equals(scope)) {
            this.setAuthType( NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
            this.setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
            /*} else if ("__nocache".equals(scope)) {
			setAuthType( NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
			setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
             */
        } else
            if( (scope != null) && !"__nocache".equals(scope)) {
                this.setAuthType( NuxeoCommandContext.AUTH_TYPE_PROFIL);
                this.setScopeProfil(this.getProfilManager().getProfil(scope));
                this.setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
            }

        this.scope = scope;


    }

    public int getAuthType() {
        return this.authType;
    }

    public void setAuthType(int authType) {
        this.authType = authType;
    }

    public NuxeoConnectionProperties getNuxeoConnectionProps() {
        if (this.nuxeoConnection == null) {
            this.nuxeoConnection = new NuxeoConnectionProperties();
        }
        return this.nuxeoConnection;
    }

    public PortalControllerContext getPortalCtx()	{

        if( this.portalCtx == null) {
            this.portalCtx = new PortalControllerContext(
                    this.getPortletCtx(), this.request, this.response);
        }

        return this.portalCtx;
    }

    public PortletRequest getRequest() {
        return this.request;
    }

    public PortletResponse getResponse() {
        return this.response;
    }

    public PortletContext getPortletCtx() {
        return this.portletCtx;
    }




    public NuxeoController(PortletRequest request, PortletResponse response, PortletContext portletCtx) throws RuntimeException  {
        super();
        this.request = request;
        this.response = response;
        this.portletCtx = portletCtx;

        try	{
            PortalWindow window = WindowFactory.getWindow(request);

            this.portalCtx = new PortalControllerContext(portletCtx, request, response);

            // v2 : Ajout héritage
            String scope = window.getProperty("osivia.cms.scope");
            if( "__inherited".equals(scope))	{
                // scope de contextualisation

                //TODO : ajouter sur le path
                scope = request.getParameter("osivia.cms.pageScope");
                if( scope == null) {
                    scope = window.getPageProperty("osivia.cms.scope");
                }
            }

            // Pour les fragments, le cache doit également concerner les PublicationInfos
            // D'où l'utilisattoin du forcePublicationScope
                String forcePublicationScope = window.getProperty("osivia.cms.forcePublicationScope");
                if (forcePublicationScope != null) {
                    // Fragments

                    if ("__inherited".equals(forcePublicationScope)) {
                        forcePublicationScope = request.getParameter("osivia.cms.pageScope");
                        if (forcePublicationScope == null)
                            forcePublicationScope = window.getPageProperty("osivia.cms.scope");
                    }

                    if (forcePublicationScope != null) {

                        scope = forcePublicationScope;
                        setForcePublicationInfosScope(forcePublicationScope);
                    }
                }
            
            this.navigationScope = window.getPageProperty("osivia.cms.navigationScope");


            this.spacePath = window.getPageProperty("osivia.cms.basePath");
            CMSItem publishSpaceConfig = null;
            if( this.spacePath != null) {
                publishSpaceConfig = getCMSService().getSpaceConfig(this.getCMSCtx(), this.spacePath);
            }


            String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");


            if( "__inherited".equals(displayLiveVersion))	{

                if( publishSpaceConfig != null) {
                    displayLiveVersion = publishSpaceConfig.getProperties().get("displayLiveVersion");
                } else {
                    displayLiveVersion = window.getPageProperty("osivia.cms.displayLiveVersion");
                }
            }


            String displayLiveVersionParam = request.getParameter("displayLiveVersion");
            if (displayLiveVersionParam != null) {
                displayLiveVersion = displayLiveVersionParam;
            }



            String hideMetadatas = window.getProperty("osivia.cms.hideMetaDatas");

            this.setScope(scope);
            this.setDisplayLiveVersion(displayLiveVersion);
            this.setHideMetaDatas(hideMetadatas);

            this.setPageMarker((String) request.getAttribute("osivia.pageMarker"));


            /* Calcul du root path */
            Window jbpWindow = (Window) request.getAttribute("osivia.window");
            Page page = (Page) jbpWindow.getParent();
            Portal portal = page.getPortal();
            if( InternalConstants.PORTAL_TYPE_SPACE.equals(portal.getDeclaredProperty("osivia.portal.portalType")))   {
                this.menuRootPath = portal.getDefaultPage().getDeclaredProperty("osivia.cms.basePath");
            }


            this.basePath = window.getPageProperty("osivia.cms.basePath");

            this.navigationPath =  request.getParameter("osivia.cms.path");

            if	((this.spacePath != null) && (request.getParameter("osivia.cms.itemRelPath") != null)) {
                this.itemNavigationPath = this.spacePath + request.getParameter("osivia.cms.itemRelPath");
            }

            this.contentPath = request.getParameter("osivia.cms.contentPath");

        } catch( Exception e)	{
            throw new RuntimeException( e);
        }
    }





    public CMSItem getNavigationItem()	throws Exception {
        if( this.navItem == null){
            if( this.getNavigationPath() != null){
                // Navigation context
                CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
                cmsReadNavContext.setControllerContext(ControllerContextAdapter.getControllerContext(this.getPortalCtx()));
                cmsReadNavContext.setScope(this.getNavigationScope());

                //TODO : factoriser dans NuxeoController

                INuxeoService nuxeoService = (INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
                this.navItem = getCMSService().getPortalNavigationItem(cmsReadNavContext,  this.getSpacePath(), this.getNavigationPath());
            }

        }

        return this.navItem;
    }



    public String getNavigationScope() {
        return this.navigationScope;
    }
    public NuxeoController(PortletContext portletCtx) {
        super();
        this.portletCtx = portletCtx;
    }




    public IPortalUrlFactory getPortalUrlFactory( ) throws Exception{
        if (this.urlFactory == null) {
            this.urlFactory = (IPortalUrlFactory) this.portletCtx.getAttribute("UrlService");
        }

        return this.urlFactory;
    }

    public INuxeoCommandService getNuxeoCommandService() throws Exception {
        if (this.nuxeoCommandService == null) {
            this.nuxeoCommandService = NuxeoCommandServiceFactory.getNuxeoCommandService(this.portletCtx);
        }
        return this.nuxeoCommandService;
    }

    public IProfilManager getProfilManager() throws Exception {
        if (this.profilManager == null) {
            this.profilManager = (IProfilManager) this.portletCtx.getAttribute(Constants.PROFILE_SERVICE_NAME);
        }


        return this.profilManager;
    }

    public IFormatter getFormatter() throws Exception {
        if (this.formatter == null) {
            this.formatter = (IFormatter) this.portletCtx.getAttribute("FormatterService");
        }


        return this.formatter;
    }




    public INuxeoService getNuxeoCMSService()		{
        if( this.nuxeoCMSService == null) {
            this.nuxeoCMSService = (INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
        }
        return this.nuxeoCMSService;
    }


    public String getPageId() {
        if (this.pageId == null) {
            Window window = (Window) this.request.getAttribute("osivia.window");
            Page page = (Page) window.getParent();
            try {
                this.pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return this.pageId;
    }


    public String getComputedPath( String portletPath){

        String computedPath = null;

        if (portletPath == null) {
            computedPath = "";
        } else {
            computedPath = portletPath;

            if (computedPath.contains("${basePath}")) {
                String path = this.getBasePath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{basePath\\}", path);
            }

            //v2.0.5
            if (computedPath.contains("${spacePath}")) {
                String path = this.getSpacePath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{spacePath\\}", path);
            }


            if (computedPath.contains("${navigationPath}")) {

                String path = this.getNavigationPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{navigationPath\\}", path);
            }

            if (computedPath.contains("${contentPath}")) {

                String path = this.getContentPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{contentPath\\}", path);
            }

            if (computedPath.contains("${sitePath}")) {
                String path = this.getMenuRootPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{sitePath\\}", path);
            }


        }

        return computedPath;

    }


    public String transformHTMLContent(String htmlContent) throws Exception {


        // Adaptation via le CMSCustomizer

        INuxeoService nuxeoService =(INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
        if( nuxeoService == null) {
            nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        }


       return  nuxeoService.getCMSCustomizer().transformHTMLContent(this.getCMSCtx(), htmlContent);



    }

    public String formatScopeList(String selectedScope) throws Exception {

        Window window = (Window) this.request.getAttribute("osivia.window");

        return this.getFormatter().formatScopeList(window, "scope", selectedScope);

    }


    public String formatRequestFilteringPolicyList(String selectedRequestFilteringPolicy) throws Exception {

        Window window = (Window) this.request.getAttribute("osivia.window");

        return this.getFormatter().formatRequestFilteringPolicyList(window, "requestFilteringPolicy", selectedRequestFilteringPolicy);

    }

    public String formatDisplayLiveVersionList(String selectedVersion) throws Exception {

        Window window = (Window) this.request.getAttribute("osivia.window");

        return this.getFormatter().formatDisplayLiveVersionList(this.getCMSCtx(), window, "displayLiveVersion", selectedVersion);

    }

    private ResourceURL createResourceURL()	{
        if( this.response instanceof RenderResponse) {
            return( (RenderResponse) this.response).createResourceURL();
        } else if( this.response instanceof ResourceResponse) {
            return( (ResourceResponse) this.response).createResourceURL();
        }
        return null;
    }

    public String createFileLink(Document doc, String fieldName)  throws Exception {

        if( "ttc:vignette".equals(fieldName))	 {
            String url = this.getRequest().getContextPath() + "/thumbnail?" + "path=" + URLEncoder.encode(doc.getPath(), "UTF-8") ;
            return url;

        }	else	{

            ResourceURL resourceURL =  this.createResourceURL();
            resourceURL.setResourceID(doc.getId() + "/" + fieldName);
            resourceURL.setParameter("type", "file");
            resourceURL.setParameter("docPath", doc.getPath());
            resourceURL.setParameter("fieldName", fieldName);

            //v1.0.19 :
            if( this.isDisplayingLiveVersion())	{
                resourceURL.setParameter("displayLiveVersion", "1");

            }

            // ne marche pas : bug JBP
            // resourceURL.setCacheability(ResourceURL.PORTLET);
            resourceURL.setCacheability(ResourceURL.PAGE);

            return resourceURL.toString();
        }
    }

    public String createExternalLink(Document doc) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(doc.getId());
        resourceURL.setParameter("type", "link");
        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);

        return resourceURL.toString();
    }

    public String createAttachedFileLink(String path, String fileIndex) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + fileIndex);

        resourceURL.setParameter("type", "attachedFile");
        resourceURL.setParameter("fileIndex", fileIndex);
        resourceURL.setParameter("docPath", path);

        if (this.isDisplayingLiveVersion()) {
            resourceURL.setParameter("displayLiveVersion", "1");
        }

        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }

    //v1.0.27
    public String createAttachedBlobLink(String path, String blobIndex) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + blobIndex);

        resourceURL.setParameter("type", "blob");
        resourceURL.setParameter("blobIndex", blobIndex);
        resourceURL.setParameter("docPath", path);

        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }

    public String createAttachedPictureLink(String path, String fileIndex) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + fileIndex);

        resourceURL.setParameter("type", "attachedPicture");
        resourceURL.setParameter("pictureIndex", fileIndex);
        resourceURL.setParameter("docPath", path);

        if( this.isDisplayingLiveVersion())	{
            resourceURL.setParameter("displayLiveVersion", "1");
        }

        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }

    public String createPictureLink(String path, String content) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + content);

        resourceURL.setParameter("type", "picture");
        resourceURL.setParameter("content", content);
        resourceURL.setParameter("docPath", path);

        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }


    public String createPermalink(String path) throws Exception {

        String permaLinkURL = this.getPortalUrlFactory().getPermaLink(this.getPortalCtx(), null, null,
                path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
        return permaLinkURL;
    }

    public URI getNuxeoPublicBaseUri() {
        if (this.nuxeoBaseURI == null) {
            this.nuxeoBaseURI = NuxeoConnectionProperties.getPublicBaseUri();
        }

        return this.nuxeoBaseURI;
    }

    public void handleErrors(NuxeoException e) throws Exception {
        if( this.response instanceof RenderResponse) {
            PortletErrorHandler.handleGenericErrors((RenderResponse) this.response, e);
        }
    }

    public Object executeNuxeoCommand(final INuxeoCommand command) throws Exception {

        NuxeoCommandContext ctx = new NuxeoCommandContext(this.portletCtx, this.request);

        ctx.setAuthType(this.getAuthType());
        ctx.setAuthProfil(this.getScopeProfil());
        ctx.setCacheTimeOut(this.cacheTimeOut);
        ctx.setCacheType(this.cacheType);
        ctx.setAsynchronousUpdates(this.asynchronousUpdates);

        try {

       return  this.getNuxeoCommandService().executeCommand(ctx, new INuxeoServiceCommand() {

            public String getId() {
                return command.getId();
            }

            public Object execute(Session nuxeoSession) throws Exception {
                return command.execute(nuxeoSession);
            }
        }); }

            catch( CMSException e){
                if( e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                    throw new NuxeoException( NuxeoException.ERROR_NOTFOUND);
                }
                if( e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                    throw new NuxeoException( NuxeoException.ERROR_FORBIDDEN);
                }
                throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);

            }

    }



    public void startNuxeoService() throws Exception {
        NuxeoCommandServiceFactory.startNuxeoCommandService(this.getPortletCtx());
    }

    public void stopNuxeoService() throws Exception {
        NuxeoCommandServiceFactory.stopNuxeoCommandService(this.getPortletCtx());
    }

    public Link getLink(Document doc) throws Exception 	{

        return this.getLink(doc, null);

    }

    public Link getLink(Document doc, String displayContext) throws Exception	{
        return this.getLink( doc,  displayContext, null);
    }






    public Link getCMSLinkByPath (String path, String displayContext) throws Exception  {


        Window window = (Window) this.getPortalCtx().getRequest().getAttribute("osivia.window");
        Page page = window.getPage();

        Map<String, String> pageParams = new HashMap<String, String>();


        String url = this.getPortalUrlFactory().getCMSUrl(this.portalCtx,
                page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, pageParams, null, displayContext, null, null, null, null);

        if( url != null)	{

            Link link = new Link(url, false);
            return link;
        }

        return null;
    }




    public Link getLink(Document doc, String displayContext, String linkContextualization) throws Exception 	{


        String localContextualization = linkContextualization;


        INuxeoService nuxeoService =(INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
        if( nuxeoService == null) {
            nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        }


        CMSServiceCtx handlerCtx = new  CMSServiceCtx();
        handlerCtx.setControllerContext(ControllerContextAdapter.getControllerContext(new PortalControllerContext(this.getPortletCtx(),
                this.getRequest(),this.getResponse())));
        handlerCtx.setPortletCtx(this.getPortletCtx());
        handlerCtx.setRequest(this.getRequest());
        if( this.response instanceof RenderResponse) {
            handlerCtx.setResponse( (RenderResponse)this.response);
        }
        handlerCtx.setScope(this.getScope());
        handlerCtx.setDisplayLiveVersion(this.getDisplayLiveVersion());
        handlerCtx.setPageId(this.getPageId());
        handlerCtx.setDoc(doc);
        handlerCtx.setHideMetaDatas(this.getHideMetaDatas());
        handlerCtx.setDisplayContext(displayContext);


        // On regarde si le lien est géré par le portlet

        Link portletLink = nuxeoService.getCMSCustomizer().createCustomLink(handlerCtx);
        if( portletLink != null) {
            return portletLink;
        }



        // Sinon on passe par le gestionnaire de cms pour recontextualiser

        Window window = (Window) this.getPortalCtx().getRequest().getAttribute("osivia.window");
        Page page = window.getPage();

        Map<String, String> pageParams = new HashMap<String, String>();


        //v2.0-rc7 : suppression du scope
        //		String url = getPortalUrlFactory().getCMSUrl(portalCtx,
        //				page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), doc.getPath(), pageParams, localContextualization, displayContext, getHideMetaDatas(), getScope(), getDisplayLiveVersion(), null);

        String url = this.getPortalUrlFactory().getCMSUrl(this.portalCtx,
                page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), doc.getPath(), pageParams, localContextualization, displayContext, this.getHideMetaDatas(), null, this.getDisplayLiveVersion(), null);


        if( url != null)	{

            Link link = new Link(url, false);
            return link;
        }

        return null;

    }


    public void insertContentMenuBarItems 	() throws Exception	{


        // Adaptation via le CMSCustomizer

        INuxeoService nuxeoService =(INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
        if( nuxeoService == null) {
            nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        }

        List<MenubarItem> menuBar = (List<MenubarItem>) this.request.getAttribute("osivia.menuBar");

        nuxeoService.getCMSCustomizer().formatContentMenuBar(this.getCMSCtx());
    }


     public Map<String, DocTypeDefinition> getDocTypeDefinitions  () throws Exception {

        // Adaptation via le CMSCustomizer

        INuxeoService nuxeoService =(INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
        if( nuxeoService == null) {
            nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        }


        return nuxeoService.getCMSCustomizer().getDocTypeDefinitions(this.getCMSCtx());
    }



     




    public Document fetchDocument(String path, boolean reload) throws Exception {



        try	{
            CMSServiceCtx cmsCtx = this.getCMSCtx();
            // Prévisualisation des portlets définis au niveau du template
            if (path.equals(getNavigationPath())) {
                if (CmsPermissionHelper.getCurrentPageSecurityLevel(cmsCtx.getControllerContext(), path) == Level.allowPreviewVersion) {
                    cmsCtx.setDisplayLiveVersion("1");
                }

            }

            if (reload) {
                cmsCtx.setForceReload(true);
            }


            CMSItem cmsItem = getCMSService().getContent(cmsCtx, path);
            return (Document) cmsItem.getNativeItem();

        } catch( CMSException e){
            if( e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                throw new NuxeoException( NuxeoException.ERROR_NOTFOUND);
            }
            if( e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                throw new NuxeoException( NuxeoException.ERROR_FORBIDDEN);
            }
            throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);

        }
    }


    public Document fetchDocument(String path) throws Exception {
        return this.fetchDocument(path, false);
    }

    public String fetchLiveId 	( String path) throws Exception	{

        try	{




            CMSPublicationInfos pubInfos  = getCMSService().getPublicationInfos(this.getCMSCtx(), path);
            return pubInfos.getLiveId();

        } catch( CMSException e){
            if( e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                throw new NuxeoException( NuxeoException.ERROR_NOTFOUND);
            }
            if( e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                throw new NuxeoException( NuxeoException.ERROR_FORBIDDEN);
            }
            throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);

        }
    }


    public String getParentPath 	( String path) throws Exception	{
        // One level up
        CMSObjectPath parentPath = CMSObjectPath.parse(path).getParent();
        return parentPath.toString();
    }


    public String  getLivePath(String path){
        String result = path;
        if( path.endsWith(".proxy")) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }





    public CMSBinaryContent fetchAttachedPicture(String docPath, String pictureIndex) {
        try	{


            return getCMSService().getBinaryContent(this.getCMSCtx(), "attachedPicture", docPath, pictureIndex);

        } catch( CMSException e){
            if( e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                throw new NuxeoException( NuxeoException.ERROR_NOTFOUND);
            }
            if( e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                throw new NuxeoException( NuxeoException.ERROR_FORBIDDEN);
            }
            throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);

        }
    }

    public CMSBinaryContent fetchPicture(String docPath, String content) {
        try {


            return getCMSService().getBinaryContent(this.getCMSCtx(), "picture", docPath, content);

        } catch (CMSException e) {
            if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }
            if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
            }
            throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);

        }
    }

    public CMSBinaryContent fetchFileContent(String docPath, String fieldName) {
        try {


            return getCMSService().getBinaryContent(this.getCMSCtx(), "file", docPath, fieldName);

        } catch (CMSException e) {
            if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }
            if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
            }
            throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);

        }
    }




    public static ICMSService getCMSService()  {

        if( cmsServiceLocator == null){
            cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
        }

        return cmsServiceLocator.getCMSService();

    }


    public CMSServiceCtx getCMSCtx()	{

        this.cmsCtx = new  CMSServiceCtx();

        this.cmsCtx.setControllerContext(ControllerContextAdapter.getControllerContext(new PortalControllerContext(this.getPortletCtx(),
                this.getRequest(),this.getResponse())));


        this.cmsCtx.setPortletCtx(this.getPortletCtx());
        this.cmsCtx.setRequest(this.getRequest());
        if( this.response instanceof RenderResponse) {
            this.cmsCtx.setResponse( (RenderResponse)this.response);
        }
        this.cmsCtx.setScope(this.getScope());
        this.cmsCtx.setForcePublicationInfosScope(getForcePublicationInfosScope());
        
        this.cmsCtx.setDisplayLiveVersion(this.getDisplayLiveVersion());
        this.cmsCtx.setPageId(this.getPageId());
        this.cmsCtx.setDoc(this.getCurrentDoc());
        this.cmsCtx.setHideMetaDatas(this.getHideMetaDatas());
        this.cmsCtx.setDisplayContext(this.displayContext);


        return this.cmsCtx;
    }


    public Map<String, String> getDocumentConfiguration(Document doc) throws Exception{

        // Adaptation via le CMSCustomizer

        INuxeoService nuxeoService =(INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
        if( nuxeoService == null) {
            nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        }


        return nuxeoService.getCMSCustomizer().getDocumentConfiguration(this.getCMSCtx(), doc);

    }

    public String getDebugInfos()	{
        String output = "";

        if ("1".equals(System.getProperty("nuxeo.debugHtml"))) {

            output += "<p class=\"nuxeo-debug\" align=\"center\">";
            output += "scope : " + this.getScope();
            output += "</p>";

            return output;
        } else	{

            output += "<!--";
            output += "scope : " + this.getScope();
            output += "-->";
        }

        return output;

    }

}
