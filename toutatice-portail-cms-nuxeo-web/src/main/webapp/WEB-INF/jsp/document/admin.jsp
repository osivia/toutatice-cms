
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>


<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

String displayLiveVersion = "";
if( "1".equals( request.getAttribute("displayLiveVersion")))
	displayLiveVersion = "checked";
%>



	<div>
		<form method="post" action="<portlet:actionURL/>">
		
			<label>Path</label><br/>
			<input type="text" name="nuxeoPath" value="${nuxeoPath}" size="40"><br/>
<!--  			<label>Scope</label><br/>
<%= ctx.formatScopeList( (String) renderRequest.getAttribute("scope")) %>--><br/> 
			
<%			
			String checkOnlyDescription = "checked";
			String onlyDescription = (String) request.getAttribute("onlyDescription");
			if( ! "1".equals( onlyDescription))
				checkOnlyDescription = "";
%>			
			
		<input type="checkbox" name="onlyDescription" value="1" <%=checkOnlyDescription%>/>	Afficher uniquement la description <br/>
		
			
<%			
			String checkShowMetadatas = "checked";
			String showMetadatas = (String) request.getAttribute("showMetadatas");
			if( ! "1".equals( showMetadatas))
				checkShowMetadatas = "";
%>			
			
		<input type="checkbox" name="showMetadatas" value="1" <%=checkShowMetadatas%>/>	Afficher les méta-données <br/>	
		
			
		<input type="checkbox" name="displayLiveVersion" value="1" <%= displayLiveVersion%>/>Affichage des versions non publiées <br/>
		
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	