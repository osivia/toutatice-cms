<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<c:choose>
    <c:when test="${empty template}">
        <ttc:include page="view-default.jsp" />
    </c:when>
    
    <c:otherwise>
        <ttc:include page="view-${template}.jsp" />
    </c:otherwise>
</c:choose>
