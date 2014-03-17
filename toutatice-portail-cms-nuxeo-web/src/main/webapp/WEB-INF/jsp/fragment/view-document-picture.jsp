

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>





<portlet:defineObjects />

<%

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")   ;

Document pictureDocument = (Document) renderRequest.getAttribute("pictureDocument");
String propertyName = (String) renderRequest.getAttribute("propertyName");
String targetPath = (String) renderRequest.getAttribute("targetPath");

%>

<div class="nuxeo-fragment-view-picture">

<% if( pictureDocument != null) { 
    
    String src = ctx.createFileLink(pictureDocument,propertyName); %>
    
<%  if( targetPath != null) { %>    
        <a href="<%= ctx.getCMSLinkByPath(targetPath,null).getUrl()%>">
<%  } %>
        <img src="<%= src %>">
<%  if( targetPath != null) { %>    
        </a>
<%  } %>

<%  } %>

</div>


