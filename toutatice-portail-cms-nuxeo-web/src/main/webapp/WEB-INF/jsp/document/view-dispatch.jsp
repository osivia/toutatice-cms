
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>



<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Blob"%>


<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.StringHelper"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>

<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%><portlet:defineObjects />

<%
Document doc = (Document) renderRequest.getAttribute("doc");

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

%>

<% 	
	if("annonce".equals(doc.getType().toLowerCase()) || ( "note".equals(doc.getType().toLowerCase()) ) || ( "contextuallink".equals(doc.getType().toLowerCase()) ) )	{
	String jspName = "view-"+ doc.getType().toLowerCase() + ".jsp";
%>
		<jsp:include page="<%= jspName %>"></jsp:include>

<% } else { %>	
			
		<jsp:include page="view-default.jsp"></jsp:include>
		
<% } %> 

