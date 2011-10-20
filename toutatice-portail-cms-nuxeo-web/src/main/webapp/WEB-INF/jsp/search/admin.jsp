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
			<label>Scope</label><br/>
<%= ctx.formatScopeList( (String) renderRequest.getAttribute("scope")) %><br/>
			<input type="checkbox" name="displayLiveVersion" value="1" <%= displayLiveVersion%>/>Recherche sur les versions non publiées <br/>
		
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	