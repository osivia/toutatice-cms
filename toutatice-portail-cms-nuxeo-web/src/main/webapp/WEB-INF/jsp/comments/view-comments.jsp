<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page isELIgnored="false"%>

<%@ page import="java.util.List"%>

<%@ page import="fr.toutatice.portail.cms.nuxeo.portlets.document.comments.HTMLCommentsTreeBuilder"%>

<portlet:defineObjects />

<script type="text/javascript">
	var msg = "Le commentaire n'est pas renseigné";
	function showCommentField(id){
		var commentField = document.getElementById(id);
		commentField.style.display = "block";
	}
	function hideCommentField(id){
		var commentDiv = document.getElementById(id);
		commentDiv.style.display = 'none';
	}
	function focusCommentField(){
		document.getElementById('addedContent').focus();
	}
</script>
<% 	
	String containerId = (String) renderRequest.getAttribute("osivia.window.ID");
	String commentsTree = (String) renderRequest.getAttribute("comments"); 
  	if (commentsTree != null) {
%>
    <div class="commentsTitle"><h2>Commentaires</h2></div>
	
	<!-- Inclusion de la jsp des réponses aux commentaires -->
<%
	String[] commentsTreeSplit = commentsTree.split(HTMLCommentsTreeBuilder.ADD_COM_CHILD_JSP_TAG); 
	int index = 1;
	int treeSize = commentsTreeSplit.length;
%>
	<div class="commentsTree">
	<%  String commentId = "";
		for(String commentsTreePart : commentsTreeSplit){ 
		    commentsTreePart = commentsTreePart.replace(HTMLCommentsTreeBuilder.DIV_COM_ID_TAG, commentId);
		    commentsTreePart = commentsTreePart.replace(HTMLCommentsTreeBuilder.ADD_CONTAINER_ID, containerId);
	%>
		<%= commentsTreePart %>
		<% if(index < treeSize){ 
			commentId = "com-" + String.valueOf(index);
			renderRequest.setAttribute("commentDivId", commentId);
		%>
			<jsp:include page="add-child-comment.jsp"></jsp:include>		
	<% 	   }
		  index++;
	   }
	%>
	</div> 
	
	<div id="div_delete_comment<%= containerId %>" style="display: none">
		<jsp:include page="delete-comment.jsp"></jsp:include>
	</div>
	
	<div class="add-comment-link">
		<div id="fake_add_comment<%= containerId %>"  class="add-comment" onclick="showCommentField('div_add_comment<%= containerId %>');hideCommentField('fake_add_comment<%= containerId %>');focusCommentField();" >	
				<textarea id="fakeaddedContent" name="content" rows="1" cols="60" style="color:#bbbbbb">Ajouter un commentaire...</textarea><br/>
		</div>
    	<div id="div_add_comment<%= containerId %>" style="display: none">
			<jsp:include page="add-comment.jsp"></jsp:include>
		</div>
	</div>
<% 
	} 
%>