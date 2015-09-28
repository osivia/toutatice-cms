<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<portlet:actionURL name="replyComment" var="replyCommentURL">
    <portlet:param name="id" value="${comment.id}" />
</portlet:actionURL>

<portlet:actionURL name="deleteComment" var="deleteCommentURL">
    <portlet:param name="id" value="${comment.id}" />
</portlet:actionURL>


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="currentComment" value="${comment}" />
<c:set var="deleteTitle"><is:getProperty key="DELETE" /></c:set>


<div class="comment">
    <!-- Actions -->
    <div class="btn-toolbar pull-right" role="toolbar">
        <div class="btn-group btn-group-sm">
            <!-- Reply -->
            <a href="#${namespace}-reply-comment-${currentComment.id}" class="btn btn-default no-ajax-link" data-toggle="collapse">
                <i class="glyphicons glyphicons-chat"></i>
                <span><is:getProperty key="REPLY" /></span>
            </a>
            
            <!-- Delete -->
            <c:if test="${currentComment.deletable}">
                <a href="#${namespace}-delete-comment-${currentComment.id}" class="btn btn-default fancybox_inline no-ajax-link" title="${deleteTitle}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-remove"></i>
                </a>
            </c:if>
        </div>
    </div>
    
    <div>
        <!-- Informations -->
        <p class="small">
            <ttc:user name="${currentComment.author}"/>
            <span> - </span>
            <span><fmt:formatDate value="${currentComment.creationDate}" type="both" dateStyle="full" timeStyle="short" /></span>
        </p>
    
        <!-- Content -->
        <div>${currentComment.content}</div>
        
        <!-- Children -->
        <div class="children">
            <c:forEach var="child" items="${currentComment.children}">
                <c:set var="comment" value="${child}" scope="request" />
                <jsp:include page="comment.jsp" />
            </c:forEach>
        
        
            <div id="${namespace}-reply-comment-${currentComment.id}" class="collapse">
                <hr>
            
                <form action="${replyCommentURL}" method="post" role="form">
                    <div class="form-group">
                        <label for="${namespace}-comment-content"><is:getProperty key="COMMENT_CONTENT" /></label>
                        <textarea id="${namespace}-comment-content" name="content" class="form-control"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <button type="submit" class="btn btn-primary">
                            <span><is:getProperty key="SAVE" /></span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    
    <!-- Delete comment confirmation fancybox -->
    <div class="hidden">
        <div id="${namespace}-delete-comment-${currentComment.id}" class="container-fluid">
            <form action="${deleteCommentURL}" method="post" role="form">            
                <div class="form-group">
                    <p><is:getProperty key="COMMENT_SUPPRESSION_CONFIRM_MESSAGE" /></p>
                    <div class="text-center">
                        <button type="submit" class="btn btn-warning">
                            <i class="halflings halflings-alert"></i>
                            <span><is:getProperty key="YES" /></span>
                        </button>
                        <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="NO" /></button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>


