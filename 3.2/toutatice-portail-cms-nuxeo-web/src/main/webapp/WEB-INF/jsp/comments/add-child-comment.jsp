<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false" %>
<%@ page import="fr.toutatice.portail.cms.nuxeo.portlets.document.comments.HTMLCommentsTreeBuilder"%>

<portlet:defineObjects/>

<%
String commentId = (String) renderRequest.getAttribute("commentDivId");
String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
%>

<portlet:actionURL var="valid_child" ><portlet:param name="comments" value="addChild"/></portlet:actionURL>

<div id="<%= commentId %><%= containerId %>" class="add-child-comment" style="display: none">
	<form method="post" action="${valid_child}" class="ajax-form">
		<textarea id="addedChildComment<%= commentId %><%= containerId %>" name="childCommentContent" rows="6" cols="60"></textarea><br/>
		<div id="errorAddChildCom<%= commentId %><%= containerId %>" class="contentError"></div>
		<input id="commentParentId<%= commentId %><%= containerId %>" type="hidden" name="commentId" value=""/>	
		<input type="submit" name="addComment"  value="Répondre" class="ajax-link">		
		<input type="reset" name="noAddComment"  value="Annuler" onclick="hideCommentField('<%= commentId %><%= containerId %>');">
	</form>
</div>