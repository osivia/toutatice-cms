package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

public interface ITemplateModule {

	public void doView(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception ;

	public void processAction(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res) throws Exception ;

}