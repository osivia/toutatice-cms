

<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.osivia.portal.api.urls.Link"%>
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


Link link = ctx.getLink(doc);
String url = link.getUrl();
String target = Formater.formatTarget(link);

String srcVignette = "";
PropertyMap map = doc.getProperties().getMap("ttc:vignette");

if( map != null && map.getString("data") != null)	
	srcVignette = "<div class=\"vignette\"><img class=\"vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" /></div>";
else
	srcVignette = "<div class=\"vignette-vide\"> </div>";

String linkClassName = "";

if( "File".equals(doc.getType()))	
	linkClassName = " download";
	
if(	link.isExternal())
	linkClassName = " external";


ResourceURL resourceURL = renderResponse.createResourceURL();
resourceURL.setResourceID("zoom");
resourceURL.setParameter("docId", doc.getId());

%>

		<li class="item<%=parite%>">
			<%=srcVignette%>
			<a class="title<%= linkClassName%>" <%=target%> href="<%=url%>"><%=doc.getTitle()%></a>
			
			
			<p class="description"><%= Formater.formatDescription(doc)%></p>

			<p style="text-align: right;" class="action-link">
				<a title="+ d'infos" class="fancyframe" href="<%= resourceURL %>">+ d'infos</a>
			</p>
	
			<div class="separateur"></div>
		</li>
		