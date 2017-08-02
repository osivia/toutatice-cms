package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Get user subscriptions Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class GetUserSubscriptionsCommand implements INuxeoCommand {

    /** Operation request identifier. */
    private static final String REQUEST_ID = "Notification.GetUserSubscriptions";


    /**
     * Constructor.
     */
    public GetUserSubscriptionsCommand() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Operation request
        OperationRequest request = nuxeoSession.newRequest(REQUEST_ID);

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getClass().getName();
    }

}
