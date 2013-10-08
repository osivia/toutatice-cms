package fr.toutatice.portail.cms.nuxeo.portlets;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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
        List<LdapName> ldapNames = null;
        InjectionData groupsInjectionData = new InjectionData();
        groupsInjectionData.setCount(5);
        groupsInjectionData.setDepth(3);
        try {
            ldapNames = this.service.createGroups(groupsInjectionData);
        } catch (NamingException e) {
            throw new PortletException(e);
        }

        // Create documents
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        InjectionData documentsInjectionData = new InjectionData();
        documentsInjectionData.setParentPath("/default-domain/Diffusion");
        documentsInjectionData.setWorkspacePath("/default-domain/workspaces/workspace");
        documentsInjectionData.setCount(2);
        documentsInjectionData.setNotesCount(4);
        documentsInjectionData.setDepth(3);
        documentsInjectionData.setProbabilities(new float[]{0.2f, 0.05f, 0.01f});
        try {
            this.service.createDocuments(nuxeoController, ldapNames, documentsInjectionData);
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
