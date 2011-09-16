package fr.toutatice.portail.cms.nuxeo.core;

import javax.portlet.PortletException;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;

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
