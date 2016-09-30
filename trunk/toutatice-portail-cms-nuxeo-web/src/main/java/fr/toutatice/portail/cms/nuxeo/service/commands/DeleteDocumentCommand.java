/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.service.commands;

import java.util.HashMap;

import org.jboss.portal.core.controller.ControllerContext;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.core.cms.ICMSService;


/**
 * @author david
 *
 */
public class DeleteDocumentCommand extends EcmCommand {
    
    /** Notification service. */
    private INotificationsService notifService;
    /** Internationalization service. */
    private IInternationalizationService itlzService;
    
    /**
     * @param commandName
     * @param strategy
     * @param realCommand
     * @param realCommandParameters
     */
    public DeleteDocumentCommand(INotificationsService notifService, IInternationalizationService itlzService) {
        super(EcmCommonCommands.deleteDocument.toString(), ReloadAfterCommandStrategy.refreshNavigation, "Document.Delete",
                new HashMap<String, Object>());
        this.itlzService = itlzService;
        this.notifService = notifService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAfterCommand(ControllerContext controllerContext) {
        String success = itlzService.getString("SUCCESS_MESSAGE_DRAFT_DELETED", controllerContext.getServerInvocation().getRequest().getLocale());

        PortalControllerContext pcc = new PortalControllerContext(controllerContext);
        notifService.addSimpleNotification(pcc, success, NotificationsType.SUCCESS);
    }

}
