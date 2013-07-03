<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<portlet:defineObjects/>

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String changeDisplayMode = "";
if( "1".equals( request.getAttribute("changeDisplayMode")))
	changeDisplayMode = "checked";
String forceContextualization = "";
if( "1".equals( request.getAttribute("forceContextualization")))
	forceContextualization = "checked";
%>


	<div>
		<form method="post" action="<portlet:actionURL/>">
		
			<label>Path</label><br/>
			<input type="text" name="nuxeoPath" value="${nuxeoPath}" size="40"><br/>
			<label>Scope</label><br/>
<%= ctx.formatScopeList( (String) renderRequest.getAttribute("scope")) %><br/>

			<label>Version</label><br/>
<%= ctx.formatDisplayLiveVersionList( (String) renderRequest.getAttribute("displayLiveVersion")) %><br/>

		<input type="checkbox" name="forceContextualization" value="1" <%= forceContextualization%>/>Contextualiser dans la page<br/>
		<input type="checkbox" name="changeDisplayMode" value="1" <%= changeDisplayMode%>/>Sélecteur mode affichage détaillé<br/>
		
		
		
<br/>




<br/>
<br/>	
		
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	