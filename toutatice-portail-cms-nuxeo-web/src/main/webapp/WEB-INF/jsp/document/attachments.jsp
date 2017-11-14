<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:if test="${not empty document.attachments}">
    <div class="hidden-print">
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
                            <c:choose>
                                <c:when test="${empty attachment.icon}"><i class="glyphicons glyphicons-file"></i></c:when>
                                <c:otherwise><i class="${attachment.icon}"></i></c:otherwise>
                            </c:choose>
                    
                            <span>${attachment.name}</span>
                        
                            <c:if test="${not empty attachment.size}">
                                <small>(<ttc:fileSize size="${attachment.size}"/>)</small>
                            </c:if>
                        </a>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</c:if>
