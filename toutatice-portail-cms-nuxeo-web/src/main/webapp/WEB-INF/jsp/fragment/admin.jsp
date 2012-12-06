
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.fragment.FragmentType"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>


<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

String fragmentTypeId = (String) renderRequest.getAttribute("fragmentTypeId")	;
Map<String, FragmentType> fragments = (Map<String, FragmentType>) request.getAttribute("fragmentTypes");
%>



	<div>
		<form method="post" action="<portlet:actionURL/>">
		
		
		<label>Type de fragment</label><br/>
		
		<select name="fragmentTypeId">
<%
			for(Map.Entry<String,FragmentType> fragmentType : fragments.entrySet()){
				String possibleFragmentType = fragmentType.getValue().getKey();
					if( possibleFragmentType.equals(fragmentTypeId)){
%>
										<option selected="selected" value="<%= possibleFragmentType %>"><%= fragmentType.getValue().getLabel() %></option>
<%
					}else{
%>
										<option value="<%= possibleFragmentType %>"><%= fragmentType.getValue().getLabel() %></option>
<%						
					}
				}

%>
		</select><input type="submit" name="changeFragmentType"  value=">>"><br/><br/>	
									
									
		
		
<% 	
	if(fragmentTypeId != null)	{
	   String jspName = "admin-"+ fragments.get(fragmentTypeId).getAdminJspName()  + ".jsp";
%>
		<jsp:include page="<%= jspName %>"></jsp:include>

		
<% } %> 
		
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	