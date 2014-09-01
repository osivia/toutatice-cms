package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Document.AddComplexProperty Nuxeo command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class DocumentAddComplexPropertyCommand implements INuxeoCommand {

    /** Command input document. */
    private final Document inputDocument;
    /** Property schema. */
    private final String schema;
    /** Property added value. */
    private final Map<String, String> value;


    /**
     * Constructor.
     *
     * @param inputDocument command input document
     * @param schema property schema
     * @param value property added value
     */
    public DocumentAddComplexPropertyCommand(Document inputDocument, String schema, Map<String, String> value) {
        super();
        this.inputDocument = inputDocument;
        this.schema = schema;
        this.value = value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Serialized value
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Entry<String, String> entry : this.value.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append("\n");
            }
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }

        // Request
        OperationRequest request = nuxeoSession.newRequest("Document.AddComplexProperty");
        request.setInput(this.inputDocument);
        request.set("schema", this.schema);
        request.set("value", builder.toString());

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(" : ");
        builder.append(this.schema);

        return builder.toString();
    }

}
