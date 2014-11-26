/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
package fr.toutatice.portail.cms.nuxeo.api;

import javax.portlet.PortletException;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;

/**
 * The Class PortletErrorHandler.
 * 
 * Displays nuxeo user messages
 */
public  class PortletErrorHandler {
	
	public static void handleGenericErrors (RenderResponse response, NuxeoException e) throws PortletException	 {
		try	{
		if( e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN)	{
			response.setContentType("text/html");
			response.getWriter().print("<h2>Accès interdit</h2>");
			response.getWriter().close();

		} else if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND)	{
			response.setContentType("text/html");
			response.getWriter().print("<h2>Document inexistant</h2>");
			response.getWriter().close();

		} else if (e.getErrorCode() == NuxeoException.ERROR_UNAVAILAIBLE)	{
		    
		    if( e.getCause() != null){
		        throw new PortletException( e);
		    }
		    
			response.setContentType("text/html");
			response.getWriter().print("<h2>Service indisponible</h2>");
			response.getWriter().close();

		} else
			throw new PortletException( e);
		} catch(Exception e2){
			throw new PortletException( e2);
		}
			
		}


}
