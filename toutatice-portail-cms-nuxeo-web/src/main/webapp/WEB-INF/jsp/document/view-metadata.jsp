<%@ page contentType="text/html; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.StringTokenizer"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Vector"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="javax.portlet.ResourceURL"%>

<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.StringHelper"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.VocabularyHelper"%>
<%@page import="fr.toutatice.portail.api.urls.Link"%>

<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
Document doc = (Document) renderRequest.getAttribute("doc");

String date = "";
date = Formater.formatDate( doc);


String creator = doc.getProperties().getString("dc:creator");
if( creator == null)
	creator = "-";
%>

<div class="nuxeo-docview-metadata" >


	<div style="border-bottom:1px solid #88ABBC;color: #88ABBC;font-weight: bold;">Méta-données </div>
	<span style="font-weight:bold">Auteur : </span><span><%=creator%></span><br/>
	<span style="font-weight:bold">Publication : </span><span><%=date%></span><br/>
<%
String srcVignette = "";
PropertyMap map = doc.getProperties().getMap("ttc:vignette");
if( map != null && map.getString("data") != null)	
	srcVignette = "<img class=\"nuxeo-docview-vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" />";
%>
<%if(srcVignette.length()!=0) {%>
	<span style="font-weight:bold">Vignette : </span><br/><%=srcVignette %>
<%} %>


	
</div>
