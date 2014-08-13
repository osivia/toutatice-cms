<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<c:choose>
    <c:when test="${not empty displayItem}">
        <ul class="list-inline">
            <c:forEach var="child" items="${displayItem.children}">
                <c:remove var="current" />
                <c:if test="${child.current}">
                    <c:set var="current" value="active" />
                </c:if>

                <li>
                    <a href="${child.url}" class="${current}">            
                        <span>${child.title}</span>
                    </a>
                </li>
            </c:forEach>
        </ul>
    </c:when>

    <c:otherwise>
        <p class="text-danger">
            <i class="glyphicons halflings exclamation-sign"></i>
            <span><is:getProperty key="MESSAGE_PATH_NOT_DEFINED" /></span>
        </p>
    </c:otherwise>
</c:choose>
