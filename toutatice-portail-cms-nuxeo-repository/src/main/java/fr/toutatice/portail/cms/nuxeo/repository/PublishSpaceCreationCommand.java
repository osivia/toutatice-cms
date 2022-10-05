package fr.toutatice.portail.cms.nuxeo.repository;

import java.util.Date;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Publish space creation Nuxeo command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */

public class PublishSpaceCreationCommand implements INuxeoCommand {


 
    private String webId;

    /**
     * Constructor.
     *
     * @param form workspace creation form
     * @param items workspace default taskbar items
     * @param bundle internationalization bundle
     */
    public PublishSpaceCreationCommand( String webId) {
        super();
        this.webId = webId;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        // Workspace
        Document workspace = this.createWorkspace(nuxeoSession, documentService);


        return workspace;
    }


    /**
     * Create workspace document.
     *
     * @param nuxeoSession Nuxeo session
     * @param documentService document service
     * @return document
     * @throws Exception
     */
    private Document createWorkspace(Session nuxeoSession, DocumentService documentService) throws Exception {
        // Workspaces container
        DocRef container = new DocRef("/default-domain");

        // Properties
        PropertyMap properties = new PropertyMap();
        properties.set("ttc:webid", webId);


        // Name
        String name = webId;

        // Workspace creation
        Document workspace = documentService.createDocument(container, "PortalSite", name, properties);

        return workspace;
    }


   
   


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
    	StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(new Date().getTime());
        builder.append("/");
        builder.append(webId);
        return builder.toString();    }

}
