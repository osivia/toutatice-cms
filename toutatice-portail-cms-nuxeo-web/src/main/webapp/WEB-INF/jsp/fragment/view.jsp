<%@ page contentType="text/plain; charset=UTF-8"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.fragment.FragmentType"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

FragmentType fragmentType = (FragmentType) renderRequest.getAttribute("fragmentType")	;
%>



<div class="nuxeo-fragment-view">

<% 	
	if(fragmentType != null)	{
	   String jspName = "view-"+ fragmentType.getViewJspName()  + ".jsp";
%>
		<jsp:include page="<%= jspName %>"></jsp:include>
<% } %> 

</div>



<%= ctx.getDebugInfos() %>