<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<portlet:defineObjects/>

<%
String commentId = renderResponse.getNamespace() + "add-comment";
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
%>

<script type="text/javascript">
	var msg = "Le commentaire n'est pas renseigné";	
</script>

<div id="<%=commentId%>" class="add-comment">
	<form method="post" action="<portlet:actionURL><portlet:param name="comments" value="toAdd"/></portlet:actionURL>">	
		<textarea id="addedContent" name="content" rows="6" cols="60"></textarea><br/>
		<div id="errorAddCom" class="contentError"></div><br/>
		<input type="submit" name="addComment"  value="Ajouter" onClick="if(!isEmptyField('addedContent','errorAddCom', msg)){fancyContainerDivId='<%=containerId%>';}else{return false;}">
		<input type="reset" name="noAddComment"  value="Annuler" onclick="closeFancyBox();">
	</form>
</div>

<script type="text/javascript">
	var addDiv = document.getElementById('<%=commentId%>');
	Event.observe(addDiv, "click", bilto);
</script>