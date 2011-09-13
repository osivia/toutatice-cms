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


import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoConnection;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandService;
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
	NuxeoConnection nuxeoConnection;
	IProfilManager profilManager;
	String scope;
	public String getScope() {
		return scope;
	}

	int scopeType = NuxeoCommandContext.SCOPE_TYPE_USER;	
	private ProfilBean scopeProfil = null;


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
		setScopeType(NuxeoCommandContext.SCOPE_TYPE_USER);
		
		if ("anonymous".equals(scope)) {
			setScopeType( NuxeoCommandContext.SCOPE_TYPE_ANONYMOUS);
		} else if( scope != null) {
			setScopeType( NuxeoCommandContext.SCOPE_TYPE_PROFIL);
			setScopeProfil(getNuxeoService().getProfilManager().getProfil(scope));
		}

		this.scope = scope;
		
	}
	
	public int getScopeType() {
		return scopeType;
	}

	public void setScopeType(int scopeType) {
		this.scopeType = scopeType;
	}	

	public NuxeoConnection getNuxeoConnection() {
		if (nuxeoConnection == null)
			nuxeoConnection = new NuxeoConnection();
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

	public NuxeoController(PortletRequest request, RenderResponse response, PortletContext portletCtx) {
		super();
		this.request = request;
		this.response = response;
		this.portletCtx = portletCtx;
	}

	public NuxeoController(PortletContext portletCtx) {
		super();
		this.portletCtx = portletCtx;
	}

	public IPortalUrlFactory getPortalUrlFactory() {
		if (urlFactory == null)
			urlFactory = (IPortalUrlFactory) portletCtx.getAttribute("PortalUrlFactory");
		return urlFactory;
	}

	public INuxeoCommandService getNuxeoService() throws Exception {
		if (nuxeoService == null)
			nuxeoService = (INuxeoCommandService) NuxeoCommandServiceFactory.getNuxeoCommandService(getPortletCtx());
		return nuxeoService;
	}

	public IProfilManager getProfilManager() throws Exception {
		if (profilManager == null)
			profilManager = (IProfilManager) getNuxeoService().getProfilManager();
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
			if (scope == null || scope.length() == 0) {

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

	public String createFileLink(Document doc, String fieldName) {

		ResourceURL resourceURL = response.createResourceURL();
		resourceURL.setResourceID(doc.getId() + "/" + fieldName);
		resourceURL.setParameter("type", "file");
		resourceURL.setParameter("docPath", doc.getPath());
		resourceURL.setParameter("fieldName", fieldName);
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

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


	public URI getNuxeoBaseUri() {
		if (nuxeoBaseURI == null) {
			nuxeoBaseURI = getNuxeoConnection().getBaseUri();
		}

		return nuxeoBaseURI;
	}

	public void handleErrors(NuxeoException e) throws Exception {
		PortletErrorHandler.handleGenericErrors(getResponse(), e);
	}

	public Object executeNuxeoCommand(INuxeoCommand command) throws Exception {

		NuxeoCommandContext ctx = new NuxeoCommandContext(portletCtx, request);

		ctx.setScopeType(getScopeType());
		ctx.setScopeProfil(getScopeProfil());
		ctx.setCacheTimeOut(cacheTimeOut);
		ctx.setAsynchronousUpdates(asynchronousUpdates);

		return getNuxeoService().executeCommand(ctx, command);
	}
	


	public void startNuxeoService() throws Exception {
		NuxeoCommandServiceFactory.startNuxeoCommandService(getPortletCtx());
	}

	public void stopNuxeoService() throws Exception {
		NuxeoCommandServiceFactory.stopNuxeoCommandService(getPortletCtx());
	}

}
