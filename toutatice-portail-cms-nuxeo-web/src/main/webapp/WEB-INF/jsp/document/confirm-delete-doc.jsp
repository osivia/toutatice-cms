<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>

<portlet:defineObjects/>

<%
String deleteDivId = renderResponse.getNamespace() + "delete-file-item";
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
Document document = (Document) renderRequest.getAttribute("doc");
%>


<div id="<%=deleteDivId%>" class="delete-file-item">
	<form method="post" action="<portlet:actionURL></portlet:actionURL>">
		<div>Confirmez-vous la suppression de l'élément ?</div><br/>
		<input id="currentDocId" type="hidden" name="docId" value="<%= document.getId() %>"/>
		<input id="currentDocPath" type="hidden" name="docPath" value="<%= document.getPath() %>"/>
		<input type="submit" name="deleteDoc"  value="Confirmer" onClick="fancyContainerDivId='<%=containerId%>';">
		<input type="reset" name="noDeleteDoc"  value="Annuler" onclick="closeFancyBox();">
	</form>
</div>

<script>
	var deleteDiv = document.getElementById('<%=deleteDivId%>');
	Event.observe(deleteDiv, "click", bilto);
</script>