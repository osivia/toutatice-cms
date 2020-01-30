package fr.toutatice.portail.cms.nuxeo.services;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.transaction.ITransactionResource;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandServiceFactory;

public class TransactionResource implements ITransactionResource {

    private Object internalTransaction;
    private NuxeoCommandContext nuxeoCtx;


    /**
     * Getter for internalTransaction.
     * 
     * @return the internalTransaction
     */
    public Object getInternalTransaction() {
        return internalTransaction;
    }


    public TransactionResource(NuxeoCommandContext nuxeoCtx, Object internalTransaction) {
        super();
        this.internalTransaction = internalTransaction;
        this.nuxeoCtx = nuxeoCtx;
    }

    @Override
    public void commit() {

        NuxeoController nuxeoController = getNuxeoController();
        nuxeoController.executeNuxeoCommand(new EndTransactionCommand((String) getInternalTransaction(), true));
    }


    private NuxeoController getNuxeoController() {
        NuxeoController nuxeoController = new NuxeoController(nuxeoCtx.getPortletContext());
        
        
        ServerInvocation invocation = this.nuxeoCtx.getServerInvocation();


        if (invocation == null) {
            ControllerContext controllerCtx = this.nuxeoCtx.getControlerContext();
            if( controllerCtx != null)
                invocation = controllerCtx.getServerInvocation();
            else    {
                nuxeoController.setServletRequest((HttpServletRequest) this.nuxeoCtx.getRequest());
            }
        }


        if (invocation != null) {
            nuxeoController.setServletRequest(invocation.getServerContext().getClientRequest());

        }

    
    return nuxeoController;
    }

    @Override
    public void rollback() {

        NuxeoController nuxeoController = getNuxeoController();
        nuxeoController.executeNuxeoCommand(new EndTransactionCommand((String) getInternalTransaction(), false));


    }

}
