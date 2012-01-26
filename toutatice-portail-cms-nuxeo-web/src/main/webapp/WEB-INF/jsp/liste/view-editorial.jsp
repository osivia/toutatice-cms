

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

String srcVignette = "";
PropertyMap map = doc.getProperties().getMap("ttc:vignette");

if( map != null && map.getString("data") != null)	
	srcVignette = "<div class=\"vignette\"><img class=\"vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" /></div>";
else
	srcVignette = "<div class=\"vignette-vide\"> </div>";


%>

		<li class="item<%=parite%>">
			<%=srcVignette%>
			<a class="title" <%=target%> href="<%=url%>"><%=doc.getTitle()%></a>
			<p class="description"><%= Formater.formatDescription(doc)%></p>
			<div class="separateur"></div>
		</li>
		