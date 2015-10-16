<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<div class="document clearfix">
    <c:choose>
        <c:when test="${not empty document}">
            <c:choose>
                <c:when test="${onlyRemoteSections}">
                    <jsp:include page="only-remote-sections.jsp" />
                </c:when>
                
                <c:otherwise>
        
		            <c:choose>
		                <c:when test="${onlyDescription}">
		                    <jsp:include page="only-description.jsp" />
		                </c:when>
		                
		                <c:otherwise>
		                    <!-- Document view -->
		                    <ttc:include page="view-${dispatchJsp}.jsp" />
		                    
		                    <!-- Document attachments view -->
		                    <c:if test="${attachments}">
		                        <jsp:include page="attachments.jsp" />
		                    </c:if>
		                    
		                    <!-- Metadata -->
		                    <c:if test="${metadata}">
		                        <jsp:include page="metadata.jsp" />
		                    </c:if>
		                    
		                    <!-- Document comments view -->
                            <ttc:comments document="${document}" />
		                </c:otherwise>
		            </c:choose>
		            
		         </c:otherwise>   
	         </c:choose>
        </c:when>
        
        <c:otherwise>
            <p class="text-danger">
                <i class="halflings halflings-exclamation-sign"></i>
                <span><op:translate key="MESSAGE_PATH_UNDEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
