<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<portlet:defineObjects/>

<%
String deleteDivId = renderResponse.getNamespace() + "delete-file-item";
String deleteFormId = renderResponse.getNamespace() + "delete-file-form";
%>


<div id="<%=deleteDivId%>" class="delete-file-item">
	<form method="post" id="<%=deleteFormId%>" action="">
		<div>Confirmez-vous la suppression de l'élément ?</div><br/>
		<input type="submit" name="deleteFileItem"  value="Confirmer">
		<input type="button" name="noDeleteFileItem"  value="Annuler" onclick="closeFancybox()">
	</form>
</div>

