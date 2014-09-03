<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="nuxeo-publish-navigation">
    <c:choose>
        <c:when test="${not empty displayItem}">
            <nav class="menu-horizontal">
                <!-- Title -->
                <h3 class="hidden"><is:getProperty key="MENU_TITLE_HORIZONTAL" /></h3>
                
                <!-- Menu -->
                <ul class="list-inline">
                    <!-- Home -->
                    <c:remove var="current" />
                    <c:if test="${displayItem.current}">
                        <c:set var="current" value="active" />
                    </c:if>
                    <li class="visible-xs">
                        <a href="${displayItem.url}" class="${current}">            
                            <i class="glyphicons halflings home"></i>
                            <span class=sr-only>${displayItem.title}</span>
                        </a>
                    </li>
                
                
                    <!-- Children -->
                    <c:forEach var="child" items="${displayItem.children}">
                        <c:remove var="selected" />
                        <c:if test="${child.selected}">
                            <c:set var="selected" value="active" />
                        </c:if>
        
                        <li>
                            <a href="${child.url}" class="${selected}">            
                                <span>${child.title}</span>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:when>
    
        <c:otherwise>
            <p class="text-danger">
                <i class="glyphicons halflings exclamation-sign"></i>
                <span><is:getProperty key="MESSAGE_PATH_NOT_DEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
