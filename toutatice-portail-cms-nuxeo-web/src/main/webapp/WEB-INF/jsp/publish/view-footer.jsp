<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="nuxeo-publish-navigation">
    <c:choose>
        <c:when test="${not empty displayItem}">
            <nav class="menu-footer">
                <!-- Title -->
                <h3 class="hidden"><is:getProperty key="MENU_TITLE_FOOTER" /></h3>
    
                <!-- Menu -->
                <ul class="list-inline">
                    <c:forEach var="firstLevelChild" items="${displayItem.children}">        
                        <li>
                            <p class="lead">
                                <a href="${firstLevelChild.url}">            
                                    <span>${firstLevelChild.title}</span>
                                </a>
                            </p>
                            
                            <c:if test="${not empty firstLevelChild.children}">
                                <!-- Level 2 -->
                                <ul class="list-unstyled">
                                    <c:forEach var="secondLevelChild" items="${firstLevelChild.children}">
                                        <li>
                                            <a href="${secondLevelChild.url}">            
                                                <span>${secondLevelChild.title}</span>
                                            </a>
                                            
                                            <c:if test="${not empty secondLevelChild.children}">
                                                <!-- Level 3 -->
                                                <ul class="list-unstyled">
                                                    <c:forEach var="thirdLevelChild" items="${secondLevelChild.children}">
                                                        <li>
                                                            <a href="${thirdLevelChild.url}">            
                                                                <span>${thirdLevelChild.title}</span>
                                                            </a>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:if>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:if>
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
