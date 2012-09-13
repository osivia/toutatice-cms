

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="fr.toutatice.portail.api.menubar.MenubarItem"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>


<portlet:defineObjects />

<%

Document doc = (Document) renderRequest.getAttribute("doc");


NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;


List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("pia.menuBar");

MenubarItem item = new MenubarItem("Ouvrir le lien", MenubarItem.ORDER_PORTLET_SPECIFIC, ctx.getLink(doc, "player").getUrl(), null, "portlet-menuitem-contextuallink", "_new");
item.setAjaxDisabled(true);
menuBar.add(item);

%>
