

<%@page import="fr.toutatice.portail.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>

<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<%@page import="javax.portlet.PortletURL"%>




<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>


<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

Document doc = (Document) renderRequest.getAttribute("doc");
int parite = (Integer) renderRequest.getAttribute("parite");


Link link = ctx.getDirectLink(doc);
String url = link.getUrl();
String target = Formater.formatTarget(link);
String icon = Formater.formatNuxeoIcon(doc);

icon = "<img class=\"icon\" src=\""+renderRequest.getContextPath()+icon+"\">";


%>

		<li> <div class="list-bloc">  <%= icon%> <a class="title" <%=target%> href="<%=url%>"><span><%=doc.getTitle()%></span> </a><p class="list-date"><%= Formater.formatDateAndTime(doc)%></p></div></li>	
