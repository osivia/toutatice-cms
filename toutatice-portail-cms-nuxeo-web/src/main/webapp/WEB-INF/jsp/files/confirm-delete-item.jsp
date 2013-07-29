<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<portlet:defineObjects/>

<%
String deleteDivId = renderResponse.getNamespace() + "delete-file-item";
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
%>


<div id="<%=deleteDivId%>" class="delete-file-item">
	<form method="post" action="<portlet:actionURL></portlet:actionURL>">
		<div>Confirmez-vous la suppression de l'élément ?</div><br/>
		<input id="currentFileItemId" type="hidden" name="fileItemId" value=""/>
		<input type="submit" name="deleteFileItem"  value="Confirmer" onClick="fancyContainerDivId='<%=containerId%>';">
		<input type="reset" name="noDeleteFileItem"  value="Annuler" onclick="closeFancyBox();">
	</form>
</div>

<script>
	var deleteDiv = document.getElementById('<%=deleteDivId%>');
	Event.observe(deleteDiv, "click", bilto);
</script>