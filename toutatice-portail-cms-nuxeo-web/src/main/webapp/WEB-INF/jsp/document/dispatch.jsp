<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page isELIgnored="false"%>


<c:set var="type" value="${document.type}" />


<c:choose>
    <c:when test="${'Annonce' == type}">
        <jsp:include page="view-annonce.jsp" />
    </c:when>
    
    <c:when test="${'File' == type}">
        <jsp:include page="view-file.jsp" />
    </c:when>
    
    <c:when test="${'Note' == type}">
        <jsp:include page="view-note.jsp" />
    </c:when>
        
    <c:otherwise>
        <jsp:include page="view-default.jsp" />
    </c:otherwise>
</c:choose>
