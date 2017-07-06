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

import java.util.HashMap;

import org.jboss.portal.core.controller.ControllerContext;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.NotificationsType;

/**
 * @author loic
 *
 */
public class UnsynchronizeCommand extends EcmCommand {

	private INotificationsService notifService;
	
	private IInternationalizationService itlzService;
	
	/**
	 * @param notifService
	 * @param itlzService
	 */
	public UnsynchronizeCommand(INotificationsService notifService,
			IInternationalizationService itlzService) {

		
        super(EcmCommonCommands.unsynchronizeFolder.toString(), ReloadAfterCommandStrategy.REFRESH_NAVIGATION, "NuxeoDrive.SetSynchronization",
                new HashMap<String, Object>());
		getRealCommandParameters().put("enable", Boolean.FALSE);
		
		this.itlzService = itlzService;
		this.notifService = notifService;

		
	}

	/* (non-Javadoc)
	 * @see org.osivia.portal.core.ecm.GenericEcmCommand#notifyAfterCommand()
	 */
	@Override
	public void notifyAfterCommand(ControllerContext ctx) {
		String success = itlzService.getString("SUCCESS_MESSAGE_UNSYNCHRONIZE", ctx.getServerInvocation().getRequest().getLocale());
		
		PortalControllerContext pcc = new PortalControllerContext(ctx);
		notifService.addSimpleNotification(pcc, success, NotificationsType.SUCCESS);
	}

}
