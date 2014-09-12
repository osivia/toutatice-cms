<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="zoom">
    <c:choose>
        <c:when test="${empty template}">
            <jsp:include page="view-zoom-default.jsp" />
        </c:when>
        
        <c:otherwise>
            <jsp:include page="view-zoom-${fn:toLowerCase(template)}.jsp" />
        </c:otherwise>
    </c:choose>
</div>
