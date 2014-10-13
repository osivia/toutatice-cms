<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="nuxeo-publish-navigation">
    <c:choose>
        <c:when test="${not empty displayItem}">
            <c:if test="${not empty displayItem.children}">
                <nav class="menu-default">
                    <!-- Title -->
                    <h3 class="hidden"><is:getProperty key="MENU_TITLE_DEFAULT" /></h3>
            
                    <!-- Current item ? -->
                    <c:remove var="current" />
                    <c:if test="${displayItem.current}">
                        <c:set var="current" value="btn-primary" />
                    </c:if>
                
                    <!-- Navigation home link -->
                    <a href="${displayItem.url}" class="btn btn-default btn-block visible-xs ${current}">
                        <i class="glyphicons halflings home"></i>
                        <span>${displayItem.title}</span>
                    </a>
                
                    <!-- Menu -->
                    <c:set var="parent" value="${displayItem}" scope="request" />
                    <c:set var="level" value="1" scope="request" />
                    <jsp:include page="display-items.jsp" />
                </nav>
            </c:if>
        </c:when>
    
        <c:otherwise>
            <p class="lead text-danger">
                <i class="glyphicons halflings exclamation-sign"></i>
                <span><is:getProperty key="MESSAGE_PATH_UNDEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
