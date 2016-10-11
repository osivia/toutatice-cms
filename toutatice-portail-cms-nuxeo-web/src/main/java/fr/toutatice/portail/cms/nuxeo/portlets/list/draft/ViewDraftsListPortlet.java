/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.list.draft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ListCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet;


/**
 * @author david
 *
 */
public class ViewDraftsListPortlet extends ViewListPortlet {
    
    /** Schemas. */
    protected static final String DRAFTS_LIST_SCHEMAS = DefaultCMSCustomizer.DEFAULT_SCHEMAS.concat(", ")
            .concat(DocumentConstants.DRAFT_SCHEMA);
    /** Drafts list template. */
    protected static final String DRAFTS_LIST_TEMPLATE = "drafts";
    /** Drafts query. */
    // FIXME: build userWorkspace path instaed of hard coded
    public static final String DRAFTS_QUERY_WHERE_CLAUSE = " ecm:mixinType = 'OttcDraft' and dc:lastContributor = '%s'"
            .concat(" and ottcDft:checkoutParentId = '%s'");
    
    /**
     * Constructor.
     */
    public ViewDraftsListPortlet() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);
            // Bundle
            IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                    IInternationalizationService.MBEAN_NAME);
            IBundleFactory bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
            Bundle bundle = bundleFactory.getBundle(request.getLocale());

            String folderPath = window.getProperty("osivia.drafts.folderWebId");
            
            String query = String.format(ViewDraftsListPortlet.DRAFTS_QUERY_WHERE_CLAUSE, request.getRemoteUser(), folderPath);
            int pageSize = Integer.valueOf(CommandConstants.PAGE_PROVIDER_UNLIMITED_MAX_RESULTS).intValue();

            INuxeoCommand command = new ListCommand(query, String.valueOf(NuxeoQueryFilterContext.STATE_LIVE), 0, pageSize, DRAFTS_LIST_SCHEMAS, null, true);
            Documents draftDocs = (Documents) nuxeoController.executeNuxeoCommand(command);

            // Result list
            List<DocumentDTO> draftDocsDTO = new ArrayList<DocumentDTO>(draftDocs.size());
            for (Document document : draftDocs) {
                DocumentDTO documentDTO = DocumentDAO.getInstance().toDTO(document);
                draftDocsDTO.add(documentDTO);
            }
            request.setAttribute("documents", draftDocsDTO);
            
            // Generic properties
            response.setTitle(bundle.getString("DRAFTS_PORTLET_TITLE"));
            window.setProperty("osivia.bootstrapPanelStyle", "true");
            // Template
            request.setAttribute("style", DRAFTS_LIST_TEMPLATE);

        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (Exception e) {
            throw new PortletException(e);
        }

        response.setContentType("text/html");


        PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(PATH_VIEW);
        dispatcher.include(request, response);
    }

}
