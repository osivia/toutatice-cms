<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false"%>

<%@ page import="net.sf.json.JSONArray"%>
<%@ page import="net.sf.json.JSONObject"%>
<%@ page import="java.util.Iterator"%>

<portlet:defineObjects />

<% String commentsTree = (String) renderRequest.getAttribute("comments"); 
   if (commentsTree != null) {%>
    <div class="commentsTitle"><h2>Commentaires</h2></div>
    
    <div class="add-comment-link">
		<a class="fancybox_comment" href="#div_add_comment">Ajouter un commentaire</a>
	</div>
   
	<div class="commentsTree"><%= commentsTree %></div> 
	
	<div id="div_add_comment" style="display: none">
		<jsp:include page="add-comment.jsp"></jsp:include>
	</div>
	
	<div id="div_add_child_comment" style="display: none">
		<jsp:include page="add-child-comment.jsp"></jsp:include>
	</div>
	
	<div id="div_delete_comment" style="display: none">
		<jsp:include page="delete-comment.jsp"></jsp:include>
	</div>

<% } %>