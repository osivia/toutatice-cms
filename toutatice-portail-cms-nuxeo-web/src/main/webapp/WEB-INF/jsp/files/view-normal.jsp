
<%@page import="fr.toutatice.portail.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
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
String displayMode = (String) request.getAttribute("displayMode");

%>

<div class="nuxeo-file-browser">


<%@ include file="header.jsp" %>

	
<div class="separateur"></div>	

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
	Link link = null;
	String icon = Formater.formatNuxeoIcon(doc);
	String target = "";	
	boolean noAjax = true;
	
	 if(  FileBrowserPortlet.isNavigable( doc))	{
		PortletURL folderURL = renderResponse.createRenderURL();
		folderURL.setParameter("folderPath", doc.getPath());
		if( displayMode != null)
			folderURL.setParameter("displayMode", displayMode);
	
		url = folderURL.toString();
		link = new Link(url, false);
		
		// le mode ajax n'est autorise que pour les folders en mode NORMAL
		if( WindowState.NORMAL.equals(renderRequest.getWindowState()))
			noAjax = false;
			

	}	else {
		link = ctx.getLink(doc);
		url = link.getUrl();
		target = Formater.formatTarget(link);

	}
	 
	icon = "<img style=\"vertical-align:middle\" src=\""+renderRequest.getContextPath()+icon+"\">";					
%>


		<tr> 
			<td> 
				<%=icon%>
			</td> 
			<td>
<% if (noAjax)		{ %> 
	 <div class="no-ajax-link"> 
<% }	%>			
				<a <%=target%> href="<%=url%>">  <%=doc.getTitle()%> </a>
<% if (noAjax)		{ %> 
	 </div> 
<% }	%>					
			</td>
			<td>
				<%= Formater.formatDateAndTime(doc) %>
			</td>
			<td align="right">
				<%= Formater.formatSize(doc) %>
			</td>
			
<% 		if( WindowState.MAXIMIZED.equals(renderRequest.getWindowState()))	{	
			String description = Formater.formatDescription(doc, false);
			if( description.length() > 20)
				description = description.substring(0, 20) + "...";
	
%>			
			<td>
				<%= description %>
			</td>
<% 		} %>
			
		</tr>
		

<%
	}

%>
</table>





	

</div>

<!--
<p align="center">
		scope	<%=  ctx.getScope()  %> <br/>
</p>
-->