
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
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.TransformationContext"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%><portlet:defineObjects />

<%
Document doc = (Document) renderRequest.getAttribute("doc");

TransformationContext ctx = (TransformationContext) renderRequest.getAttribute("ctx")	;


String srcImage = "";

PropertyMap mapImage = doc.getProperties().getMap("annonce:image");
if( mapImage != null && mapImage.getString("data") != null)	
	srcImage = "<img class=\"nuxeo-docview-image\" src=\""+ ctx.createFileLink(doc, "annonce:image") + "\" />";
		
String resume = doc.getProperties().getString("annonce:resume");


String note = doc.getString("note:note", "");
if( note != null)
		note = ctx.transformHTMLContent( note);	

%>


<%= srcImage %>

<% if (resume != null && resume.length() > 0)	{	%>	
			<p class="nuxeo-docview-resume">
				<%=  Formater.formatText( resume, true)%> 
			</p>
<% } %>	

			
<div class="nuxeo-docview-note">			
		<%= note %>
</div>
			
	
