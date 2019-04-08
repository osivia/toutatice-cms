package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Inline edition Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class InlineEditionCommand implements INuxeoCommand {

    /** Document path. */
    private final String path;
    /** Property. */
    private final String property;
    /** Values. */
    private final String[] values;


    /**
     * Constructor.
     * 
     * @param path document path
     * @param property property
     * @param values values
     */
    public InlineEditionCommand(String path, String property, String[] values) {
        super();
        this.path = path;
        this.property = property;
        this.values = values;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);
        
        // Document reference
        DocRef reference = new DocRef(this.path);

        // Properties
        PropertyMap properties = new PropertyMap(1);
        properties.set(this.property, StringUtils.trimToNull(StringUtils.join(this.values, ",")));

        return documentService.update(reference, properties);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }

}
