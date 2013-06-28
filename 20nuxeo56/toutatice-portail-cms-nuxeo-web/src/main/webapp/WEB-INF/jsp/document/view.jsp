
<%@page import="org.osivia.portal.api.urls.IPortalUrlFactory"%>
<%@page import="org.osivia.portal.api.menubar.MenubarItem"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.document.ViewDocumentPortlet"%>
<%@page import="org.osivia.portal.api.urls.Link"%>
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



String onlyDescription = (String) request.getAttribute("onlyDescription");

String showMetadatas = (String) request.getAttribute("showMetadatas");

if( "detailedView".equals(ctx.getDisplayContext()))
	showMetadatas = "1";



String srcVignette = "";
PropertyMap map = doc.getProperties().getMap("ttc:vignette");
if( map != null && map.getString("data") != null)	
	srcVignette = "<img class=\"nuxeo-docview-vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" />";

%>

<div class="nuxeo-docview-<%= doc.getType().toLowerCase()%>">

<% if( "1".equals(onlyDescription) && !renderRequest.getWindowState().equals(WindowState.MAXIMIZED))	{	%>
	<div class="nuxeo-docview-short-view">

			<%= srcVignette %><p class="nuxeo-docview-description"><%=Formater.formatDescription(doc)%></p>

			<div class="nuxeo-docview-switch-mode"><a href="<%= ctx.getLink(doc).getUrl() %>">suite...</a></div>
	</div>			
			
<% } else	{	 %>

<div class="nuxeo-docview-normal-view">

	<% if( "1".equals(showMetadatas) )	{	%>


	<jsp:include page="view-metadata.jsp"></jsp:include>
	
	<% }  %>

	<div class="nuxeo-docview-main">

	<jsp:include page="view-dispatch.jsp"></jsp:include>


<% 
	PropertyList files = doc.getProperties().getList("files:files");
	if( files != null && !files.isEmpty())	{	
		
%>
		<div class="nuxeo-docview-files">
			<h3>
				<span>Fichiers joints</span>
			</h3>
			<ul>
<% 		
		int fileIndex = 0;
		while (fileIndex < files.size()) {	
			String fileName = files.getMap(fileIndex).getString("filename");
			
			String fileUrl = ctx.createAttachedFileLink(doc.getPath(),  Integer.toString(fileIndex));
			
%>
					<li> <a href="<%= fileUrl %>"> <%=fileName  %> </a></li>
<%			fileIndex++;
		} %>						
			</ul>
		</div>	
		
<%	} %>	

	</div>


</div>

<%	} %>
			
</div>

<%= ctx.getDebugInfos() %>