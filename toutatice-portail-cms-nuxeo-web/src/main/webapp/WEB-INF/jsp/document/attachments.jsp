<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false"%>


<c:if test="${not empty document.attachments}">
    <hr>

    <div class="attachments">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">
                    <i class="glyphicons halflings uni-paperclip"></i>
                    <span><is:getProperty key="ATTACHMENTS" /></span>
                </h3>
            </div>
        
            <div class="list-group">
                <c:forEach var="attachment" items="${document.attachments}">
                    <a href="${attachment.url}" class="list-group-item">
                        <i class="glyphicons halflings file"></i>
                        <span>${attachment.name}</span>
                    </a>
                </c:forEach>
            </div>
        </div>
    </div>
</c:if>
