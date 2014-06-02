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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

public class XSLFunctions {

	private static Log logger = LogFactory.getLog(XSLFunctions.class);

    private static DefaultCMSCustomizer customizer;
    CMSServiceCtx ctx;
    IPortalUrlFactory urlFactory;
    PortalControllerContext portalCtx;
    NuxeoController nuxeoCtx;
    IWebIdService webIdService;


	private final Pattern scope = Pattern.compile(".*");

//	private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z&&[^/]]*)/default/(.*)(.*)/");

	// "/nuxeo/nxfile/default/0d067ed3-2d6d-4786-9708-d65f444cb002/files:files/0/file/disconnect.png";
	private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/files:files/([0-9]*)/(.*)");


	private final Pattern blobExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/blobholder:([0-9]*)/(.*)");

	///nuxeo/nxpicsfile/default/3e0f9ada-c48f-4d89-b410-e9cc93a79d78/Original:content/Wed%20Jan%2004%2021%3A41%3A25%20CET%202012
	private final Pattern picturesExp = Pattern.compile("/nuxeo/nxpicsfile/default/([a-zA-Z0-9[-]&&[^/]]*)/(.*):content/(.*)");


    private final Pattern webIdExp = Pattern.compile("/nuxeo/web/([a-zA-Z0-9[-]/]*)(.*)");


	///nuxeo/nxfile/default/a1bbb41d-88f7-490c-8480-7772bb085a4c/ttc:images/0/file/banniere.jpg
	private final Pattern internalPictureExp = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/ttc:images/([0-9]*)/(.*)");

	private final Pattern permaLinkExp = Pattern.compile("/nuxeo/nxdoc/default/([^/]*)/view_documents(.*)");

	private final Pattern documentExp = Pattern.compile("/nuxeo/([a-z]*)/default([^@]*)@view_documents(.*)");

	private final Pattern portalRefExp = Pattern.compile("http://([^/:]*)(:[0-9]*)?/([^/]*)(/auth/|/)pagemarker/([0-9]*)/(.*)");
	private Matcher portalMatcherReference = null;

	private static final String PORTAL_REF = "/portalRef?";

	private static final int PORTAL_REF_LG = PORTAL_REF.length();

	public XSLFunctions(DefaultCMSCustomizer _customizer, CMSServiceCtx ctx) {
	    customizer = _customizer;
		this.ctx = ctx;
	}

	 public IPortalUrlFactory getPortalUrlFactory() throws Exception {
	        if (this.urlFactory == null) {
                this.urlFactory = (IPortalUrlFactory) this.ctx.getPortletCtx().getAttribute("UrlService");
            }

	        return this.urlFactory;
	}



	 public PortalControllerContext getPortalControllerContext() throws Exception {
           if (this.portalCtx == null)   {
               this.portalCtx = new PortalControllerContext(this.ctx.getPortletCtx(), this.ctx.getRequest(), this.ctx.getResponse());
           }

           return this.portalCtx;
    }


     public NuxeoController getNuxeoController() throws Exception {
         if (this.nuxeoCtx == null)   {
             this.nuxeoCtx = new NuxeoController( this.ctx.getRequest(), this.ctx.getResponse(), this.ctx.getPortletCtx());
         }

         return this.nuxeoCtx;
  }


    /**
     * WebId service used to transform urls
     * 
     * @return the service
     */
    public IWebIdService getWebIdService() {
        if (this.webIdService == null) {
            this.webIdService = (IWebIdService) this.ctx.getPortletCtx().getAttribute("webIdService");
        }

        return this.webIdService;
    }

	private static  List<URI> baseURIs = null;

	public static List<URI> getNuxeoBaseURIs() throws URISyntaxException	{

		if (baseURIs == null) {
			setNuxeoBaseURIs();
		}

		return baseURIs;
	 }



	public static synchronized void setNuxeoBaseURIs() throws URISyntaxException	{

		if( baseURIs == null){

			List<URI> tmpBaseURIs = new ArrayList<URI>();

			// First, the nuxeo public URI
			tmpBaseURIs.add(customizer.getNuxeoConnectionProps().getPublicBaseUri());

			// Then the alternate paths
			String altServers = System.getProperty("nuxeo.alternativeServerNames");

			if (altServers != null) {
				String[] serverToks = altServers.split("\\|");
				for (String serverTok : serverToks) {
					tmpBaseURIs.add(new URI("http://" + serverTok + customizer.getNuxeoConnectionProps().getNuxeoContext()));
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
			if( this.ctx.getRequest().getAttribute("maxChars") != null)	{
				maxChars = Integer.parseInt((String)this.ctx.getRequest().getAttribute("maxChars"));
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

		if( this.getMaxChars() > 0)	{
			if( WindowState.NORMAL.equals(this.ctx.getRequest().getWindowState())) {
                displayMode = "partiel";
            }
		}

		return displayMode;
	}

	public String maximizedLink() throws WindowStateException	{

		PortletURL portletUrl = null;


		if( this.ctx.getResponse() instanceof RenderResponse) {
            portletUrl = this.ctx.getResponse().createRenderURL();
        } else if( this.ctx.getResponse() instanceof ResourceResponse) {
            portletUrl = ((ResourceResponse) this.ctx.getResponse()).createRenderURL();
        }

		portletUrl.setWindowState(WindowState.MAXIMIZED);

		return portletUrl.toString();
	}



	public String link(String link) {
		if (link.startsWith("#")) {
			return link;
		} else {
			return this.rewrite(link, true);
		}
	}










public static String transformPortalUrl(String orginalUrl,  Matcher mResReference) throws Exception	{
		// Les urls portail sont toutes absolues

		Pattern expOrginial = Pattern.compile("http://([^/:]*)(:[0-9]*)?/"+mResReference.group(3)+"(/auth/|/)((pagemarker/[0-9]*/)?)(.*)");


		Matcher mResOriginal = expOrginial.matcher(orginalUrl);

		if( !mResOriginal.matches()) {
            throw new Exception("Not a portal URL !!!");
        }


		String transformedUrl = "";
		transformedUrl = "http://"+ mResOriginal.group(1);

		//Port
		if(StringUtils.isNotEmpty(mResOriginal.group(2))){
			transformedUrl += mResOriginal.group(2);
		}
		//context
		transformedUrl += "/" + mResReference.group(3);

		//auth
		transformedUrl +=  mResReference.group(4);

		// add pagemarker
		transformedUrl += "pagemarker/";
		transformedUrl += mResReference.group(5) + '/';

		// End of url
		transformedUrl += mResOriginal.group(6);

		return transformedUrl;
	}



// TODO : enlever bidouille ( génération d'un lien pour récupérer host, context, page marker)
// car inaccessibles dans le context sans modifie l'API
// A refaire pendant packaging toutatice-cms


public Matcher getPortalMatcherReference() throws Exception	{

	if( this.portalMatcherReference == null)	{
		String sampleUrl = this.getPortalUrlFactory().getCMSUrl(
		        this.getPortalControllerContext(), null, "", null, null, null, null,
				null, null, null);

		Matcher mReference = this.portalRefExp.matcher(sampleUrl);

		if( mReference.matches())	{
			this.portalMatcherReference = mReference;
		} else {
            throw new Exception("reference is unmatchable");
        }

	}

		return this.portalMatcherReference;
}



	private String rewrite(String link, boolean checkScope) {

		try	{

		// v.0.13 : ajout de liens vers le portail

		/* Liens vers le portail */


		// JSS 20130321 :
		// - le lien portalRef peut etre préfixé
		// - les liens portails type permalink doitent etre regénérés avec un pageMarker (gestion du retour des onglets dynamiques)


			try	{

			int iPortalRef = link.indexOf(PORTAL_REF);

			if (iPortalRef != -1) {

				// Liens de type portalRef permettant d'instancier une page dynamique

							String paramsArray[] = link.substring(
									iPortalRef + PORTAL_REF_LG).split("&");
							Map<String, String> params = new HashMap<String, String>();

							for (String element : paramsArray) {
								String values[] = element.split("=");
								if (values.length == 2) {
									params.put(values[0], values[1]);
								}
							}

                    if ("dynamicPage".equals(params.get("type"))) {
                        String portalName = "/" + PageProperties.getProperties().getPagePropertiesMap().get(Constants.PORTAL_NAME);

                        String templatePath = params.get("templatePath");
                        String pageName = params.get("pageName");
                        if (pageName == null) {
                            pageName = "genericDynamicWindow";
                        }

                        Map<String, String> dynaProps = new HashMap<String, String>();
                        Map<String, String> dynaParams = new HashMap<String, String>();

                        String dynamicUrl = this.getPortalUrlFactory().getStartPageUrl(this.getPortalControllerContext(), portalName, pageName, templatePath,
                                dynaProps, dynaParams);
                        return dynamicUrl;
                    }

		}
			else	{
					// Autres liens portails
					// Ils sont retraités pour ajouter le page marker

					Matcher mReference = this.getPortalMatcherReference();

					// COntrole du host + context (portail ou portal ...)
					if (link.startsWith("http://" + mReference.group(1))) {
						// COntrole du contexte portail, portal
						int indiceRawPath = link.indexOf("/", 7);
						if (indiceRawPath != -1) {
							String rawPath = link.substring(indiceRawPath);
							if ((rawPath.length() > 1)
									&& rawPath.substring(1).startsWith(
											mReference.group(3))) {

								return transformPortalUrl(link, mReference);

							}
						}

					}
			}
			} catch( Exception e)	{
				// Probleme dans le parsing, on continue plutot de de renvoyer l'url d'origine
				// il peut par exemple s'agir d'une url nuxeo mal parsée ...
		}


		//On traite uniquement les liens absolus ou commencant par /nuxeo
		if( !link.startsWith("http") && !link.startsWith(customizer.getNuxeoConnectionProps().getNuxeoContext())) {
            //	correction v2 pour le mailto
			//return "";
			return link;
        }

		String trim = link.trim().replace(" ", "%20");



		for(URI baseURI : getNuxeoBaseURIs())	{

		URI url = baseURI.resolve(trim);


		if (url.getScheme().equals("http") || url.getScheme().equals("https")) {
			if (url.getHost().equals(baseURI.getHost())) {

					//String testUrl = "/nuxeo/nxfile/default/0d067ed3-2d6d-4786-9708-d65f444cb002/files:files/0/file/disconnect.png";
//					private final Pattern ressourceExp = Pattern.compile("/nuxeo/([a-z&&[^/]]*)/default/(.*)(.*)/");

					String query = url.getRawPath();


					Matcher mRes = this.ressourceExp.matcher(query);

					if( mRes.matches())	{

					if (mRes.groupCount() > 0) {

						String uid = mRes.group(2);

						//v 1.0.11 : pb. des pices jointes dans le proxy
						// Ne fonctionne pas correctement
						if(  this.ctx.getDoc() != null) {
                            uid = ((Document) this.ctx.getDoc()).getId();
                        }

						String fileIndex = mRes.group(3);

						return this.getNuxeoController().createAttachedFileLink(uid, fileIndex);
						}
					}

					// Ajout v1.0.13 : internal picture

					Matcher mResInternalPicture = this.internalPictureExp.matcher(query);

					if( mResInternalPicture.matches())	{

					if (mResInternalPicture.groupCount() > 0) {

						String uid = mResInternalPicture.group(2);

	                    if(  this.ctx.getDoc() != null) {
                            uid = ((Document) this.ctx.getDoc()).getId();
                        }

						String pictureIndex = mResInternalPicture.group(3);

						String portalLink =    this.getNuxeoController().createAttachedPictureLink(uid, pictureIndex);
						return portalLink;
						}
					}





					Matcher mPictures = this.picturesExp.matcher(query);

					if( mPictures.matches())	{

					if (mPictures.groupCount() > 0) {

						String uid = mPictures.group(1);

						String content = mPictures.group(2);

						return  this.getNuxeoController().createPictureLink(uid, content);
						}
					}

						Matcher mDoc = this.documentExp.matcher(query);

						if( mDoc.matches())	{

						if (mDoc.groupCount() > 0) {

							String path = mDoc.group(2);


							// v2 : simplification : phase de redirection trop complexe

							String portalLink =   this.getNuxeoController().getCMSLinkByPath(path, null).getUrl();
							return portalLink;


							//return ctx.createRedirectDocumentLink(path);
						} else {
                            return url.toString();
                        }
						}



						// 1.0.27 ajout permalin nuxeo + blobholder (lien téléchargement)

						Matcher permaDoc = this.permaLinkExp.matcher(query);

						if( permaDoc.matches())	{

						if (permaDoc.groupCount() > 0) {

							String id = permaDoc.group(1);

							return  this.getNuxeoController().getCMSLinkByPath(id, null).getUrl();
						} else {
                            return url.toString();
                        }
						}



						// Lien téléchargement directement extrait de nuxeo
						Matcher mBlobExp = this.blobExp.matcher(query);

						if( mBlobExp.matches())	{

						if (mBlobExp.groupCount() > 0) {

							String uid = mBlobExp.group(2);


							String blobIndex = mBlobExp.group(3);

							return  this.getNuxeoController().createAttachedBlobLink(uid, blobIndex);
							}
						}


                        Matcher mWebId = this.webIdExp.matcher(query);

                        if (mWebId.matches()) {

                            if (mWebId.groupCount() > 0) {

                                String webpath = mWebId.group(1);

                                String params = url.getQuery();
                                if (params != null) {
                                    String[] split = params.split("&");
                                    for (int i = 0; i < split.length; i++) {

                                        // In case of resources url, serve the resource
                                        if (split[i].startsWith("content")) {
                                            String[] param = split[i].split("=");
                                            
                                            String webId = getWebIdService().webPathToFetchInfoService(webpath);

                                            return this.getNuxeoController().createWebIdLink(webId, param[1]);
                                        }
                                    }
                                }
                                // In case of pages
                                return this.getNuxeoController().getCMSLinkByPath(getWebIdService().webPathToPageUrl(webpath), null).getUrl();

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
		return this.scope.matcher(uri.toString()).matches();
	}



	public boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == s2) {
            return true;
        }
		if (s1 == null) {
            return false;
        }
		return s1.equalsIgnoreCase(s2);
	}

}
