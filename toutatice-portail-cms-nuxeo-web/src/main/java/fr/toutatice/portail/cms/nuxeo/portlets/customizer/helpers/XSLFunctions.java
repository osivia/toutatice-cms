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

import javax.portlet.MimeResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;

/**
 * XSL functions.
 */
public class XSLFunctions {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(XSLFunctions.class);

    /** Resource pattern. */
    private static final Pattern PATTERN_RESOURCE = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/files:files/([0-9]*)/(.*)");
    /** Blob pattern. */
    private static final Pattern PATTERN_BLOB = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/blobholder:([0-9]*)/(.*)");
    /** Picture pattern. */
    private static final Pattern PATTERN_PICTURE = Pattern.compile("/nuxeo/nxpicsfile/default/([a-zA-Z0-9[-]&&[^/]]*)/(.*):content/(.*)");
    /** Web ID pattern. */
    private static final Pattern PATTERN_WEB_ID = Pattern.compile("/nuxeo/web/([a-zA-Z0-9[-]/]*)(.*)");
    /** Internal picture pattern. */
    private static final Pattern PATTERN_INTERNAL_PICTURE = Pattern.compile("/nuxeo/([a-z]*)/default/([a-zA-Z0-9[-]&&[^/]]*)/ttc:images/([0-9]*)/(.*)");
    /** Permalink pattern. */
    private static final Pattern PATTERN_PERMALINK = Pattern.compile("/nuxeo/nxdoc/default/([^/]*)/view_documents(.*)");
    /** Document pattern. */
    private static final Pattern PATTERN_DOCUMENT = Pattern.compile("/nuxeo/([a-z]*)/default([^@]*)@view_documents(.*)");
    /** Portal reference pattern. */
    private static final Pattern PATTERN_PORTAL_REF = Pattern.compile("http://([^/:]*)(:[0-9]*)?/([^/]*)(/auth/|/)pagemarker/([0-9]*)/(.*)");

    /** Portal reference. */
    private static final String PORTAL_REFERENCE = "/portalRef?";
    /** Portal reference length. */
    private static final int PORTAL_REFERENCE_LENGTH = PORTAL_REFERENCE.length();

    /** CMS context. */
    private final CMSServiceCtx cmsContext;

    /** Portal controller context. */
    private final PortalControllerContext portalControllerContext;
    /** Nuxeo controller. */
    private final NuxeoController nuxeoController;

    /** Nuxeo base URIs. */
    private final List<URI> nuxeoBaseURIs;

    /** Portal URL factory. */
    private final IPortalUrlFactory portalURLFactory;
    /** Web ID service. */
    private final IWebIdService webIdService;

    /** Portal reference matcher. */
    private final Matcher portalReferenceMatcher;


    /**
     * Constructor.
     *
     * @param nuxeoCustomizer Nuxeo customizer
     * @param cmsContext CMS context
     */
    public XSLFunctions(INuxeoCustomizer nuxeoCustomizer, CMSServiceCtx cmsContext) {
        super();
        this.cmsContext = cmsContext;

        // Portlet context
        PortletContext portletContext = cmsContext.getPortletCtx();
        // Request
        PortletRequest request = cmsContext.getRequest();
        // Response
        MimeResponse response = cmsContext.getResponse();


        // Portal controller context
        this.portalControllerContext = new PortalControllerContext(portletContext, request, response);
        // Nuxeo controller
        this.nuxeoController = new NuxeoController(request, response, portletContext);
        this.nuxeoController.setCurrentDoc((Document) cmsContext.getDoc());

        // Nuxeo base URIs
        this.nuxeoBaseURIs = this.getNuxeoBaseURIs();

        // Portal URL factory
        this.portalURLFactory = (IPortalUrlFactory) this.cmsContext.getPortletCtx().getAttribute("UrlService");
        // Web ID service
        this.webIdService = (IWebIdService) this.cmsContext.getPortletCtx().getAttribute("webIdService");

        // Portal reference matcher
        this.portalReferenceMatcher = this.getPortalReferenceMatcher(this.portalURLFactory, this.portalControllerContext);
    }


    /**
     * Get Nuxeo base URIs.
     *
     * @return Nuxeo base URIs
     */
    private List<URI> getNuxeoBaseURIs() {
        List<URI> baseURIs = new ArrayList<URI>();

        // Nuxeo public URI
        URI publicBaseURI = NuxeoConnectionProperties.getPublicBaseUri();
        baseURIs.add(publicBaseURI);

        // Alternate paths
        String alternativeServerNames = System.getProperty("nuxeo.alternativeServerNames");
        if (StringUtils.isNotEmpty(alternativeServerNames)) {
            String scheme = "http";
            String path = NuxeoConnectionProperties.getNuxeoContext();

            for (String host : StringUtils.split(alternativeServerNames, "\\|")) {
                try {
                    URI alternativeBaseURI = new URI(scheme, host, path, null);
                    baseURIs.add(alternativeBaseURI);
                } catch (URISyntaxException e) {
                    // Do nothing
                }
            }
        }

        return baseURIs;
    }


    /**
     * Get portal reference matcher.
     *
     * @param portalURLFactory portal URL factory
     * @param portalControllerContext portal controller context
     * @return portal reference matcher
     */
    private Matcher getPortalReferenceMatcher(IPortalUrlFactory portalURLFactory, PortalControllerContext portalControllerContext) {
        String sampleURL = portalURLFactory.getCMSUrl(portalControllerContext, null, StringUtils.EMPTY, null, null, null, null, null, null, null);
        Matcher portalReferenceMatcher = PATTERN_PORTAL_REF.matcher(sampleURL);
        return portalReferenceMatcher;
    }


    /**
     * Get max chars to display.
     *
     * @return max chars
     */
    private int getMaxChars() {
        int maxChars = 0;
        try {
            if (this.cmsContext.getRequest().getAttribute("maxChars") != null) {
                maxChars = Integer.parseInt((String) this.cmsContext.getRequest().getAttribute("maxChars"));
            }
        } catch (NumberFormatException e) {

        }
        return maxChars;
    }


    /**
     * Get WYSIWYG display mode (full or partial).
     *
     * @return WYSIWYG display mode
     */
    public String wysiwygDisplayMode() {
        String displayMode = "complet";

        if (this.getMaxChars() > 0) {
            if (WindowState.NORMAL.equals(this.cmsContext.getRequest().getWindowState())) {
                displayMode = "partiel";
            }
        }

        return displayMode;
    }


    /**
     * Get maximized link.
     *
     * @return maximized link
     * @throws WindowStateException
     */
    public String maximizedLink() throws WindowStateException {
        PortletURL portletUrl = this.cmsContext.getResponse().createRenderURL();
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        return portletUrl.toString();
    }


    /**
     * Adapt link.
     *
     * @param link link
     * @return adapted link
     */
    public String link(String link) {
        if (link.startsWith("#")) {
            return link;
        } else {
            return this.rewrite(link, true);
        }
    }


    /**
     * Rewrite link URL.
     *
     * @param link link URL
     * @param checkScope check scope indicator
     * @return rewrited link URL
     */
    private String rewrite(String link, boolean checkScope) {
        try {
            // v.0.13 : ajout de liens vers le portail

            /* Liens vers le portail */

            // JSS 20130321 :
            // - le lien portalRef peut etre préfixé
            // - les liens portails type permalink doitent etre regénérés avec un pageMarker (gestion du retour des onglets dynamiques)
            try {
                int iPortalRef = link.indexOf(PORTAL_REFERENCE);
                if (iPortalRef != -1) {
                    // Liens de type portalRef permettant d'instancier une page dynamique
                    String paramsArray[] = link.substring(iPortalRef + PORTAL_REFERENCE_LENGTH).split("&");
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

                        String dynamicUrl = this.portalURLFactory.getStartPageUrl(this.portalControllerContext, portalName, pageName, templatePath, dynaProps,
                                dynaParams);
                        return dynamicUrl;
                    }
                } else {
                    // Autres liens portails
                    // Ils sont retraités pour ajouter le page marker
                    Matcher mReference = this.portalReferenceMatcher;
                    // COntrole du host + context (portail ou portal ...)
                    if (link.startsWith("http://" + mReference.group(1))) {
                        // COntrole du contexte portail, portal
                        int indiceRawPath = link.indexOf("/", 7);
                        if (indiceRawPath != -1) {
                            String rawPath = link.substring(indiceRawPath);
                            if ((rawPath.length() > 1) && rawPath.substring(1).startsWith(mReference.group(3))) {
                                return this.transformPortalURL(link, mReference);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Probleme dans le parsing, on continue plutot de de renvoyer l'url d'origine
                // il peut par exemple s'agir d'une url nuxeo mal parsée ...
            }


            // On traite uniquement les liens absolus ou commencant par /nuxeo
            if (!link.startsWith("http") && !link.startsWith(NuxeoConnectionProperties.getNuxeoContext())) {
                // correction v2 pour le mailto
                // return "";
                return link;
            }

            String trim = link.trim().replace(" ", "%20");

            for (URI baseURI : this.nuxeoBaseURIs) {
                URI url = baseURI.resolve(trim);
                if (url.getScheme().equals("http") || url.getScheme().equals("https")) {
                    if (url.getHost().equals(baseURI.getHost())) {
                        String query = url.getRawPath();

                        Matcher mRes = PATTERN_RESOURCE.matcher(query);
                        if (mRes.matches()) {
                            if (mRes.groupCount() > 0) {
                                String uid = mRes.group(2);

                                // v 1.0.11 : pb. des pices jointes dans le proxy
                                // Ne fonctionne pas correctement
                                if (this.cmsContext.getDoc() != null) {
                                    uid = ((Document) this.cmsContext.getDoc()).getId();
                                }

                                String fileIndex = mRes.group(3);

                                return this.nuxeoController.createAttachedFileLink(uid, fileIndex);
                            }
                        }

                        // Ajout v1.0.13 : internal picture
                        Matcher mResInternalPicture = PATTERN_INTERNAL_PICTURE.matcher(query);
                        if (mResInternalPicture.matches()) {
                            if (mResInternalPicture.groupCount() > 0) {
                                String uid = mResInternalPicture.group(2);

                                if (this.cmsContext.getDoc() != null) {
                                    uid = ((Document) this.cmsContext.getDoc()).getId();
                                }

                                String pictureIndex = mResInternalPicture.group(3);

                                String portalLink = this.nuxeoController.createAttachedPictureLink(uid, pictureIndex);
                                return portalLink;
                            }
                        }

                        Matcher mPictures = PATTERN_PICTURE.matcher(query);
                        if (mPictures.matches()) {
                            if (mPictures.groupCount() > 0) {
                                String uid = mPictures.group(1);
                                String content = mPictures.group(2);
                                return this.nuxeoController.createPictureLink(uid, content);
                            }
                        }

                        Matcher mDoc = PATTERN_DOCUMENT.matcher(query);
                        if (mDoc.matches()) {
                            if (mDoc.groupCount() > 0) {
                                String path = mDoc.group(2);

                                String parameters = url.getQuery();
                                if(StringUtils.isNotBlank(parameters)){
                                    path = path.concat("?").concat(parameters);
                                }

                                // v2 : simplification : phase de redirection trop complexe
                                String portalLink = this.nuxeoController.getCMSLinkByPath(path, null).getUrl();
                                return portalLink;
                            } else {
                                return url.toString();
                            }
                        }

                        // 1.0.27 ajout permalin nuxeo + blobholder (lien téléchargement)
                        Matcher permaDoc = PATTERN_PERMALINK.matcher(query);
                        if (permaDoc.matches()) {
                            if (permaDoc.groupCount() > 0) {
                                String id = permaDoc.group(1);
                                return this.nuxeoController.getCMSLinkByPath(id, null).getUrl();
                            } else {
                                return url.toString();
                            }
                        }

                        // Lien téléchargement directement extrait de nuxeo
                        Matcher mBlobExp = PATTERN_BLOB.matcher(query);
                        if (mBlobExp.matches()) {
                            if (mBlobExp.groupCount() > 0) {
                                String uid = mBlobExp.group(2);
                                String blobIndex = mBlobExp.group(3);
                                return this.nuxeoController.createAttachedBlobLink(uid, blobIndex);
                            }
                        }

                        Matcher mWebId = PATTERN_WEB_ID.matcher(query);
                        if (mWebId.matches()) {
                            if (mWebId.groupCount() > 0) {
                                String webpath = mWebId.group(1);

                                String params = url.getQuery();
                                if (params != null) {
                                    query = query.concat("?").concat(params);
                                    String[] split = params.split("&");
                                    for (String element : split) {
                                        // In case of resources url, serve the resource
                                        if (element.startsWith("content")) {
                                            String[] param = element.split("=");
                                            String webId = this.webIdService.cmsPathToFetchPath(webpath);
                                            return this.nuxeoController.createPictureLink(webId, param[1]);
                                        }
                                    }
                                }
                                // In case of pages
                                return this.nuxeoController.getLinkFromNuxeoURL(query).getUrl();
                            }
                        }

                        return url.toString();
                    }
                }
            }
        } catch (Exception e) {
            // Don't block on a link
            LOGGER.error("Link " + link + "generates " + e.getMessage());
        }
        return link;
    }


    /**
     * Transform portal URL.
     *
     * @param originalUrl original URL
     * @param referenceMatcher reference matcher
     * @return transformed portal URL
     */
    private String transformPortalURL(String originalUrl, Matcher referenceMatcher) throws Exception {
        // Les urls portail sont toutes absolues
        Pattern expOrginial = Pattern.compile("http://([^/:]*)(:[0-9]*)?/" + referenceMatcher.group(3) + "(/auth/|/)((pagemarker/[0-9]*/)?)(.*)");

        Matcher mResOriginal = expOrginial.matcher(originalUrl);
        if (!mResOriginal.matches()) {
            throw new Exception("Not a portal URL !!!");
        }

        String transformedUrl = "";
        transformedUrl = "http://" + mResOriginal.group(1);

        // Port
        if (StringUtils.isNotEmpty(mResOriginal.group(2))) {
            transformedUrl += mResOriginal.group(2);
        }
        // context
        transformedUrl += "/" + referenceMatcher.group(3);

        // auth
        transformedUrl += referenceMatcher.group(4);

        // add pagemarker
        transformedUrl += "pagemarker/";
        transformedUrl += referenceMatcher.group(5) + '/';

        // End of url
        transformedUrl += mResOriginal.group(6);

        return transformedUrl;
    }


    /**
     * Get thumbnail image source.
     *
     * @param imageSource image source
     * @return thumbnail image source
     */
    public String thumbnailSource(String imageSource) {
        String thumbnailSource;

        Pattern pattern = Pattern.compile("^(.*\\?(.*&)?)content=([A-Za-z]+)(&.*)?$");
        Matcher matcher = pattern.matcher(imageSource);
        if (matcher.matches()) {
            // Adapt mediatheque image content size
            StringBuilder builder = new StringBuilder();
            builder.append(matcher.group(1));
            builder.append("content=Original");
            builder.append(StringUtils.trimToEmpty(matcher.group(4)));
            thumbnailSource = builder.toString();
        } else {
            thumbnailSource = imageSource;
        }

        return this.link(thumbnailSource);
    }


    public String thumbnailClasses(String styles) {
        String classes;
        if (StringUtils.isEmpty(styles)) {
            classes = StringUtils.EMPTY;
        } else {
            // String builder
            StringBuilder builder = new StringBuilder();

            // Style properties
            Map<String, String> properties = new HashMap<String, String>();
            for (String style : StringUtils.split(styles, ";")) {
                String[] property = StringUtils.split(style, ":");
                if (property.length == 2) {
                    String key = StringUtils.lowerCase(StringUtils.trim(property[0]));
                    String value = StringUtils.lowerCase(StringUtils.trim(property[1]));
                    properties.put(key, value);
                }
            }

            // Float
            String floatValue = properties.get("float");
            if ("left".equals(floatValue)) {
                builder.append("pull-left ");
            } else if ("right".equals(floatValue)) {
                builder.append("pull-right ");
            }

            classes = builder.toString();
        }

        return classes;
    }

}
