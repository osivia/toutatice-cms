<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false"%>


<c:if test="${not empty document.attachments}">
    <div class="hidden-print">
        <hr>
    
        <div class="attachments">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                        <i class="halflings halflings-paperclip"></i>
                        <span><op:translate key="ATTACHMENTS" /></span>
                    </h3>
                </div>
            
                <div class="list-group">
                    <c:forEach var="attachment" items="${document.attachments}">
                        <a href="${attachment.url}" target="_blank" class="list-group-item">
                            <i class="halflings halflings-file"></i>
                            <span>${attachment.name}</span>
                        </a>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</c:if>
