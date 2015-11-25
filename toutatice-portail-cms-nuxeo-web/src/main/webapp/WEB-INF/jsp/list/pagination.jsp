<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>


<%@ page isELIgnored="false" %>


<portlet:defineObjects />


<c:if test="${nbPages > 1}">
    <!-- Pagination size -->
    <c:set var="size" value="3" />
    
    <!-- Minimum pagination -->
    <c:set var="min" value="0" />
    <c:if test="${currentPage > size}">
        <c:set var="min" value="${currentPage - size}" />
    </c:if>
    
    <!-- Maximum pagination -->
    <c:set var="max" value="${currentPage + size}" />
    <c:if test="${max > (nbPages - 1)}">
        <c:set var="max" value="${nbPages - 1}" />
    </c:if>

    
    <!-- Left -->
    <c:choose>
        <c:when test="${currentPage == 0}">
            <c:set var="leftUrl" value="#" />
            <c:set var="leftClass" value="disabled" />
        </c:when>
        
        <c:otherwise>
            <portlet:renderURL var="leftUrl">
                <portlet:param name="currentPage" value="${currentPage - 1}" />
                <portlet:param name="currentState" value="${renderRequest.windowState}" />
                <portlet:param name="lastSelectors" value="${lastSelectors}" />

            </portlet:renderURL>
        </c:otherwise>
    </c:choose>
    
    <!-- Right -->
    <c:choose>
        <c:when test="${currentPage == max}">
            <c:set var="rightUrl" value="#" />
            <c:set var="rightClass" value="disabled" />
        </c:when>
        
        <c:otherwise>
            <portlet:renderURL var="rightUrl">
                <portlet:param name="currentPage" value="${currentPage + 1}" />
                <portlet:param name="currentState" value="${renderRequest.windowState}" />
                 <portlet:param name="lastSelectors" value="${lastSelectors}" />
            </portlet:renderURL>
        </c:otherwise>
    </c:choose>


    <div class="text-center">
        <ul class="pagination pagination-sm">
            <!-- Start -->
            <c:if test="${min > 0}">
                <portlet:renderURL var="startUrl">
                    <portlet:param name="currentPage" value="0" />
                    <portlet:param name="currentState" value="${renderRequest.windowState}" />
                     <portlet:param name="lastSelectors" value="${lastSelectors}" />

                </portlet:renderURL>
                
                <li>
                    <a href="${startUrl}">
                        <i class="halflings halflings-step-backward"></i>
                    </a>
                </li>
            </c:if>
        
            <!-- Left -->
            <li class="${leftClass}">
                <a href="${leftUrl}">
                    <i class="halflings halflings-triangle-left"></i>
                </a>
            </li>
        
            <c:forEach var="page" begin="${min}" end="${max}">
                <c:set var="url" value="#" />
                <c:remove var="class" />
                <c:choose>
                    <c:when test="${page == currentPage}">
                        <c:set var="class" value="active" />
                    </c:when>
                    
                    <c:otherwise>
                        <portlet:renderURL var="url">
                            <portlet:param name="currentPage" value="${page}" />
                            <portlet:param name="currentState" value="${renderRequest.windowState}" />
                            <portlet:param name="lastSelectors" value="${lastSelectors}" />
                        </portlet:renderURL>
                    </c:otherwise>
                </c:choose>

                <li class="${class}">
                    <a href="${url}">
                        <span>${page + 1}</span>
                        
                        <c:if test="${page == currentPage}">
                            <span class="sr-only"> (<op:translate key="CURRENT" />)</span>
                        </c:if>
                    </a>
                </li>
            </c:forEach>
            
            <!-- Right -->
            <li class="${rightClass}">
                <a href="${rightUrl}">
                    <i class="halflings halflings-triangle-right"></i>
                </a>
            </li>
        </ul>
    </div>
</c:if>
