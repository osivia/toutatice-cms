
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>




<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyList"%>




<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.portalsite.PortalSiteBean"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.portalsite.ServiceDisplayItem"%><portlet:defineObjects />

<%
List<ServiceDisplayItem> servicesItems = (List<ServiceDisplayItem>)  request.getAttribute("serviceItems")	;
NuxeoController ctx = (NuxeoController) request.getAttribute("ctx")	;
%>


<div class="nuxeo-navigation">
	<ul>
		
<% 	
for( ServiceDisplayItem service : servicesItems)	{
	String target = "";
	if( service.isExternal())
		target = "target=\"_blank\"";
	
%>
		<li>
			<a <%= target %> href="<%= service.getUrl() %>"><%= service.getTitle() %></a>
		</li>
<%
}


 %>						
	</ul>
</div>	
		
					

<%= ctx.getDebugInfos() %>

