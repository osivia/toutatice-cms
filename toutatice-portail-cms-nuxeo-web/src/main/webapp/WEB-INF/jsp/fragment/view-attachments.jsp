<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled">
    <c:if test="${empty attachments}">
        <li>
            <p class="text-muted"><op:translate key="FRAGMENT_ATTACHMENTS_EMPTY" /></p>
        </li>
    </c:if>

    <c:forEach var="attachment" items="${attachments}">
        <li>
            <p>
                <c:choose>
                    <c:when test="${empty attachment.icon}"><i class="glyphicons glyphicons-file"></i></c:when>
                    <c:otherwise><i class="${attachment.icon}"></i></c:otherwise>
                </c:choose>
                
                <a href="${attachment.url}" target="_blank">${attachment.name}</a>
                
                <c:if test="${not empty attachment.size}">
                    <small>(<ttc:fileSize size="${attachment.size}"/>)</small>
                </c:if>
            </p>
        </li>
    </c:forEach>
</ul>
