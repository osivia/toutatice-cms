<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>


<%@page import="java.util.Map"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.TransformationContext"%>

<portlet:defineObjects/>

<%
TransformationContext ctx = (TransformationContext) renderRequest.getAttribute("ctx")	;
String beanShell = "";
if( "1".equals( request.getAttribute("beanShell")))
		beanShell = "checked";

%>


	<div>
		<form method="post" action="<portlet:actionURL/>">
		<label>Requête Nuxeo</label><br/>
		<textarea rows="8" cols="80" name="nuxeoRequest" >${nuxeoRequest}</textarea><br/><br/>
		<input type="checkbox" name="beanShell" value="1" <%= beanShell%>/>Interprétation BeanShell de la requête<br/>

		<label>Limiter les résultats à <input type="text" name="maxItems" value="${maxItems}" size="2"> items <br/><br/>

		<label>Pagination :</label> <input type="text" name="pageSize" value="${pageSize}" size="2"> items par page<br/><br/>
		<label>Style d'affichage</label><br/>
		<select name="style">
<%
			Map<String, String> styles  = (Map<String, String>) request.getAttribute("styles");
			String style = (String) request.getAttribute("style");

			for(String possibleStyle : styles.keySet()){
					if( possibleStyle.equals(style)){
%>
										<option selected="selected" value="<%= possibleStyle %>"><%= styles.get(possibleStyle) %></option>
<%
					}else{
%>
										<option value="<%= possibleStyle %>"><%= styles.get(possibleStyle) %></option>
<%						
					}
				}

%>
									</select><br/><br/>		
	
		<label>Scope</label><br/>
<%= ctx.formatScopeList( (String) renderRequest.getAttribute("scope")) %><br/><br/>

		<label>Référence permalink :</label> <input type="text" name="permaLinkRef" value="${permaLinkRef}" size="10"> <br/><br/>
			
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	