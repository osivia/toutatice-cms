
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>



	<div>
		<form method="post" action="<portlet:actionURL/>">
		
			<label>Libellé</label><br/>
			<input type="text" name="libelle" value="${libelle}" size="40"/><br/>
			<br/>
		
			<label>Identifiant sélecteur</label><br/>
			<input type="text" name="selectorId" value="${selectorId}" size="40"><br/>
			<br/>
			<% String monovalue = "";
				if("1".equals(renderRequest.getAttribute("datesMonoValued")))
					monovalue = "checked='checked'";	
			%>
			<input type="checkbox" name="datesMonoValued" value="1" <%= monovalue %>/>  Sélecteur mono-valué
			<br/><br/>
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	