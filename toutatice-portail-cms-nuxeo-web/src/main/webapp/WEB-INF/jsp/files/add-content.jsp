<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.SubType"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="org.osivia.portal.api.path.PortletPathItem"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page import="javax.portlet.WindowState"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.FileBrowserPortlet"%>

<portlet:defineObjects/>

<%	
// V2.1 : workspace

List<SubType> portalDocsToCreate = (List) renderRequest.getAttribute("portalDocsToCreate")	;

%>


<%
if( portalDocsToCreate != null) {	
%>
<div class="fancybox-content">
	<div id="<%=renderResponse.getNamespace()%>_PORTAL_CREATE" class="document-types">
		<div class="main-doc-types" id="<%=renderResponse.getNamespace()%>_MAIN">
			<div class="doc-type-title">Ajouter un contenu</div>
<%	
	int index = 1;
	int nbSubDocs = portalDocsToCreate.size();
	for (SubType subDoc: portalDocsToCreate) {
%>
		
			<div class="doc-type-detail">
				<div class="vignette"> 
					<a class="fancyframe_refresh" href="<%= subDoc.getUrl() %>">
						<img src="/toutatice-portail-cms-nuxeo/img/icons/<%= subDoc.getDocType().toLowerCase()%>_100.png"> 
					</a>	
				</div> 
				<div class="main">
					<div class="title">
						<a class="fancyframe_refresh" href="<%= subDoc.getUrl() %>"><%= subDoc.getName() %></a>
					</div>
				</div>
						
	 		 </div>

	<% if(index < nbSubDocs){ %>
		<div class="vertical-separator"></div>
	<% } %>
		
<%   
		index++;
	}
%>

		</div>
	







	
	</div>
</div>
<% } %>



