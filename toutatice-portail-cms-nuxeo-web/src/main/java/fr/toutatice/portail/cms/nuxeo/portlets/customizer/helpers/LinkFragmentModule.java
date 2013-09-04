/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

/**
 *
 */
public class LinkFragmentModule implements IFragmentModule {

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#
     * injectViewAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController,
     * org.osivia.portal.api.windows.PortalWindow, javax.portlet.PortletRequest,
     * javax.portlet.RenderResponse)
     */
    public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {

        String linkName = window.getProperty("osivia.linkName");
        String docPathForLink = window.getProperty("osivia.docPathForLink");
        boolean emptyContent = true;

        if (StringUtils.isNotEmpty(docPathForLink)) {

            docPathForLink = ctx.getComputedPath(docPathForLink);

            /* Version live en cas de lien Nuxeo */
            if ("1".equals(window.getProperty("osivia.isNuxeoLink")))
                ctx.setDisplayLiveVersion("1");
            Document doc = ctx.fetchDocument(docPathForLink);

            if (doc != null) {

                ctx.setCurrentDoc(doc);
                request.setAttribute("doc", doc);
                request.setAttribute("ctx", ctx);

                request.setAttribute("isNuxeoLink", window.getProperty("osivia.isNuxeoLink"));

                String docProperty = (String) doc.getProperties().get(linkName);
                if (docProperty != null)
                    linkName = docProperty;
                else {
                    String linkNameParam = window.getProperty("osivia.linkName");
                    if (linkNameParam != null)
                        linkName = linkNameParam;
                    else
                        linkName = "";
                }
                request.setAttribute("linkName", linkName);

                String cssLinkClass = "";
                String cssLinkClassParam = window.getProperty("osivia.cssLinkClass");
                if (cssLinkClassParam != null)
                    cssLinkClass = cssLinkClassParam;
                request.setAttribute("cssLinkClass", cssLinkClass);

                emptyContent = false;
            }

        }

        if (emptyContent)
            request.setAttribute("osivia.emptyResponse", "1");

    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#
     * injectAdminAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController,
     * org.osivia.portal.api.windows.PortalWindow, javax.portlet.PortletRequest,
     * javax.portlet.RenderResponse)
     */
    public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {

        String linkName = window.getProperty("osivia.linkName");
        if (linkName == null)
            linkName = "";
        request.setAttribute("linkName", linkName);

        String docPathForLink = window.getProperty("osivia.docPathForLink");
        if (docPathForLink == null)
            docPathForLink = "";
        request.setAttribute("docPathForLink", docPathForLink);

        String cssLinkClass = window.getProperty("osivia.cssLinkClass");
        if (cssLinkClass == null)
            cssLinkClass = "";
        request.setAttribute("cssLinkClass", cssLinkClass);

        String isNuxeoLink = window.getProperty("osivia.isNuxeoLink");
        if (isNuxeoLink == null)
            isNuxeoLink = "0";
        request.setAttribute("isNuxeoLink", isNuxeoLink);

    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#
     * processAdminAttributes
     * (fr.toutatice.portail.cms.nuxeo.api.NuxeoController,
     * org.osivia.portal.api.windows.PortalWindow, javax.portlet.ActionRequest,
     * javax.portlet.ActionResponse)
     */
    public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res) throws Exception {

        if (request.getParameter("linkName") != null) {
            if (request.getParameter("linkName").length() > 0)
                window.setProperty("osivia.linkName", request.getParameter("linkName"));
            else if (window.getProperty("osivia.linkName") != null)
                window.setProperty("osivia.linkName", null);
        }

        if (request.getParameter("docPathForLink") != null) {
            if (request.getParameter("docPathForLink").length() > 0)
                window.setProperty("osivia.docPathForLink", request.getParameter("docPathForLink"));
            else if (window.getProperty("osivia.docPathForLink") != null)
                window.setProperty("osivia.docPathForLink", null);
        }

        if ("1".equals(request.getParameter("isNuxeoLink")))
            window.setProperty("osivia.isNuxeoLink", "1");
        else if (window.getProperty("osivia.isNuxeoLink") != null)
            window.setProperty("osivia.isNuxeoLink", null);

        if (request.getParameter("cssLinkClass") != null) {
            if (request.getParameter("cssLinkClass").length() > 0)
                window.setProperty("osivia.cssLinkClass", request.getParameter("cssLinkClass"));
            else if (window.getProperty("osivia.cssLinkClass") != null)
                window.setProperty("osivia.cssLinkClass", null);
        }

    }

}
