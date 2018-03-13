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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import javax.portlet.PortletContext;

import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Ce customizer permet de définir :
 * 
 * de nouveaux templates de listes
 * le schéma du moteur de recherche
 * les templates de contenu
 * 
 * Le template d'affichage par défaut est WEB-INF/jsp/liste/view-[nom-du-template].jsp
 * 
 * @author jeanseb
 * @see DefaultCMSCustomizer
 */
public class CMSCustomizer extends DefaultCMSCustomizer {

    /**
     * Constructor.
     * 
     * @param portletContext portlet context
     * @param cmsService CMS service
     */
    public CMSCustomizer(PortletContext portletContext, CMSService cmsService) {
        super(portletContext, cmsService);
    }

}
