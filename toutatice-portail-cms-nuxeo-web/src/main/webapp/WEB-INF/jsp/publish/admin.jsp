<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>


<%@page import="java.util.List"%>


<%@page import="java.util.Map"%>


<portlet:defineObjects/>
<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

%>
	<div>
		<form method="post" action="<portlet:actionURL/>">
		

		<label>Nombre de niveaux ouverts :</label> <input type="text" name="openLevels" value="${openLevels}" size="1"> <br/>
		<label>Nombre de niveaux maximum :</label> <input type="text" name="maxLevels" value="${maxLevels}" size="1"> <br/>
			
		
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	