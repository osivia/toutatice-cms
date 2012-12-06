package fr.toutatice.portail.cms.nuxeo.api;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.api.cache.services.CacheInfo;
import fr.toutatice.portail.api.cache.services.ICacheService;
import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.locator.Locator;
import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;


import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSObjectPath;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.formatters.IFormatter;
import fr.toutatice.portail.core.nuxeo.INuxeoService;

import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;
import fr.toutatice.portail.core.profils.IProfilManager;
import fr.toutatice.portail.core.profils.ProfilBean;

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
	String contentPath;
	String spacePath;
	

	String hideMetaDatas;
	String displayContext;
	String navigationScope = null;
	CMSItem navItem;
	CMSServiceCtx cmsCtx;
	
	//v 1.0.11 : pb. des pices jointes dans le proxy
	Document currentDoc;
	
	
	public Document getCurrentDoc() {
		return currentDoc;
	}
	public void setCurrentDoc(Document currentDoc) {
		this.currentDoc = currentDoc;
	}
	PortalControllerContext portalCtx;	
	

	public String getSpacePath() {
		return spacePath;
	}
	public void setSpacePath(String spacePath) {
		this.spacePath = spacePath;
	}

	public String getBasePath() {
		
		return basePath;
	}

	public String getNavigationPath() {
		return navigationPath;
	}

	public String getContentPath() {
		return contentPath;
	}



	

	
	public String getDisplayContext() {
		return displayContext;
	}

	public void setDisplayContext(String displayContext) {
		this.displayContext = displayContext;
	}



	public String getHideMetaDatas() {
		return hideMetaDatas;
	}

	public void setHideMetaDatas(String hideMetaDatas) {
		this.hideMetaDatas = hideMetaDatas;
	}

	String pageMarker;
	
	public void setPageMarker(String pageMarker) {
		this.pageMarker = pageMarker;
	}

	public String getDisplayLiveVersion() {
		return displayLiveVersion;
	}

	public void setDisplayLiveVersion(String displayLiveVersion) {
		this.displayLiveVersion = displayLiveVersion;
	}
	
	public boolean isDisplayingLiveVersion(){
		boolean fDisplayLiveVersion = false;
		if( "1".equals(displayLiveVersion)){
		// Il faut récupérer les proxys
		fDisplayLiveVersion = true;
	} 
	return fDisplayLiveVersion;
	}

	

	public String getScope() {
		return scope;
	}

	int authType = NuxeoCommandContext.AUTH_TYPE_USER;	
	private ProfilBean scopeProfil = null;


	int cacheType = CacheInfo.CACHE_SCOPE_NONE;

	
	public int getCacheType() {
		return cacheType;
	}

	public void setCacheType(int cacheType) {
		this.cacheType = cacheType;
	}

	private ProfilBean getScopeProfil() {
		return scopeProfil;
	}

	private void setScopeProfil(ProfilBean scopeProfil) {
		this.scopeProfil = scopeProfil;
	}

	INuxeoCommandService nuxeoService;
	private long cacheTimeOut = -1;

	public boolean asynchronousUpdates = false;

	public boolean isAsynchronousUpdates() {
		return asynchronousUpdates;
	}

	public void setAsynchronousUpdates(boolean asynchronousUpdates) {
		this.asynchronousUpdates = asynchronousUpdates;
	}

	public long getCacheTimeOut() {
		return cacheTimeOut;
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
		setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
		setCacheType( CacheInfo.CACHE_SCOPE_NONE);
		
		if ("anonymous".equals(scope)) {
			setAuthType( NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
			setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
		/*} else if ("__nocache".equals(scope)) {
			setAuthType( NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
			setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
			*/
		} else 
			if( scope != null && !"__nocache".equals(scope)) {
			setAuthType( NuxeoCommandContext.AUTH_TYPE_PROFIL);
			setScopeProfil(getProfilManager().getProfil(scope));
			setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);			
		}
		
		this.scope = scope;

	
	}
	
	public int getAuthType() {
		return authType;
	}

	public void setAuthType(int authType) {
		this.authType = authType;
	}	

	public NuxeoConnectionProperties getNuxeoConnectionProps() {
		if (nuxeoConnection == null)
			nuxeoConnection = new NuxeoConnectionProperties();
		return nuxeoConnection;
	}

	public PortalControllerContext getPortalCtx()	{

		if( portalCtx == null)
			portalCtx = new PortalControllerContext(
			 getPortletCtx(), request, response);
		
		return portalCtx;
	}
	
	public PortletRequest getRequest() {
		return request;
	}

	public PortletResponse getResponse() {
		return response;
	}

	public PortletContext getPortletCtx() {
		return portletCtx;
	}

	public NuxeoController(PortletRequest request, PortletResponse response, PortletContext portletCtx) throws RuntimeException  {
		super();
		this.request = request;
		this.response = response;
		this.portletCtx = portletCtx;
		
		try	{
		PortalWindow window = WindowFactory.getWindow(request);
		
		portalCtx = new PortalControllerContext(portletCtx, request, response);
		
		// v2 : Ajout héritage
		String scope = window.getProperty("pia.cms.scope");
		if( "__inherited".equals(scope))	{
			// scope de contextualisation
			scope = request.getParameter("pia.cms.pageScope");
			if( scope == null)
				scope = window.getPageProperty("pia.cms.scope");
		}
		
		navigationScope = window.getPageProperty("pia.cms.navigationScope");
		
		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		String hideMetadatas = window.getProperty("pia.cms.hideMetaDatas");

		setScope(scope);
		setDisplayLiveVersion(displayLiveVersion);
		setHideMetaDatas(hideMetadatas);
		
		setPageMarker((String) request.getAttribute("pia.pageMarker"));
		
		spacePath = window.getPageProperty("pia.cms.basePath");
		
		
		Window jbpWindow = (Window) request.getAttribute("pia.window");
		Page page = (Page) jbpWindow.getParent();
		

		if( "cms".equals(page.getProperty("pia.navigationMode")))	{
			
			// En navigation CMS, on descend d'un niveau par rapport à l'espace pour déterminer le path de la page
			basePath = request.getParameter("pia.cms.path");
			if( basePath != null)	{
			CMSObjectPath parent = CMSObjectPath.parse(basePath).getParent();
			String parentPath= parent.toString();

			while ( parentPath.contains(spacePath) && !(parentPath.equals(spacePath)))	{
				  
				 basePath = parentPath.toString();
				 
				 parent = CMSObjectPath.parse(basePath).getParent();
				 parentPath= parent.toString();
				 
			 }
			}
		}
		else
			basePath = window.getPageProperty("pia.cms.basePath");
		
		
		navigationPath =  request.getParameter("pia.cms.path");
		if	(spacePath != null && request.getParameter("pia.cms.itemRelPath") != null)
			contentPath = spacePath + request.getParameter("pia.cms.itemRelPath");
		
		} catch( Exception e)	{
			throw new RuntimeException( e);
		}
	}
	
	
	
	
	public CMSItem getNavigationItem()	throws Exception {
		if( navItem == null){
			if( getNavigationPath() != null){
				// Navigation context
				CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
				cmsReadNavContext.setControllerContext(getPortalCtx().getControllerCtx());
				cmsReadNavContext.setScope(getNavigationScope());		
				
				//TODO : factoriser dans NuxeoController

				INuxeoService nuxeoService = (INuxeoService) getPortletCtx().getAttribute("NuxeoService");
				navItem = nuxeoService.getPortalNavigationItem(cmsReadNavContext,  getSpacePath(), getNavigationPath());
			}
				
		}
		
		return navItem;
	}
	
	

	public String getNavigationScope() {
		return navigationScope;
	}
	public NuxeoController(PortletContext portletCtx) {
		super();
		this.portletCtx = portletCtx;
	}
	
	
	

	public IPortalUrlFactory getPortalUrlFactory( ) throws Exception{
		if (urlFactory == null)
			urlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");
		
		return urlFactory;
	}

	public INuxeoCommandService getNuxeoService() throws Exception {
		if (nuxeoService == null)
			nuxeoService = (INuxeoCommandService) NuxeoCommandServiceFactory.getNuxeoCommandService(getPortletCtx());
		return nuxeoService;
	}

	public IProfilManager getProfilManager() throws Exception {
		if (profilManager == null)
			 profilManager = (IProfilManager) portletCtx.getAttribute("ProfilService");
		

		return profilManager;
	}
	
	public IFormatter getFormatter() throws Exception {
		if (formatter == null)
			formatter = (IFormatter) portletCtx.getAttribute("FormatterService");
		

		return formatter;
	}
	
	
	

	public INuxeoService getNuxeoCMSService()		{
		if( nuxeoCMSService == null)
			nuxeoCMSService = (INuxeoService) getPortletCtx().getAttribute("NuxeoService");
		return nuxeoCMSService;
	}
	

	public String getPageId() {
		if (pageId == null) {
			Window window = (Window) request.getAttribute("pia.window");
			Page page = (Page) window.getParent();
			try {
				pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return pageId;
	}
	
	
	public String getComputedPath( String portletPath){
		
		String computedPath = null;
		
		if (portletPath == null) {
			computedPath = "";
		} else {
			computedPath = portletPath;

			if (computedPath.contains("${basePath}")) {
				String path = getBasePath();
				if (path == null)
					path = "";

				computedPath = computedPath.replaceAll("\\$\\{basePath\\}", path);
			}

			if (computedPath.contains("${navigationPath}")) {

				String path = getNavigationPath();
				if (path == null)
					path = "";

				computedPath = computedPath.replaceAll("\\$\\{navigationPath\\}", path);
			}

			if (computedPath.contains("${contentPath}")) {

				String path = getContentPath();
				if (path == null)
					path = "";

				computedPath = computedPath.replaceAll("\\$\\{contentPath\\}", path);
			}
		}

		return computedPath;

	}
	

	public String transformHTMLContent(String htmlContent) throws Exception {

		Transformer transformer = WysiwygParser.getInstance().getTemplate().newTransformer();

		transformer.setParameter("bridge", new XSLFunctions(this));
		OutputStream output = new ByteArrayOutputStream();
		XMLReader parser = WysiwygParser.getInstance().getParser();
		transformer.transform(new SAXSource(parser, new InputSource(new StringReader(htmlContent))), new StreamResult(
				output));

		return output.toString();

	}

	public String formatScopeList(String selectedScope) throws Exception {
		
		Window window = (Window) request.getAttribute("pia.window");

		return getFormatter().formatScopeList(window, "scope", selectedScope);

	}

	private ResourceURL createResourceURL()	{
		if( response instanceof RenderResponse)
			return( (RenderResponse) response).createResourceURL();
		else if( response instanceof ResourceResponse)
			return( (ResourceResponse) response).createResourceURL();
		return null;
	}

	public String createFileLink(Document doc, String fieldName)  throws Exception {
		
		if( "ttc:vignette".equals(fieldName))	 {
			String url = getRequest().getContextPath() + "/thumbnail?" + "path=" + URLEncoder.encode(doc.getPath(), "UTF-8") ;
			return url;
			
		}	else	{

		ResourceURL resourceURL =  createResourceURL();
		resourceURL.setResourceID(doc.getId() + "/" + fieldName);
		resourceURL.setParameter("type", "file");
		resourceURL.setParameter("docPath", doc.getPath());
		resourceURL.setParameter("fieldName", fieldName);
		
		//v1.0.19 : 
		if( isDisplayingLiveVersion())	{
			resourceURL.setParameter("displayLiveVersion", "1");

		}
		
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
		}
	}
	
	public String createExternalLink(Document doc) {

		ResourceURL resourceURL = createResourceURL();
		resourceURL.setResourceID(doc.getId());
		resourceURL.setParameter("type", "link");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);

		return resourceURL.toString();
	}

	public String createAttachedFileLink(String path, String fileIndex) {

		ResourceURL resourceURL = createResourceURL();
		resourceURL.setResourceID(path + "/" + fileIndex);

		resourceURL.setParameter("type", "attachedFile");
		resourceURL.setParameter("fileIndex", fileIndex);
		resourceURL.setParameter("docPath", path);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	
	//v1.0.27
	public String createAttachedBlobLink(String path, String blobIndex) {

		ResourceURL resourceURL = createResourceURL();
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

		ResourceURL resourceURL = createResourceURL();
		resourceURL.setResourceID(path + "/" + fileIndex);

		resourceURL.setParameter("type", "attachedPicture");
		resourceURL.setParameter("pictureIndex", fileIndex);
		resourceURL.setParameter("docPath", path);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	
	public String createPictureLink(String path, String content) {

		ResourceURL resourceURL = createResourceURL();
		resourceURL.setResourceID(path + "/" + content);

		resourceURL.setParameter("type", "picture");
		resourceURL.setParameter("content", content);
		resourceURL.setParameter("docPath", path);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	
	/*
	public String createRedirectDocumentLink(String path) {

		// On ne peut se permettre de lire tous les docs référencés en lien hyper-texte
		// Il est préférable de faire une redirection
		
		ResourceURL resourceURL = createResourceURL();
		resourceURL.setResourceID(path );


		resourceURL.setParameter("type", "documentLink");
		resourceURL.setParameter("docPath", path);
		if( pageMarker != null)
			resourceURL.setParameter("pageMarker", pageMarker);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	*/

	public String createPermalink(String path) throws Exception {
		
		String permaLinkURL = getPortalUrlFactory().getPermaLink(getPortalCtx(), null, null,
			path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
		return permaLinkURL;
	}

	public URI getNuxeoPublicBaseUri() {
		if (nuxeoBaseURI == null) {
			nuxeoBaseURI = NuxeoConnectionProperties.getPublicBaseUri();
		}

		return nuxeoBaseURI;
	}

	public void handleErrors(NuxeoException e) throws Exception {
		if( response instanceof RenderResponse)
			PortletErrorHandler.handleGenericErrors((RenderResponse) response, e);
	}

	public Object executeNuxeoCommand(INuxeoCommand command) throws Exception {

		NuxeoCommandContext ctx = new NuxeoCommandContext(portletCtx, request);

		ctx.setAuthType(getAuthType());
		ctx.setAuthProfil(getScopeProfil());
		ctx.setCacheTimeOut(cacheTimeOut);
		ctx.setCacheType(cacheType);
		ctx.setAsynchronousUpdates(asynchronousUpdates);

		return getNuxeoService().executeCommand(ctx, command);
	}
	


	public void startNuxeoService() throws Exception {
		NuxeoCommandServiceFactory.startNuxeoCommandService(getPortletCtx());
	}

	public void stopNuxeoService() throws Exception {
		NuxeoCommandServiceFactory.stopNuxeoCommandService(getPortletCtx());
	}
	
	public Link getLink(Document doc) throws Exception 	{
		
		return getLink(doc, null);

	}	
	
	public Link getLink(Document doc, String displayContext) throws Exception	{
		return getLink( doc,  displayContext, null);
	}
	
	
	
	
	
	
	public Link getCMSLinkByPath (String path, String displayContext) throws Exception  {
		
		
		Window window = (Window) getPortalCtx().getRequest().getAttribute("pia.window");
		Page page = window.getPage();

		Map<String, String> pageParams = new HashMap<String, String>();

		
		String url = getPortalUrlFactory().getCMSUrl(portalCtx,
				page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, pageParams, null, displayContext, null, null, null, null);
		
		if( url != null)	{
			
			Link link = new Link(url, false);
			return link;
			}
			
			return null;
	}
	
	
	public Link getLink(Document doc, String displayContext, String linkContextualization) throws Exception 	{

		
		String localContextualization = linkContextualization;

		
		INuxeoService nuxeoService =(INuxeoService) getPortletCtx().getAttribute("NuxeoService");
		if( nuxeoService == null)
			nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");
		
		
		CMSServiceCtx handlerCtx = new  CMSServiceCtx();
		handlerCtx.setControllerContext(new PortalControllerContext(getPortletCtx(),
				getRequest(),getResponse()).getControllerCtx());
		handlerCtx.setPortletCtx(getPortletCtx());
		handlerCtx.setRequest(getRequest());
		if( response instanceof RenderResponse)
			handlerCtx.setResponse( (RenderResponse)response);
		handlerCtx.setScope(getScope());
		handlerCtx.setDisplayLiveVersion(getDisplayLiveVersion());
		handlerCtx.setPageId(getPageId());
		handlerCtx.setDoc(doc);
		handlerCtx.setHideMetaDatas(getHideMetaDatas());
		handlerCtx.setDisplayContext(displayContext);

		
		// On regarde si le lien est géré par le portlet
		
		Link portletLink = nuxeoService.getCMSCustomizer().createCustomLink(handlerCtx);
		if( portletLink != null)
			return portletLink;
		
		
		
		// Sinon on passe par le gestionnaire de cms pour recontextualiser
		
		Window window = (Window) getPortalCtx().getRequest().getAttribute("pia.window");
		Page page = window.getPage();

		Map<String, String> pageParams = new HashMap<String, String>();
		
		String url = getPortalUrlFactory().getCMSUrl(portalCtx,
				page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), doc.getPath(), pageParams, localContextualization, displayContext, getHideMetaDatas(), getScope(), getDisplayLiveVersion(), null);
		
		if( url != null)	{
		
		Link link = new Link(url, false);
		return link;
		}
		
		return null;

	}	
	
	
	public void insertContentMenuBarItems 	() throws Exception	{
		
		
		// Adaptation via le CMSCustomizer
		
		INuxeoService nuxeoService =(INuxeoService) getPortletCtx().getAttribute("NuxeoService");
		if( nuxeoService == null)
			nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");
		
		
		List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("pia.menuBar");		

		nuxeoService.getCMSCustomizer().formatContentMenuBar(getCMSCtx());
		
	}
	
	public Document fetchDocument 	( String path) throws Exception	{

		try	{
		
		INuxeoService nuxeoService =(INuxeoService) getPortletCtx().getAttribute("NuxeoService");
		if( nuxeoService == null)
			nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");
		
		
		CMSItem cmsItem = nuxeoService.getContent(getCMSCtx(), path);
		return (Document) cmsItem.getNativeItem();
		
		} catch( CMSException e){
			if( e.getErrorCode() == CMSException.ERROR_NOTFOUND)
				throw new NuxeoException( NuxeoException.ERROR_NOTFOUND);
			if( e.getErrorCode() == CMSException.ERROR_FORBIDDEN)
				throw new NuxeoException( NuxeoException.ERROR_FORBIDDEN);
			throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);
			
		}
	}

	
	public CMSServiceCtx getCMSCtx()	{

		cmsCtx = new  CMSServiceCtx();
		
		cmsCtx.setControllerContext(new PortalControllerContext(getPortletCtx(),
				getRequest(),getResponse()).getControllerCtx());
		
		
		cmsCtx.setPortletCtx(getPortletCtx());
		cmsCtx.setRequest(getRequest());
		if( response instanceof RenderResponse)
			cmsCtx.setResponse( (RenderResponse)response);
		cmsCtx.setScope(getScope());
		cmsCtx.setDisplayLiveVersion(getDisplayLiveVersion());
		cmsCtx.setPageId(getPageId());
		cmsCtx.setDoc(getCurrentDoc());
		cmsCtx.setHideMetaDatas(getHideMetaDatas());
		cmsCtx.setDisplayContext(displayContext);

		
		return cmsCtx;
	}
	
	
	public String getDebugInfos()	{
		String output = "";
		
		if ("1".equals(System.getProperty("nuxeo.debugHtml"))) {
			
			output += "<p class=\"nuxeo-debug\" align=\"center\">";
			output += "scope : " + getScope();
			output += "</p>";

			return output;
		} else	{
			
			output += "<!--";
			output += "scope : " + getScope();
			output += "-->";
		}
		
		return output;

	}

}
