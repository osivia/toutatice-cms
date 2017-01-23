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
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.domain.ITemplateModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.SliderListEditableWindow;


/**
 * @author david chevrier
 *
 */
public class SliderTemplateModule implements ITemplateModule {

    /** Type of document displayed in slider. */
    public static final String SLIDER_DOC_TYPE = "docType";
    /** Temporisation of slider */
    public static final String SLIDER_TIMER = "timer";

    /**
     * Constructor.
     */
    public SliderTemplateModule() {
        super();
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext, PortalWindow window, RenderRequest request, RenderResponse response)
            throws PortletException {
        /* Value filled for EditableWindows */
        String docType = window.getProperty(SLIDER_DOC_TYPE);
        if(StringUtils.isBlank(docType)){
            docType = SliderListEditableWindow.ALL_DOC_TYPES;
        }
        request.setAttribute(SLIDER_DOC_TYPE, docType);
        request.setAttribute(SLIDER_TIMER, window.getProperty(SLIDER_TIMER));
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    public void processAction(PortalControllerContext portalControllerContext, PortalWindow window, ActionRequest request, ActionResponse response)
            throws PortletException {
        // Nothing
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response, PortalControllerContext portalControllerContext) throws PortletException {
        // Nothing
    }

}
