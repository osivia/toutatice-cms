

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>





<portlet:defineObjects />

<%

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

String navigationPageTemplate = (String) renderRequest.getAttribute("navigationPageTemplate");

%>


<% if( navigationPageTemplate != null) { %>

<%= navigationPageTemplate %>

<%	}	%>




