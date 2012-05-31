package fr.toutatice.portail.cms.nuxeo.api;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;
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
import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.locator.Locator;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.nuxeo.LinkHandlerCtx;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;
import fr.toutatice.portail.core.profils.IProfilManager;
import fr.toutatice.portail.core.profils.ProfilBean;

public class NuxeoController {

	private static Log log = LogFactory.getLog(NuxeoController.class);
	


	PortletRequest request;
	RenderResponse response;
	PortletContext portletCtx;
	IPortalUrlFactory urlFactory;
	String pageId;
	URI nuxeoBaseURI;
	NuxeoConnectionProperties nuxeoConnection;
	IProfilManager profilManager;
	String scope;
	String displayLiveVersion;
	String hideMetaDatas;
	
	//v 1.0.11 : pb. des pices jointes dans le proxy
	Document currentDoc;
	public Document getCurrentDoc() {
		return currentDoc;
	}
	public void setCurrentDoc(Document currentDoc) {
		this.currentDoc = currentDoc;
	}
	PortalControllerContext portalCtx;	
	

	public PortalControllerContext getPortalCtx() {
		return portalCtx;
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
		} else if( scope != null) {
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


	public PortletRequest getRequest() {
		return request;
	}

	public RenderResponse getResponse() {
		return response;
	}

	public PortletContext getPortletCtx() {
		return portletCtx;
	}

	public NuxeoController(PortletRequest request, RenderResponse response, PortletContext portletCtx) throws RuntimeException  {
		super();
		this.request = request;
		this.response = response;
		this.portletCtx = portletCtx;
		
		try	{
		PortalWindow window = WindowFactory.getWindow(request);
		
		portalCtx = new PortalControllerContext(portletCtx, request, response);
		
		String scope = window.getProperty("pia.cms.scope");
		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		String hideMetadatas = window.getProperty("pia.cms.hideMetaDatas");

		setScope(scope);
		setDisplayLiveVersion(displayLiveVersion);
		setHideMetaDatas(hideMetadatas);
		
		setPageMarker((String) request.getAttribute("pia.pageMarker"));
		
		} catch( Exception e)	{
			throw new RuntimeException( e);
		}
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

		// On sélectionne les profils ayant un utilisateur Nuxeo
		List<ProfilBean> profils = getProfilManager().getListeProfils();

		Map<String, String> scopes = new HashMap<String, String>();
		for (ProfilBean profil : profils) {
			if (profil.getNuxeoVirtualUser() != null && profil.getNuxeoVirtualUser().length() > 0) {
				scopes.put(profil.getName(), "Profil " + profil.getName());
			}
		}
		scopes.put("anonymous", "Anonyme");

		StringBuffer select = new StringBuffer();
		select.append("<select name=\"scope\">");

		if (!scopes.isEmpty()) {
			if (selectedScope == null || selectedScope.length() == 0) {

				select.append("<option selected=\"selected\" value=\"\">Pas de cache</option>");

			} else {

				select.append("<option value=\"\">Pas de cache</option>");

			}
			for (String possibleScope : scopes.keySet()) {
				if (selectedScope != null && selectedScope.length() != 0 && possibleScope.equals(selectedScope)) {

					select.append("<option selected=\"selected\" value=\"" + possibleScope + "\">"
							+ scopes.get(possibleScope) + "</option>");

				} else {

					select.append("<option value=\"" + possibleScope + "\">" + scopes.get(possibleScope) + "</option>");

				}
			}
		}

		select.append("</select>");

		return select.toString();

	}

	public String createFileLink(Document doc, String fieldName)  throws Exception {
		
		if( "ttc:vignette".equals(fieldName))	 {
			String url = getRequest().getContextPath() + "/thumbnail?" + "path=" + URLEncoder.encode(doc.getPath(), "UTF-8") ;
			return url;
			
		}	else	{

		ResourceURL resourceURL = response.createResourceURL();
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

		ResourceURL resourceURL = response.createResourceURL();
		resourceURL.setResourceID(doc.getId());
		resourceURL.setParameter("type", "link");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);

		return resourceURL.toString();
	}

	public String createAttachedFileLink(String path, String fileIndex) {

		ResourceURL resourceURL = response.createResourceURL();
		resourceURL.setResourceID(path + "/" + fileIndex);

		resourceURL.setParameter("type", "attachedFile");
		resourceURL.setParameter("fileIndex", fileIndex);
		resourceURL.setParameter("docPath", path);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	
	public String createAttachedPictureLink(String path, String fileIndex) {

		ResourceURL resourceURL = response.createResourceURL();
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

		ResourceURL resourceURL = response.createResourceURL();
		resourceURL.setResourceID(path + "/" + content);

		resourceURL.setParameter("type", "picture");
		resourceURL.setParameter("content", content);
		resourceURL.setParameter("docPath", path);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	
	public String createRedirectDocumentLink(String path) {

		// On ne peut se permettre de lire tous les docs référencés en lien hyper-texte
		// Il est préférable de faire une redirection
		
		ResourceURL resourceURL = response.createResourceURL();
		resourceURL.setResourceID(path );
		/*
		resourceURL.setResourceID("/pagemarker/"+path );
		*/

		resourceURL.setParameter("type", "documentLink");
		resourceURL.setParameter("docPath", path);
		if( pageMarker != null)
			resourceURL.setParameter("pageMarker", pageMarker);

		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}


	public URI getNuxeoPublicBaseUri() {
		if (nuxeoBaseURI == null) {
			nuxeoBaseURI = NuxeoConnectionProperties.getPublicBaseUri();
		}

		return nuxeoBaseURI;
	}

	public void handleErrors(NuxeoException e) throws Exception {
		PortletErrorHandler.handleGenericErrors(getResponse(), e);
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
		INuxeoService nuxeoService =(INuxeoService) getPortletCtx().getAttribute("NuxeoService");
		if( nuxeoService == null)
			nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");
		LinkHandlerCtx handlerCtx = new  LinkHandlerCtx( getPortletCtx(), getRequest(), getResponse(), getScope(), getDisplayLiveVersion(), getPageId(), getNuxeoPublicBaseUri(),  doc);
		handlerCtx.setHideMetaDatas(getHideMetaDatas());
		return nuxeoService.getLinkHandler().getLink(handlerCtx);
	}	
	
	public Link getServiceLink(Document doc) throws Exception 	{
		INuxeoService nuxeoService =(INuxeoService) getPortletCtx().getAttribute("NuxeoService");
		if( nuxeoService == null)
			nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");
		LinkHandlerCtx handlerCtx = new  LinkHandlerCtx( getPortletCtx(), getRequest(), getResponse(), getScope(), getDisplayLiveVersion(), getPageId(), getNuxeoPublicBaseUri(),  doc);
		handlerCtx.setHideMetaDatas(getHideMetaDatas());		
		return nuxeoService.getLinkHandler().getServiceLink(handlerCtx);
	}
	
}
