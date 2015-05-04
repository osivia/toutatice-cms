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

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;


/**
 * Return all the navigation items.
 * 
 * @author Jean-Sébastien Steux
 * @see INuxeoCommand
 */
public class WebConfigurationQueryCommand implements INuxeoCommand {

    /** Schémas. */
    public static final String BASIC_NAVIGATION_SCHEMAS = "dublincore, common, toutatice, webconfiguration";


    /** Le domaine du site courant. */
    private String domainPath;

    /** Le type de configuration cherché. */
    private WebConfigurationType type;

    // FIXME (dch): better to work with domain id.
    /**
     * Constructor.
     * 
     * @param domainPath current site domain path
     * @param type configuration type
     */
    public WebConfigurationQueryCommand(String domainPath, WebConfigurationType type) {
        super();

        this.domainPath = domainPath;
        this.type = type;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        OperationRequest request;

        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_60)) {
            request = session.newRequest("Context.GetWebConfigurations");
            request.set("domainPath", this.domainPath);
            request.set("confType", this.type.getTypeName());

        } else {
            request = session.newRequest("Document.Query");

            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * FROM Document WHERE ( ecm:path STARTSWITH '");
            builder.append(this.domainPath);
            builder.append("' AND (wconf:type = '");
            builder.append(this.type.getTypeName());
            builder.append("') AND (wconf:enabled=1)) ORDER BY wconf:order");

            request.set("query", builder.toString());
        }

        request.setHeader(Constants.HEADER_NX_SCHEMAS, BASIC_NAVIGATION_SCHEMAS);

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "WebConfigurationQueryCommand/" + this.domainPath + "/" + this.type.toString();
    };


    /**
     * Configuration types enumeration.
     * 
     * @author Jean-Sébastien Steux
     */
    public enum WebConfigurationType {

        /** CMS Navigation adapter. */
        CMS_NAVIGATION_ADAPTER("CMSNavigationAdapter"),
        /** CMS player. */
        CMS_PLAYER("CMSPlayer"),
        /** CMS to web path adapter. */
        CMS_TO_WEB_PATH_ADAPTER("CMSToWebPathAdapter"),
        /** Extra request filter. */
        EXTRA_REQUEST_FILTER("extraRequestFilter"),
        /** Region layout. */
        REGION_LAYOUT("regionlayout");


        /** Type name. */
        private final String typeName;


        /**
         * Constructor.
         * 
         * @param typeName type name
         */
        private WebConfigurationType(String typeName) {
            this.typeName = typeName;
        }


        /**
         * Getter for typeName.
         * 
         * @return the typeName
         */
        public String getTypeName() {
            return this.typeName;
        }

    }

}
