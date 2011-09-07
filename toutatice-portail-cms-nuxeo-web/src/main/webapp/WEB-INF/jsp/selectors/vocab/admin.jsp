
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.TransformationContext"%>

<portlet:defineObjects/>



	<div>
		<form method="post" action="<portlet:actionURL/>">
		
		
			<label>Identifiant sélecteur</label><br/>
			<input type="text" name="selectorId" value="${selectorId}" size="40"><br/>

			<label>Nom vocabulaire niveau 1</label><br/>
			<input type="text" name="vocabName1" value="${vocabName1}" size="40"><br/>


			<label>Nom vocabulaire niveau 2 (facultatif)</label><br/>
			<input type="text" name="vocabName2" value="${vocabName2}" size="40"><br/>


			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	