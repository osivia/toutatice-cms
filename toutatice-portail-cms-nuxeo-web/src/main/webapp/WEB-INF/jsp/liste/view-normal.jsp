

<%@page import="org.osivia.portal.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>

<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<%@page import="javax.portlet.PortletURL"%>




<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>


<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

Document doc = (Document) renderRequest.getAttribute("doc");
int parite = (Integer) renderRequest.getAttribute("parite");


Link link = ctx.getLink(doc);

String icon = Formater.formatNuxeoIcon(doc);

String username = doc.getProperties().get("dc:creator").toString();
String avatarLink = ctx.getUserAvatar(username).getUrl();


icon = "<img class=\"icon\" src=\""+renderRequest.getContextPath()+icon+"\">";


%>

		<li> <div class="list-bloc">  <%= icon%> <%= Formater.formatLink(link, doc, "title") %><p class="list-date"><%= Formater.formatDateAndTime(doc)%> par <img src="<%=avatarLink%>" class="avatar"/> <%=username%> </p></div></li>	
