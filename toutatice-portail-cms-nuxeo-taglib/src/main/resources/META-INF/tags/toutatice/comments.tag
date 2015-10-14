<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" isELIgnored="false" %>
<%@ attribute name="document" description="Document DTO." required="true" rtexprvalue="true" type="fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>


<portlet:actionURL name="addComment" var="addCommentUrl" />
<portlet:actionURL name="deleteComment" var="deleteCommentUrl" />


<c:set var="namespace"><portlet:namespace /></c:set>


<c:if test="${document.commentable}">
    <div class="hidden-print">
        <hr>
        
        <div class="comments">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                        <i class="glyphicons glyphicons-conversation"></i>                
                        <span><op:translate key="COMMENTS" /></span>
                    </h3>
                </div>
                
                <div class="list-group">
                    <c:forEach items="${document.comments}" var="comment">
                        <div class="list-group-item">
                            <ttc:comment comment="${comment}" />
                        </div>
                    </c:forEach>
                </div>
                
                <div class="panel-body">
                    <a href="#${namespace}-add-comment" class="btn btn-default no-ajax-link" data-toggle="collapse">
                        <i class="glyphicons glyphicons-chat"></i>
                        <span><op:translate key="COMMENT_ADD" /></span>
                    </a>
                    
                    <div id="${namespace}-add-comment" class="collapse">
                        <hr>
                    
                        <form action="${addCommentUrl}" method="post" class="no-ajax-link" role="form">
                            <div class="form-group">
                                <label for="${namespace}-comment-content"><op:translate key="COMMENT_CONTENT" /></label>
                                <textarea id="${namespace}-comment-content" name="content" class="form-control"></textarea>
                            </div>
                            
                            <div class="form-group">
                                <button type="submit" class="btn btn-primary">
                                    <span><op:translate key="SAVE" /></span>
                                </button>
                                
                                <button type="button" class="btn btn-default" onclick="$JQry('#${namespace}-add-comment').collapse('hide')">
                                    <span><op:translate key="CANCEL" /></span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    
    <!-- Delete comment confirmation fancybox -->
    <div class="hidden">
        <div id="${namespace}-delete-comment" class="container-fluid">
            <form action="${deleteCommentUrl}" method="post" class="no-ajax-link" role="form">
                <input type="hidden" name="id">
            
                <div class="form-group">
                    <p><op:translate key="COMMENT_SUPPRESSION_CONFIRM_MESSAGE" /></p>
                    
                    <div class="text-center">
                        <button type="submit" class="btn btn-warning">
                            <i class="halflings halflings-alert"></i>
                            <span><op:translate key="YES" /></span>
                        </button>
                        
                        <button type="button" class="btn btn-default" onclick="closeFancybox()">
                            <span><op:translate key="NO" /></span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</c:if>
