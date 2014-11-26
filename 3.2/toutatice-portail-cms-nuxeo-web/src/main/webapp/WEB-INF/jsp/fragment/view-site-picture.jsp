

<%@page import="java.net.URLEncoder"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>





<portlet:defineObjects />

<%

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")   ;

String nuxeoPath = (String) renderRequest.getAttribute("nuxeoPath");
String targetPath = (String) renderRequest.getAttribute("targetPath");

%>

<div class="nuxeo-fragment-view-picture">

<% if( nuxeoPath != null) { 
    String src = renderRequest.getContextPath() + "/sitepicture?" + "path=" + URLEncoder.encode(nuxeoPath, "UTF-8") ;
%>
    
<%  if( targetPath != null) { %>    
        <a href="<%= ctx.getCMSLinkByPath(targetPath,null).getUrl()%>">
<%  } %>
        <img src="<%= src %>">
<%  if( targetPath != null) { %>    
        </a>
<%  } %>

<%  } %>

</div>


