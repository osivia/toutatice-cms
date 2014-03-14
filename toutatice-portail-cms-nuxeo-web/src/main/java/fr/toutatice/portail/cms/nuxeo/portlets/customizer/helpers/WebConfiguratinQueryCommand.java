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

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;



/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class WebConfiguratinQueryCommand implements INuxeoCommand {



    /** Le domaine du site courant. */
    private String domainPath;

    /** Le type de configuration cherché. */
    private WebConfigurationType type;

    /** Types de configuration possible. */
    public enum WebConfigurationType {
        CMSNavigationAdapter, CMSPlayer, CMSToWebPathAdapter, extraRequestFilter;
    }
    
    /** Schémas. */
    public final static String basicNavigationSchemas = "dublincore,common,toutatice,webconfiguration";
	
    /**
     * 
     * @param domainPath
     * @param type
     */
    public WebConfiguratinQueryCommand(String domainPath, WebConfigurationType type) {
		super();

        this.domainPath = domainPath;
        this.type = type;
	}
	
	public Object execute(Session session) throws Exception {
		
		OperationRequest request;

		request = session.newRequest("Document.Query");

        String nuxeoRequest = "( ecm:path STARTSWITH '" + domainPath + "'  " + "AND  (wconf:type = '" + type.toString() + "') AND (wconf:enabled=1) )";
		

        request.set("query", "SELECT * FROM Document WHERE " + nuxeoRequest + " ORDER BY wconf:order");
		

		String navigationSchemas = basicNavigationSchemas;

		request.setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas);


        Documents configurations = (Documents) request.execute();
        return configurations;
		

	}

	public String getId() {

        return "WebConfiguratinQueryCommand/" + domainPath + "/" + type.toString();
	};

}
