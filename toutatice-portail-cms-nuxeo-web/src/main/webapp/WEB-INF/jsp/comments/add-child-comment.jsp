<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<portlet:defineObjects/>

<%
String commentId = renderResponse.getNamespace() + "add-child-comment";
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
%>

<script type="text/javascript">
	var msg = "Le commentaire n'est pas renseigné";
</script>

<div id="<%=commentId%>" class="add-child-comment">
	<form method="post" action="<portlet:actionURL><portlet:param name="comments" value="addChild"/></portlet:actionURL>">
		<textarea id="addedChildComment" name="childCommentContent" rows="6" cols="60"></textarea><br/>
		<div id="errorAddChildCom" class="contentError"></div><br/>
		<input id="commentParentId" type="hidden" name="commentId" value=""/>
		<input type="submit" name="addComment"  value="Répondre" onClick="if(!isEmptyField('addedChildComment','errorAddChildCom',msg)){fancyContainerDivId='<%=containerId%>';}else{return false;}">
		<input type="reset" name="noAddComment"  value="Annuler" onclick="closeFancyBox();">
	</form>
</div>

<script>
	var addChildDiv = document.getElementById('<%=commentId%>');
	Event.observe(addChildDiv, "click", bilto);
</script>