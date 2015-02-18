<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="document clearfix">
    <c:choose>
        <c:when test="${not empty document}">
            <c:choose>
                <c:when test="${onlyDescription}">
                    <jsp:include page="only-description.jsp" />
                </c:when>
                
                <c:otherwise>
                    <!-- Document view -->
                    <jsp:include page="dispatch.jsp" />
                    
                    <!-- Document attachments view -->
                    <c:if test="${attachments}">
                        <jsp:include page="attachments.jsp" />
                    </c:if>
                    
                    <!-- Metadata -->
                    <c:if test="${metadata}">
                        <jsp:include page="metadata.jsp" />
                    </c:if>
                    
                    <!-- Document comments view -->
                    <jsp:include page="comments.jsp" />
                </c:otherwise>
            </c:choose>
        </c:when>
        
        <c:otherwise>
            <p class="text-danger">
                <i class="halflings halflings-exclamation-sign"></i>
                <span><is:getProperty key="MESSAGE_PATH_UNDEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
