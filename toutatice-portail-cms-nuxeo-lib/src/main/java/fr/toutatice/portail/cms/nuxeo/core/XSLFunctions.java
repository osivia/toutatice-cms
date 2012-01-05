package fr.toutatice.portail.cms.nuxeo.core;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

public class XSLFunctions {

	private static Log logger = LogFactory.getLog(XSLFunctions.class);

	NuxeoController ctx;

	private final Pattern scope = Pattern.compile(".*");

//	private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z&&[^/]]*)/default/(.*)(.*)/");
	
	// "/nuxeo/nxfile/default/0d067ed3-2d6d-4786-9708-d65f444cb002/files:files/0/file/disconnect.png";
	private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/files:files/([0-9]*)/(.*)");
	
	private final Pattern documentExp = Pattern.compile("/nuxeo/([a-z]*)/default([^@]*)@view_documents(.*)");

	public XSLFunctions(NuxeoController ctx) {
		this.ctx = ctx;

		// TODO : attention aux images externes
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
		
		PortletURL portletUrl = ctx.getResponse().createRenderURL();
		
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
		
		//On traite uniquement les liens absolus ou commencant par /nuxeo
		if( !link.startsWith("http") && !link.startsWith(ctx.getNuxeoConnectionProps().getNuxeoContext()))
			return "";
		
		String trim = link.trim().replace(" ", "%20");
		URI url = ctx.getNuxeoPublicBaseUri().resolve(trim);

		if (url.getScheme().equals("http") || url.getScheme().equals("https")) {
			if (url.getHost().equals(ctx.getNuxeoPublicBaseUri().getHost())) {

					//String testUrl = "/nuxeo/nxfile/default/0d067ed3-2d6d-4786-9708-d65f444cb002/files:files/0/file/disconnect.png";
//					private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z&&[^/]]*)/default/(.*)(.*)/");
						
					try	{
					Matcher mRes = ressourceExp.matcher(url.getRawPath());
					
					if( mRes.matches())	{

					if (mRes.groupCount() > 0) {

						String uid = mRes.group(2);
						String fileIndex = mRes.group(3);
						
						return ctx.createAttachedFileLink(uid, fileIndex);
						} 
					}
					
						Matcher mDoc = documentExp.matcher(url.getRawPath());
						
						if( mDoc.matches())	{

						if (mDoc.groupCount() > 0) {

							String path = mDoc.group(2);
								
							return ctx.createRedirectDocumentLink(path);
						}
						else
							return url.toString();
						}
				} catch (Exception e) {
					//incorrect parsing, continue with the native value
				}
				
				return url.toString();

			} else {
				return url.toString();
			}
		} else {
			return link;
		}
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
