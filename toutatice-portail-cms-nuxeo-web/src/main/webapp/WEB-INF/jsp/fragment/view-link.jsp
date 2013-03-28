<%@ page contentType="text/plain; charset=UTF-8"%>

<%@ page import="org.osivia.portal.api.urls.Link"%>
<%@ page import="org.osivia.portal.api.urls.IPortalUrlFactory"%>
<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>
<%@ page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@ page import="javax.portlet.PortletURL"%>
<%@ page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx");

Document doc = (Document) renderRequest.getAttribute("doc");
String linkName = (String) renderRequest.getAttribute("linkName");
String cssLinkClass = (String) renderRequest.getAttribute("cssLinkClass");
String isNuxeoLinkParam = (String) renderRequest.getAttribute("isNuxeoLink");
boolean isNuxeoLink = isNuxeoLinkParam != null && "1".equals(isNuxeoLinkParam);

if(doc != null){%>
	
	<% if(isNuxeoLink) { %>		
		<a target="_blank" title="<%= linkName %>" href="<%= ctx.getLink(doc,"nuxeo-link").getUrl() %>">
			<span class="<%= cssLinkClass %>"><%= linkName %>&nbsp;</span>
		</a>
	<% } else { %>
		<a class="<%= cssLinkClass %>" href="<%= ctx.getLink(doc).getUrl() %>">
			<span class="<%= cssLinkClass %>"><%= linkName %>&nbsp;</span>
		</a>
	<% } %>
	<div class="separateur"></div>

<% } %>




