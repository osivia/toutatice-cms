<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<c:choose>
    <c:when test="${not empty fragmentType}">
        <div class="fragment" data-fragment-type="${fragmentType.key}">
            <!-- Message -->
            <c:if test="${not empty messageKey}">
                <p class="text-danger">
    	            <i class="halflings halflings-exclamation-sign"></i>
    	            <span><op:translate key="${messageKey}" /></span>
    	        </p>
            </c:if>
        
            <!-- Fragment -->
            <c:if test="${not empty fragmentType.module.viewJSPName}">
                <ttc:include page="view-${fragmentType.module.viewJSPName}.jsp" />
            </c:if>
        </div>
    </c:when>
    
    <c:otherwise>
        <p class="text-danger">
            <i class="halflings halflings-exclamation-sign"></i>
            <span><op:translate key="FRAGMENT_MESSAGE_NOT_CONFIGURED" /></span>
        </p>
    </c:otherwise>
</c:choose>
