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
package fr.toutatice.portail.cms.nuxeo.portlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.services.InjectionData;
import fr.toutatice.portail.cms.nuxeo.services.InjectionService;

/**
 * Injection in LDAP and Nuxeo test portlet.
 *
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class InjectionTestPortlet extends CMSPortlet {

    /** View page path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/injection/view.jsp";

    /** Number of probabilities. */
    private static final int PROBABILITIES_COUNT = 5;

    /** Injection service. */
    private final InjectionService service;


    /**
     * Default constructor.
     */
    public InjectionTestPortlet() {
        super();
        this.service = InjectionService.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Create groups
        List<LdapName> ldapGroupNames = this.createGroups(request);

        // Create users
        this.createUsers(request, ldapGroupNames);

        // Create documents
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        this.createDocuments(request, nuxeoController, ldapGroupNames);
    }


    /**
     * Utility method used to create groups.
     *
     * @param request action request
     * @return LDAP names of groups
     * @throws PortletException
     */
    private List<LdapName> createGroups(ActionRequest request) throws PortletException {
        List<LdapName> ldapNames = null;

        // Request parameters
        int count = Integer.valueOf(request.getParameter("groups-count"));
        int depth = Integer.valueOf(request.getParameter("groups-depth"));

        // Data
        InjectionData data = new InjectionData();
        data.setCount(count);
        data.setDepth(depth);

        try {
            ldapNames = this.service.createGroups(data);
        } catch (NamingException e) {
            throw new PortletException(e);
        }
        return ldapNames;
    }


    /**
     * Utility method used to create users.
     *
     * @param request action request
     * @param ldapGroupNames LDAP names of groups
     * @throws PortletException
     */
    private void createUsers(ActionRequest request, List<LdapName> ldapGroupNames) throws PortletException {
        // Request parameters
        int count = Integer.valueOf(request.getParameter("users-count"));
        List<Float> list = new ArrayList<Float>();
        for (int i = 1; i <= PROBABILITIES_COUNT; i++) {
            String parameterName = "users-probability-" + i;
            String probability = request.getParameter(parameterName);
            if (StringUtils.isNotBlank(probability)) {
                list.add(Float.valueOf(probability));
            }
        }
        float[] probabilities = ArrayUtils.toPrimitive(list.toArray(new Float[list.size()]));

        // Data
        InjectionData data = new InjectionData();
        data.setCount(count);
        data.setProbabilities(probabilities);

        try {
            this.service.createUsers(data, ldapGroupNames);
        } catch (NamingException e) {
            throw new PortletException(e);
        }
    }


    /**
     * Utility method used to create Nuxeo documents.
     *
     * @param request action request
     * @param nuxeoController Nuxeo controller
     * @param ldapGroupNames LDAP names of groups
     * @throws PortletException
     */
    private void createDocuments(ActionRequest request, NuxeoController nuxeoController, List<LdapName> ldapGroupNames) throws PortletException {
        // Request parameters
        String parentPath = request.getParameter("documents-parent");
        String workspacePath = request.getParameter("documents-workspace");
        int count = Integer.valueOf(request.getParameter("documents-count"));
        int notesCount = Integer.valueOf(request.getParameter("documents-notes-count"));
        int depth = Integer.valueOf(request.getParameter("documents-depth"));
        List<Float> list = new ArrayList<Float>();
        for (int i = 1; i <= PROBABILITIES_COUNT; i++) {
            String parameterName = "documents-probability-" + i;
            String probability = request.getParameter(parameterName);
            if (StringUtils.isNotBlank(probability)) {
                list.add(Float.valueOf(probability));
            }
        }
        float[] probabilities = ArrayUtils.toPrimitive(list.toArray(new Float[list.size()]));

        // Data
        InjectionData data = new InjectionData();
        data.setParentPath(parentPath);
        data.setWorkspacePath(workspacePath);
        data.setCount(count);
        data.setNotesCount(notesCount);
        data.setDepth(depth);
        data.setProbabilities(probabilities);

        try {
            this.service.createDocuments(nuxeoController, ldapGroupNames, data);
        } catch (CMSException e) {
            throw new PortletException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_VIEW).include(request, response);
    }

}
