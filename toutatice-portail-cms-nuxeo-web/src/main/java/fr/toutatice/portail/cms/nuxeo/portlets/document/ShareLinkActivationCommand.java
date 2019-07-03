package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cms.IDGenerator;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Activate share link command.
 *
 * @author Jean-Sébastien Steux
 * @see INuxeoCommand
 */
public class ShareLinkActivationCommand implements INuxeoCommand {
    
    private final static String DEFAULT_FORMAT = "default";
    

    /** Parent identifier. */
    private final Document  doc;

    

    /** The link activate indicator. */
    boolean activate;
    
    /**
     * Constructor.
     */
    public ShareLinkActivationCommand(Document doc,  boolean activate) {
        super();
        this.doc = doc;
        this.activate = activate;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);
         
        PropertyMap properties = new PropertyMap();    
        
        if(activate) {
            // Generate link ID
            String shareId = doc.getProperties().getString("rshr:linkId") ;
            if( StringUtils.isEmpty(shareId)) {
                shareId = IDGenerator.generateId();
                properties.set( "rshr:linkId", shareId);
            }
            
            // if not target, the link is considered as public
            PropertyList targets = doc.getProperties().getList("rshr:targets") ;
            if( targets == null || targets.size() == 0) {
                properties.set("rshr:publicLink", true);
            }
         }
        
        properties.set( "rshr:enabledLink", activate);
        documentService.update(doc, properties);
        

  
        return null;
    }





    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(" : ");
        builder.append(this.doc.getPath());

        return builder.toString();
    }

}
