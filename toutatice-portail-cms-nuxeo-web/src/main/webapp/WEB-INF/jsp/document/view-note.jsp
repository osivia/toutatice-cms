
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>



<%@page import="org.nuxeo.ecm.automation.client.model.Blob"%>


<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyList"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.StringHelper"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>

<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%><portlet:defineObjects />

<%
Document doc = (Document) renderRequest.getAttribute("doc");

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;



String note = doc.getString("note:note", "");
if( note != null)
		note = ctx.transformHTMLContent( note);	

%>

			
<div class="nuxeo-docview-note">			
		<%= note %>
</div>
			
<!-- NOTE -->
