

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>





<portlet:defineObjects />

<%

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

Document navigationPictureContainer = (Document) renderRequest.getAttribute("navigationPictureContainer");
String propertyName = (String) renderRequest.getAttribute("propertyName");

%>

<div class="nuxeo-fragment-view-picture">

<% if( navigationPictureContainer != null) { 
 String src = ctx.createFileLink(navigationPictureContainer,propertyName); %>

	<img src="<%= src %>">


<%	} %>

</div>


