<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<portlet:defineObjects/>

<%
String commentId = renderResponse.getNamespace() + "delete-comment";
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
%>

<div id="<%=commentId%>" class="delete-comment">
	<form method="post" action="<portlet:actionURL><portlet:param name="comments" value="delete"/></portlet:actionURL>">
		<div>Confirmez-vous la suppression du commentaire?</div><br/>
		<input id="currentCommentId" type="hidden" name="commentId" value=""/>
		<input type="submit" name="deleteComment"  value="Confirmer" onClick="fancyContainerDivId='<%=containerId%>';">
		<input type="reset" name="noDeleteComment"  value="Annuler" onclick="closeFancyBox();">
	</form>
</div>

<script>
	var deleteDiv = document.getElementById('<%=commentId%>');
	Event.observe(deleteDiv, "click", bilto);
</script>