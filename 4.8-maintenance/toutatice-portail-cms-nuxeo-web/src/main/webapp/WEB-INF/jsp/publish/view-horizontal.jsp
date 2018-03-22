<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="nuxeo-publish-navigation">
    <c:choose>
        <c:when test="${not empty displayItem}">
            <nav class="menu-horizontal">
                <!-- Title -->
                <h3 class="hidden"><op:translate key="MENU_TITLE_HORIZONTAL" /></h3>
                
                <!-- Menu -->
                <ul class="list-inline">
                    <!-- Home -->
                    <li class="visible-xs">
                        <a href="${displayItem.url}"
                            <c:if test="${displayItem.current}">class="active"</c:if>
                        >            
                            <i class="halflings halflings-home"></i>
                            <span class=sr-only>${displayItem.title}</span>
                        </a>
                    </li>


                    <!-- Children -->
                    <c:forEach var="child" items="${displayItem.children}">
                        <li>
                            <a href="${child.url}"
                                <c:if test="${child.selected}">class="active"</c:if>
                            >            
                                <span>${child.title}</span>
                            </a>
                            
                            
                            <!-- Sub-menu -->
                            <c:if test="${child.selected and not empty child.children}">
                                <div class="visible-xs">
                                    <c:set var="parent" value="${child}" scope="request" />
                                    <c:set var="level" value="2" scope="request" />
                                    <jsp:include page="display-horizontal-items.jsp" />
                                </div>
                            </c:if>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:when>
    
        <c:otherwise>
            <p class="text-danger">
                <i class="halflings halflings-exclamation-sign"></i>
                <span><op:translate key="MESSAGE_PATH_UNDEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
