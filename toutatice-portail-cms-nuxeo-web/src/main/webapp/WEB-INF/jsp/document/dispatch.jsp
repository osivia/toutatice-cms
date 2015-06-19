<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page isELIgnored="false"%>


<c:set var="type" value="${document.type.name}" />


<c:choose>
    <c:when test="${'Annonce' eq type}">
        <jsp:include page="view-annonce.jsp" />
    </c:when>
    
    <c:when test="${('File' eq type) or ('Picture' eq type) or ('Audio' eq type) or ('Video' eq type)}">
        <jsp:include page="view-file.jsp" />
    </c:when>
    
    <c:when test="${'Note' eq type}">
        <jsp:include page="view-note.jsp" />
    </c:when>
    
    <c:when test="${'ContextualLink' eq type}">
        <jsp:include page="view-contextual-link.jsp" />
    </c:when>
        
    <c:otherwise>
        <jsp:include page="view-default.jsp" />
    </c:otherwise>
</c:choose>
