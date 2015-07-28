<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<!-- Customize menubar -->
<jsp:include page="menubar.jsp" />

<div class="list clearfix">
    <c:choose>
        <c:when test="${empty error}">
            <!-- Request -->
            <c:if test="${not empty nuxeoRequest}">
                <div class="alert alert-info">${nuxeoRequest}</div>
            </c:if>
        
            <!-- Documents -->
            <div class="no-ajax-link">
                <ttc:custom-include page="view-${style}.jsp" />
            </div>
            
            <!-- Pagination -->
            <jsp:include page="pagination.jsp" />
        </c:when>
        
        <c:otherwise>
            <p class="text-danger">
                <i class="halflings halflings-exclamation-sign"></i>
                <span>${error}</span>
            </p>
            
            <c:if test="${not empty errorMessage}">
                <p class="text-danger">${errorMessage}</p>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>
