<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="java.text.Format"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="org.osivia.portal.api.urls.Link"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>

<%@ page contentType="text/plain; charset=UTF-8"%>

<portlet:defineObjects />

<%
	NuxeoController ctx = (NuxeoController) renderRequest
			.getAttribute("ctx");

	Document doc = (Document) renderRequest.getAttribute("doc");
	int parite = (Integer) renderRequest.getAttribute("parite");

	
	String srcVignette = "";
	PropertyMap map = doc.getProperties().getMap("ttc:vignette");

	if( map != null && map.getString("data") != null)	
		srcVignette = "<div class=\"vignette\"><img class=\"vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" /></div>";
	else
		srcVignette = "<div class=\"vignette-vide\"> </div>";


	String title = Formater.formatTitle(doc);
	String extrait = Formater.formatExtrait(doc);


%>


<li class="item<%=parite%>">
	
	<div class="titreactu"><%=title%></div>

	<%=srcVignette%>
	<div class="extrait"><%=extrait%></div>
	
	<div class="separateur">&nbsp;</div>
</li>