
<%@page import="org.osivia.portal.api.Constants"%>
<%@ page contentType="text/html; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.lang.Math"%>
<%@page import="java.lang.Double"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="javax.portlet.ResourceURL"%>

<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.StringHelper"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="org.osivia.portal.api.menubar.MenubarItem"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer"%>

<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) request.getAttribute("ctx");
Document doc = (Document) request.getAttribute("doc");
String lien = ctx.getLink(doc, CMSCustomizer.TEMPLATE_DOWNLOAD).getUrl();

List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);
menuBar.add(new MenubarItem("DOWNLOAD", "Télécharger...", 20, lien, null, "portlet-menuitem download", ""));

PropertyMap map = doc.getProperties().getMap("file:content");
if(map != null && !map.isEmpty()){
	String nom = map.getString("name");
	String taille = map.getString("length");
	double tailleKo = 0;
	if(taille != null)
	{
		tailleKo = Math.floor(Double.parseDouble(taille)/1024);
	}

    String mimeType = map.getString("mime-type");

	String icon = Formater.formatNuxeoIcon(doc);
	icon = "<img alt=\""+mimeType+"\" class=\"icon\" src=\""+renderRequest.getContextPath()+icon+"\">";
	%>
	
	<div class="nuxeo-docview-normal-view">
		<div class="nuxeo-docview-description">
			<%=icon%> <a href="<%=lien%>"><%=nom%></a> (<%=tailleKo%> Koctets)
		</div>
	</div>
	
<% } %>


