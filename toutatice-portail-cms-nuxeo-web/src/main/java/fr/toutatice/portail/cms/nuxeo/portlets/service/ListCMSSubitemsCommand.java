package fr.toutatice.portail.cms.nuxeo.portlets.service;

import net.sf.json.JSONArray;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;

/**
 * List CMS sub-items command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class ListCMSSubitemsCommand implements INuxeoCommand {

    /** Current parent identifier. */
    private final String parentId;
    /** Live content indicator. */
    private final boolean liveContent;


    /**
     * Constructor.
     * 
     * @param parentId current parent identifier
     * @param liveContent live content indicator
     */
    public ListCMSSubitemsCommand(String parentId, boolean liveContent) {
        super();
        this.parentId = parentId;
        this.liveContent = liveContent;
    }


    /**
     * {@inheritDoc}
     */
    public Object execute(Session nuxeoSession) throws Exception {
        // Fetch live tree with publishing infos
        OperationRequest request = nuxeoSession.newRequest("Fetch.PublishingStatusChildren");
        request.set("documentId", this.parentId);
        request.set("liveStatus", this.liveContent);
        request.setHeader(Constants.HEADER_NX_SCHEMAS, this.getSchemas());
        Blob binariesPublishingInfos = (Blob) request.execute();

        String publishingInfos = IOUtils.toString(binariesPublishingInfos.getStream(), "UTF-8");
        return JSONArray.fromObject(publishingInfos);
    }


    /**
     * {@inheritDoc}
     */
    public String getId() {
        StringBuilder id = new StringBuilder();
        id.append(this.getClass().getCanonicalName());
        id.append("[");
        id.append(this.parentId);
        id.append(";");
        id.append(this.liveContent);
        id.append("]");
        return id.toString();
    }


    /**
     * Utility method used to return schemas.
     * 
     * @return schemas
     */
    private String getSchemas() {
        return "dublincore, common, toutatice";
    }

}
