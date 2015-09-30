<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled">
    <c:forEach var="document" items="${documents}" varStatus="status">
        <!-- Document properties -->
        
        <!-- Description -->
        <c:set var="description" value="${document.properties['dc:description']}" />

        <!-- Author -->
        <c:set var="author" value="${document.properties['dc:creator']}" />
        
        <!-- Date -->
        <c:set var="date" value="${document.properties['dc:issued']}" />
        <c:if test="${empty date}">
            <c:set var="date" value="${document.properties['dc:modified']}" />
        </c:if>
        <c:if test="${empty date}">
            <c:set var="date" value="${document.properties['dc:created']}" />
        </c:if>
    
        
        <li>
            <!-- Title -->
            <p><ttc:title document="${document}" icon="true" /></p>
            
            <!-- Description -->
            <c:if test="${not empty description}">
                <p>${description}</p>
            </c:if>
            
            <!-- Informations -->
            <p class="text-muted">
                <span><op:translate key="EDITED_BY" /></span>
                <ttc:user name="${author}" linkable="true" />
                <span><op:translate key="DATE_ARTICLE_PREFIX" /></span>
                <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
            </p>
            
            
            <!-- Separator -->
            <c:if test="${not status.last}">
                <hr>
            </c:if>
        </li>
    </c:forEach>
</ul>
