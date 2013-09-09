<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>

<portlet:defineObjects/>

<%
String commentId = "add-comment" + String.valueOf(System.currentTimeMillis());
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
%>

<portlet:actionURL var="valid"><portlet:param name="comments" value="toAdd"/></portlet:actionURL>

<div id="<%=commentId%>" class="add-comment">
	<form id="add-com-form" method="post" action="${valid}" class="ajax-form">	
		<textarea id="addedContent" name="content" rows="6" cols="60"></textarea><br/>
		<div id="errorAddCom" class="contentError"></div>
		<input type="submit" name="addComment"  value="Ajouter" class="ajax-link">
		<input type="reset" name="noAddComment"  value="Annuler" onclick="hideCommentField('div_add_comment');">
	</form>
</div>