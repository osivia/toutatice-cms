<%@page import="fr.toutatice.portail.cms.nuxeo.service.editablewindow.Link"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/plain; charset=UTF-8"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>



<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>


<portlet:defineObjects />

<%
	Document doc = (Document) renderRequest.getAttribute("doc");

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
//String view = (String) renderRequest.getAttribute("view");
//view = "view-zoom-" + view + ".jsp";


%>

View Zoom

${href}
${content}
${picture}

