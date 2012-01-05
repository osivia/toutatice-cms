
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.FileBrowserPortlet"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>


<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Documents"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>




<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>
<portlet:defineObjects />

<%
Documents docs = (Documents) renderRequest.getAttribute("docs")	;
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String basePath = (String) request.getAttribute("basePath");
String folderPath = (String) request.getAttribute("folderPath");
%>



<table class="nuxeo-file-browser-table"  cellspacing="5" width="95%">

<% if( WindowState.MAXIMIZED.equals(renderRequest.getWindowState()))	{	%>

<tr align="left">
	<th width="5%">&nbsp;</th>
	<th width="20%">Nom</th>
	<th width="15%">Date</th>	
	<th width="10%">Taille</th>
	<th >Description</th>
</tr>	

<% } else	{%>

<tr align="left">
	<th width="5%">&nbsp;</th>
	<th width="40%">Nom</th>
	<th width="35%">Date</th>	
	<th width="25%">Taille</th>
</tr>	
<% } %>



<%

Iterator it = docs.iterator();
while( it.hasNext())	{
	Document doc = (Document) it.next();

	String url = null;
	
	if( "File".equals(doc.getType()))	{
		url = ctx.createFileLink(doc, "file:content");
	}	else if( FileBrowserPortlet.isFolder( doc))	{
		PortletURL folderURL = renderResponse.createRenderURL();
		folderURL.setParameter("folderPath", doc.getPath());	
		url = folderURL.toString();
	}
	
	if( url != null)	{
%>


		<tr> 
			<td> 
				<img src="<%=renderRequest.getContextPath()%><%= Formater.formatNuxeoIcon(doc)%>">
			</td> 
			<td>
				<a  href="<%=url%>"><%=doc.getTitle()%> </a>
			</td>
			<td>
				<%= Formater.formatDateAndTime(doc) %>
			</td>
			<td align="right">
				<%= Formater.formatSize(doc) %>
			</td>
			
<% if( WindowState.MAXIMIZED.equals(renderRequest.getWindowState()))	{	
%>			
			<td>
				<%= Formater.formatDescription(doc, false) %>
			</td>
<% } %>
			
		</tr>

<%
	}
}
%>
</table>
	



<!--
<p align="center">
		scope	<%=  ctx.getScope() %> <br/>
</p>
-->