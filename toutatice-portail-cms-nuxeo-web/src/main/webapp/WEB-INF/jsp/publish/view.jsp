<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="nuxeo-publish-navigation">
    <c:choose>
        <c:when test="${not empty displayItem and not empty displayItem.children}">
            <nav class="menu-default">
                <!-- Title -->
                <h3 class="hidden"><is:getProperty key="MENU_TITLE_DEFAULT" /></h3>
        
                <!-- Navigation home link -->
                <c:if test="${not displayItem.current}">
                    <p class="visible-xs">
                        <a href="${displayItem.url}" class="btn btn-link">
                            <i class="halflings halflings-home"></i>
                            <span>${displayItem.title}</span>
                        </a>
                    </p>
                </c:if>
            
                <!-- Menu -->
                <c:set var="parent" value="${displayItem}" scope="request" />
                <c:set var="level" value="1" scope="request" />
                <jsp:include page="display-items.jsp" />
            </nav>
        </c:when>
        
        <c:when test="${not empty displayItem}">
            <p class="text-muted text-center"><is:getProperty key="NO_ITEMS" /></p>
        </c:when>
    
        <c:otherwise>
            <p class="text-danger">
                <i class="halflings halflings-exclamation-sign"></i>
                <span><is:getProperty key="MESSAGE_PATH_UNDEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
