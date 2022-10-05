package fr.toutatice.portail.cms.nuxeo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.portal.theme.ThemeConstants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cms.repository.model.shared.RepositoryDocument;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Update document Nuxeo command.
 *
 * @author Jean-Sébastien Steux
 * @see INuxeoCommand
 */

public class UpdateDocumentCommand implements INuxeoCommand {


    private RepositoryDocument document;

    private List<String> allowedProperties = Arrays.asList("ttc:pageTemplate");


    /**
     * @param document
     */
    public UpdateDocumentCommand(RepositoryDocument document) {
        super();
        this.document = document;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);
        Document nxDocument = (Document) document.getNativeItem();

        PropertyMap modifiedProperties = new PropertyMap();
        List<String> removedProperties = new ArrayList<>();
        PropertyMap oldProperties = nxDocument.getProperties();

        // New or modified properties
        Map<String, Object> properties = document.getProperties();
        for (String key : properties.keySet()) {
            if (allowedProperties.contains(key))
                if (properties.get(key) instanceof String) {
                    if (oldProperties.get(key) == null || !oldProperties.get(key).equals(properties.get(key))) {
                        modifiedProperties.set(key, (String) properties.get(key));
                    }
                }
        }

        // Removed properties
        for (String key : oldProperties.getKeys()) {
            if (allowedProperties.contains(key))
                if (oldProperties.get(key) instanceof String) {
                    if (modifiedProperties.get(key) == null) {
                        removedProperties.add(key);
                    }
                }
        }


        documentService.update(nxDocument, modifiedProperties);


        for (String removedProperty : removedProperties)
            documentService.removeProperty(nxDocument, removedProperty);


        return document;
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
        builder.append(document);
        return builder.toString();
    }

}
