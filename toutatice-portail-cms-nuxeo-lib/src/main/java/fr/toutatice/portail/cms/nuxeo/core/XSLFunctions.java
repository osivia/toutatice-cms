package fr.toutatice.portail.cms.nuxeo.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.api.contexte.PortalControllerContext;
import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

public class XSLFunctions {

	private static Log logger = LogFactory.getLog(XSLFunctions.class);

	NuxeoController ctx;

	private final Pattern scope = Pattern.compile(".*");

//	private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z&&[^/]]*)/default/(.*)(.*)/");
	
	// "/nuxeo/nxfile/default/0d067ed3-2d6d-4786-9708-d65f444cb002/files:files/0/file/disconnect.png";
	private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/files:files/([0-9]*)/(.*)");
	
	
	private final Pattern blobExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/blobholder:([0-9]*)/(.*)");

	///nuxeo/nxpicsfile/default/3e0f9ada-c48f-4d89-b410-e9cc93a79d78/Original:content/Wed%20Jan%2004%2021%3A41%3A25%20CET%202012
	private final Pattern picturesExp = Pattern.compile("/nuxeo/nxpicsfile/default/([a-zA-Z0-9[-]&&[^/]]*)/(.*):content/(.*)");
	
	///nuxeo/nxfile/default/a1bbb41d-88f7-490c-8480-7772bb085a4c/ttc:images/0/file/banniere.jpg	
	private final Pattern internalPictureExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/ttc:images/([0-9]*)/(.*)");
	
	private final Pattern permaLinkExp = Pattern.compile("/nuxeo/nxdoc/default/([^/]*)/view_documents(.*)");

	
	private final Pattern documentExp = Pattern.compile("/nuxeo/([a-z]*)/default([^@]*)@view_documents(.*)");
	
	private static final String PORTAL_REF = "/portalRef?";
	
	private static final int PORTAL_REF_LG = PORTAL_REF.length();

	public XSLFunctions(NuxeoController ctx) {
		this.ctx = ctx;
	}
	
	private static  List<URI> baseURIs = null;
	
	public static List<URI> getNuxeoBaseURIs(NuxeoController ctx) throws URISyntaxException	{
		
		if (baseURIs == null) {
			setNuxeoBaseURIs(ctx);
		}

		return baseURIs;
	}
	

	
	public static synchronized void setNuxeoBaseURIs(NuxeoController ctx) throws URISyntaxException	{
		
		if( baseURIs == null){
		
			List<URI> tmpBaseURIs = new ArrayList<URI>();

			// First, the nuxeo public URI
			tmpBaseURIs.add(ctx.getNuxeoPublicBaseUri());

			// Then the alternate paths
			String altServers = System.getProperty("nuxeo.alternativeServerNames");

			if (altServers != null) {
				String[] serverToks = altServers.split("\\|");
				for (int i = 0; i < serverToks.length; i++) {
					tmpBaseURIs.add(new URI("http://" + serverToks[i] + ctx.getNuxeoConnectionProps().getNuxeoContext()));
				}
			}
			
			baseURIs = tmpBaseURIs;
		}

	}

	
	
	/**
	 * Renvoie le nombre maxi de caractères à afficher
	 * 
	 * @return
	 */
	private int getMaxChars()	{
		int maxChars = 0 ;
		try	{
			if( ctx.getRequest().getAttribute("maxChars") != null)	{
				maxChars = Integer.parseInt((String)ctx.getRequest().getAttribute("maxChars"));
			}
		} catch (NumberFormatException e){
			
		}
		return maxChars;
	}
	
	/**
	 * Renvoie le type d'affichage : 'complet' ou 'partiel'
	 * 
	 * @return
	 */
	public String wysiwygDisplayMode()	{
		
		String displayMode = "complet";
		
		if( getMaxChars() > 0)	{
			if( WindowState.NORMAL.equals(ctx.getRequest().getWindowState()))
					displayMode = "partiel";
		}
		
		return displayMode;
	}
	
	public String maximizedLink() throws WindowStateException	{
		
		PortletURL portletUrl = null;
		
		
		if( ctx.getResponse() instanceof RenderResponse)
		 portletUrl = ((RenderResponse) ctx.getResponse()).createRenderURL();
		else if( ctx.getResponse() instanceof ResourceResponse)
		 portletUrl = ((ResourceResponse) ctx.getResponse()).createRenderURL();
		
		portletUrl.setWindowState(WindowState.MAXIMIZED);
		
		return portletUrl.toString();
	}

	

	public String link(String link) {
		if (link.startsWith("#")) {
			return link;
		} else {
			return rewrite(link, true);
		}
	}

	private String rewrite(String link, boolean checkScope) {
		
		try	{
		
		// v.0.13 : ajout de liens vers le portail
		
		if( link.startsWith(PORTAL_REF) ){

			try {
				String paramsArray[] = link.substring(PORTAL_REF_LG).split("&");
				Map<String, String> params = new HashMap<String, String>();

				for (int i = 0; i < paramsArray.length; i++) {
					String values[] = paramsArray[i].split("=");
					if (values.length == 2) {
						params.put(values[0], values[1]);
					}
				}

				if ("dynamicPage".equals(params.get("type"))) {

					String templatePath = params.get("templatePath");
					String pageName = params.get("pageName");
					if( pageName == null)
						pageName = "genericDynamicWindow";

					Map<String, String> dynaProps = new HashMap<String, String>();
					Map<String, String> dynaParams = new HashMap<String, String>();

					String dynamicUrl = ctx.getPortalUrlFactory().getStartPageUrl(ctx.getPortalCtx(), "/default",
							pageName, templatePath, dynaProps, dynaParams);
					return dynamicUrl;
				}
			} catch (Exception e) {
				return "";
			}
		}
		
		
		
		//On traite uniquement les liens absolus ou commencant par /nuxeo
		if( !link.startsWith("http") && !link.startsWith(ctx.getNuxeoConnectionProps().getNuxeoContext()))
			//	correction v2 pour le mailto
			//return "";
			return link;
		
		String trim = link.trim().replace(" ", "%20");
		
	
	
		for(URI baseURI : getNuxeoBaseURIs(ctx))	{
		
		URI url = baseURI.resolve(trim);
		

		if (url.getScheme().equals("http") || url.getScheme().equals("https")) {
			if (url.getHost().equals(baseURI.getHost())) {

					//String testUrl = "/nuxeo/nxfile/default/0d067ed3-2d6d-4786-9708-d65f444cb002/files:files/0/file/disconnect.png";
//					private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z&&[^/]]*)/default/(.*)(.*)/");
						
					String query = url.getRawPath();
						
						
						
						
						
					Matcher mRes = ressourceExp.matcher(query);
					
					if( mRes.matches())	{

					if (mRes.groupCount() > 0) {

						String uid = mRes.group(2);
						
						//v 1.0.11 : pb. des pices jointes dans le proxy
						// Ne fonctionne pas correctement
						if(  ctx.getCurrentDoc() != null)
							uid = ctx.getCurrentDoc().getId();
						
						String fileIndex = mRes.group(3);
						
						return ctx.createAttachedFileLink(uid, fileIndex);
						} 
					}
					
					// Ajout v1.0.13 : internal picture
					
					Matcher mResInternalPicture = internalPictureExp.matcher(query);
					
					if( mResInternalPicture.matches())	{

					if (mResInternalPicture.groupCount() > 0) {

						String uid = mResInternalPicture.group(2);
						
						if(  ctx.getCurrentDoc() != null)
							uid = ctx.getCurrentDoc().getId();
						
						String pictureIndex = mResInternalPicture.group(3);
						
						return ctx.createAttachedPictureLink(uid, pictureIndex);
						} 
					}
				
					
				
					
					
					Matcher mPictures = picturesExp.matcher(query);
					
					if( mPictures.matches())	{

					if (mPictures.groupCount() > 0) {

						String uid = mPictures.group(1);
						
						String content = mPictures.group(2);
						
						return ctx.createPictureLink(uid, content);
						} 
					}
					
						Matcher mDoc = documentExp.matcher(query);
						
						if( mDoc.matches())	{

						if (mDoc.groupCount() > 0) {

							String path = mDoc.group(2);
							
							
							// v2 : simplification : phase de redirection trop complexe
							
							return ctx.getCMSLinkByPath(path, null).getUrl();
							
								
							//return ctx.createRedirectDocumentLink(path);
						}
						else
							return url.toString();
						}
						
						
						
						// 1.0.27 ajout permalin nuxeo + blobholder (lien téléchargement)
												
						Matcher permaDoc = permaLinkExp.matcher(query);
						
						if( permaDoc.matches())	{

						if (permaDoc.groupCount() > 0) {

							String id = permaDoc.group(1);
								
							return ctx.getCMSLinkById(id, null).getUrl();
						}
						else
							return url.toString();
						}

						
											
						// Lien téléchargement directement extrait de nuxeo
						Matcher mBlobExp = blobExp.matcher(query);
						
						if( mBlobExp.matches())	{

						if (mBlobExp.groupCount() > 0) {

							String uid = mBlobExp.group(2);
							

							String blobIndex = mBlobExp.group(3);
							
							return ctx.createAttachedBlobLink(uid, blobIndex);
							} 
						}
						
						
				
				return url.toString();

			} 
			//else {
			//	return url.toString();
			//}
		} 
		
		}
		
		} catch (Exception e)	{
			//Don't block on a link
			
			logger.error("Link "+ link + "generates " + e.getMessage() );
			
		}
		return link;
	}

	private boolean shouldRewrite(URI uri) {
		return scope.matcher(uri.toString()).matches();
	}



	public boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == s2)
			return true;
		if (s1 == null)
			return false;
		return s1.equalsIgnoreCase(s2);
	}

}
