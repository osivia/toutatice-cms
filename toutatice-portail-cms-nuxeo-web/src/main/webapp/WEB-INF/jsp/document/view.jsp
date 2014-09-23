<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="document clearfix">
    <c:choose>
        <c:when test="${not empty document}">
            <!-- Metadata -->
            <c:if test="${metadata}">
                <jsp:include page="metadata.jsp" />
            </c:if>
            
            <c:choose>
                <c:when test="${onlyDescription}">
                    <jsp:include page="only-description.jsp" />
                </c:when>
                
                <c:otherwise>
                    <!-- Document view -->
                    <jsp:include page="dispatch.jsp" />
                    
                    <!-- Document attachments view -->
                    <jsp:include page="attachments.jsp" />
                    
                    <!-- Document comments view -->
                    <jsp:include page="comments.jsp" />
                </c:otherwise>
            </c:choose>
        </c:when>
        
        <c:otherwise>
            <p class="lead text-danger">
                <i class="glyphicons halflings exclamation-sign"></i>
                <span><is:getProperty key="MESSAGE_PATH_NOT_DEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
