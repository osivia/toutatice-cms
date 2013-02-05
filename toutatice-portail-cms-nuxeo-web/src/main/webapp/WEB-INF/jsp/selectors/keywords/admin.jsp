
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>



	<div>
		<form method="post" action="<portlet:actionURL/>">
				
			<label>Identifiant sélecteur</label>
			<input type="text" name="selectorId" value="${selectorId}" size="40"/><br/>
			<% String multiValued = "";
				if("1".equals(renderRequest.getAttribute("keywordMultiValued")))
					multiValued = "checked='checked'";	
			%>
			<input type="checkbox" name="keywordMultiValued" value="1" <%= multiValued %>/>  Sélecteur multi-valué
			<br/><br/>
			<input type="submit" name="modifierPrefs"  value="Valider"/>
			<input type="submit" name="annuler"  value="Annuler"/>
			
		</form>
	</div>
	
	