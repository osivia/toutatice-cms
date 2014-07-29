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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.Zoom;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ZoomEditableWindow;

/**
 * Display zooms of a current page.
 * 
 * @author lbi
 * 
 */
public class ZoomFragmentModule implements IFragmentModule {

    /** name. */
    public static final String ID = "zoom_property";

    /** description of module. */
    public static final String DESC = "Liste de zooms";

    /** jsp view-zoom in portlet fragment. */
    public static final String JSP = "zoom";

    /** jsp admin property in portlet fragment. */
    public static final String ADMIN_JSP = "zoom";

    private static final String REF_URI = "refURI";

    public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {

        String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);
        ;
        boolean emptyContent = true;


        if (StringUtils.isNotEmpty(nuxeoPath)) {

            nuxeoPath = ctx.getComputedPath(nuxeoPath);

            Document doc = ctx.fetchDocument(nuxeoPath);

            if (doc.getTitle() != null)
                response.setTitle(doc.getTitle());

            String propertyName = ZoomEditableWindow.ZOOM_LINKS;
            String refURI = window.getProperty("osivia.refURI");
            String view = window.getProperty("osivia.cms.style");

            if (StringUtils.isNotEmpty(propertyName)) {

                Object content = doc.getProperties().get(propertyName);
                List<Zoom> zoomContents = new ArrayList<Zoom>();

                // Si paramétrage de l'URI, propriétés du fragment attendues dans propertyName
                if (StringUtils.isNotEmpty(refURI)) {

                    if (content instanceof PropertyList) {

                        PropertyList dataContents = (PropertyList) content;

                        if (dataContents != null && dataContents.size() > 0) {

                            for (int index = 0; index < dataContents.size(); index++) {
                                PropertyMap mProperty = dataContents.getMap(index);
                                String refURIValue = (String) mProperty.get(REF_URI);

                                if (refURI.equalsIgnoreCase(refURIValue)) {
                                    Zoom zoom = new Zoom();
                                    PropertyMap map = (PropertyMap) dataContents.getMap(index);
                                    zoom.setDescription(map.getString(Zoom.DESCRIPTION));
                                    zoom.setHref(map.getString(Zoom.HREF));
                                    // zoom.setImgSrc(map.getString(Zoom.ViGNETTE));
                                    zoom.setOrder(map.getLong(Zoom.ORDER).intValue());
                                    zoom.setTitle(map.getString(Zoom.TITLE));

                                    zoomContents.add(zoom);
                                }
                            }

                        }
                    }
                }

                if (zoomContents != null && zoomContents.size() > 0) {

                    Collections.sort(zoomContents);

                    ctx.setCurrentDoc(doc);
                    request.setAttribute("doc", doc);
                    request.setAttribute("ctx", ctx);
                    request.setAttribute("dataContent", zoomContents);
                    request.setAttribute("view", view);

                    emptyContent = false;
                }
            }
        }

        if (emptyContent)
            request.setAttribute("osivia.emptyResponse", "1");

    }

    public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {

        String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);
        if (nuxeoPath == null)
            nuxeoPath = "";
        request.setAttribute("nuxeoPath", nuxeoPath);


        request.setAttribute("propertyName", ZoomEditableWindow.ZOOM_LINKS);

        String scope = window.getProperty("osivia.cms.forcePublicationScope");
        request.setAttribute("scope", scope);




    }

    public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res) throws Exception {

        if (request.getParameter("nuxeoPath") != null)
            window.setProperty(Constants.WINDOW_PROP_URI, request.getParameter("nuxeoPath"));



        if (request.getParameter("scope") != null && request.getParameter("scope").length() > 0) {
            window.setProperty("osivia.cms.forcePublicationScope", request.getParameter("scope"));
        } else if (window.getProperty("osivia.cms.forcePublicationScope") != null)
            window.setProperty("osivia.cms.forcePublicationScope", null);



    }

}
