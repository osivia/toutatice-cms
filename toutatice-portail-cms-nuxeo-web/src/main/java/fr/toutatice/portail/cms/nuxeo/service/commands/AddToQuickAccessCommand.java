/*
 * (C) Copyright 2015 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.service.commands;

import org.jboss.portal.core.controller.ControllerContext;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.NotificationsType;

/**
 * @author jbarberet
 *
 */
public class AddToQuickAccessCommand extends EcmCommand {

	private INotificationsService notifService;
	
	private IInternationalizationService itlzService;
	
	/**
	 * @param notifService
	 * @param itlzService
	 */
	public AddToQuickAccessCommand(INotificationsService notifService,
			IInternationalizationService itlzService) {
		
        super(EcmCommonCommands.addToQuickAccess.toString(), ReloadAfterCommandStrategy.REFRESH_PAGE, "Document.AddToQuickAccess", null);
		
		this.itlzService = itlzService;
		this.notifService = notifService;

	}

	/* (non-Javadoc)
	 * @see org.osivia.portal.core.ecm.GenericEcmCommand#notifyAfterCommand()
	 */
	@Override
	public void notifyAfterCommand(ControllerContext ctx) {
		String success = itlzService.getString("SUCCESS_MESSAGE_ADD_TO_QUICKACCESS", ctx.getServerInvocation().getRequest().getLocale());
		
		PortalControllerContext pcc = new PortalControllerContext(ctx);
		notifService.addSimpleNotification(pcc, success, NotificationsType.SUCCESS);
	}


}
