

<%@page import="java.text.Format"%>
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
NuxeoController ctx = (NuxeoController) request.getAttribute("ctx")	;

Document doc = (Document) request.getAttribute("doc");
int parite = ((Long) request.getAttribute("parite")).intValue();


Link link = ctx.getLink(doc);

String icon = Formater.formatNuxeoIcon(doc);
icon = "<img class=\"icon\" src=\""+request.getContextPath()+icon+"\">";

String detail = "";
detail = Formater.formatDate( doc);

String size = Formater.formatSize( doc);
if( size.length() > 0)
	detail += " - " + size;



%>

		<li class="item<%=parite%>">  <%= icon%> <%= Formater.formatLink(link, doc) %> <p class="description"><%= Formater.formatDescription(doc)%></p> <p class="detail"><%=detail%></p><div class="separateur"></div></li>
