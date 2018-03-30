package fr.toutatice.portail.cms.nuxeo.portlets.rename;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * Update the dc:title of a document
 *
 * @author Dorian Licois
 */
public class RenameCommand implements INuxeoCommand {

    /** document */
    private final Document document;

    /** newTitle */
    private final String newTitle;

    public RenameCommand(Document document, String newTitle) {
        this.document = document;
        this.newTitle = newTitle;
    }

    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        DocumentService documentService = new DocumentService(nuxeoSession);
        return documentService.setProperty(document, "dc:title", newTitle);
    }

    @Override
    public String getId() {
        return "RenameCommand/" + document + "/" + newTitle;
    }

}
