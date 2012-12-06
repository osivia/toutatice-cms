package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

public interface IFragmentModule {

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception ;

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception;
	
	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res) throws Exception ;

}