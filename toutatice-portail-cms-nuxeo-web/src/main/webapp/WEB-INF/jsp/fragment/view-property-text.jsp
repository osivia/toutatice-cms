
<%@ page contentType="text/plain; charset=UTF-8"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>



<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>


<portlet:defineObjects />

<%
Document doc = (Document) renderRequest.getAttribute("doc");

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

String dataContent = (String) renderRequest.getAttribute("dataContent");

%>


<% if(doc != null && dataContent != null) { %>
	
<%= dataContent %>


<%	}	%>





