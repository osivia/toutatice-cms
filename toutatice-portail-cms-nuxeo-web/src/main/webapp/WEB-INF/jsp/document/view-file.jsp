<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:set var="previewUrl"><ttc:filePreview document="${document}" /></c:set>


<div class="file">
    <c:choose>
        <c:when test="${empty previewUrl}">
            <p class="text-center text-muted">
                <span><op:translate key="DOCUMENT_FILE_NO_PREVIEW" /></span>
            </p>
        </c:when>
        
        <c:otherwise>
            <!-- Preview in iframe -->
            <iframe src="${previewUrl}" width="100%" height="800" webkitallowfullscreen="" allowfullscreen=""></iframe>
        </c:otherwise>
    </c:choose>
</div>
