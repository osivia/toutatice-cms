

<%@page import="java.util.Map"%>
<%@page import="org.osivia.portal.core.cms.CMSItem"%>
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

String srcVignette = "";
PropertyMap map = doc.getProperties().getMap("ttc:vignette");

if( map != null && map.getString("data") != null)	
	srcVignette = "<div class=\"vignette\"><img class=\"vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" /></div>";
else
	srcVignette = "<div class=\"vignette-vide\"> </div>";


Map<String,String> docCfg = ctx.getDocumentConfiguration( doc);
	
%>

		<li class="item<%=parite%>">
			<%=srcVignette%>
			<%= Formater.formatLink(link, doc) %>
			
			
			<p class="description"><%= Formater.formatDescription(doc)%></p>


			<p style="text-align: right;" class="action-link">
				<a title="+ d'infos" class="" href="<%= ctx.getLink(doc,"detailedView").getUrl() %>">Vue détaillée</a>
			</p>
			
	
			<div class="separateur"></div>
		</li>
		