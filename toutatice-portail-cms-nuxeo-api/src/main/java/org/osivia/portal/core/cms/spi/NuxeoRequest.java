package org.osivia.portal.core.cms.spi;

import org.osivia.portal.api.cms.service.Request;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

/**
 * The Class NuxeoRequest.
 */
public class NuxeoRequest implements Request {
    
    private final String repositoryName;
    private final INuxeoCommand command;
    private final NuxeoCommandContext commandContext;



    /**
     * Instantiates a new parent request.
     *
     * @param parentId the parent id
     */
    public NuxeoRequest( String repositoryName, NuxeoCommandContext commandContext, INuxeoCommand command) {
        super();
        this.repositoryName = repositoryName;
        this.command = command;
        this.commandContext = commandContext;
    }

    
    @Override
    public String getRepositoryName() {
        return repositoryName;
    }
    
    

    public INuxeoCommand getCommand() {
        return command;
    }
    
    
    
    public NuxeoCommandContext getCommandContext() {
        return commandContext;
    }



}
