<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false"%>


<portlet:actionURL name="addComment" var="addCommentURL" />


<c:set var="namespace"><portlet:namespace /></c:set>


<c:if test="${document.commentable}">
    <hr>

    <div class="comments">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">
                    <i class="glyphicons conversation"></i>                
                    <span><is:getProperty key="COMMENTS" /></span>
                </h3>
            </div>
        
            <div class="list-group">
                <c:forEach var="comment" items="${document.comments}">
                    <div class="list-group-item">
                        <c:set var="comment" value="${comment}" scope="request" />
                        <jsp:include page="comment.jsp" />
                    </div>
                </c:forEach>
                
                
                <div class="list-group-item">
                    <a href="#${namespace}-add-comment" class="btn btn-default" data-toggle="collapse">
                        <i class="glyphicons comments"></i>
                        <span><is:getProperty key="COMMENT_ADD" /></span>
                    </a>
                    
                    
                    <div id="${namespace}-add-comment" class="collapse">
                        <hr>
                    
                        <form action="${addCommentURL}" method="post" role="form">
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
        </div>
    </div>
</c:if>
