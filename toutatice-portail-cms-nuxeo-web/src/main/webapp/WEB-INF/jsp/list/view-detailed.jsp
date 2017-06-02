<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled">
    <c:forEach var="document" items="${documents}" varStatus="status">
        <!-- Document properties -->
        
        <!-- Description -->
        <c:set var="description" value="${document.properties['dc:description']}" />

        <!-- Author -->
        <c:set var="author" value="${document.properties['dc:lastContributor']}" />
        
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
                <p class="text-pre-wrap">${description}</p>
            </c:if>
            
            <!-- Last edition informations -->
            <c:if test="${not document.type.root}">
                <p class="text-muted">
                    <span><op:translate key="DOCUMENT_METADATA_MODIFIED_ON" /></span>
                    <span><op:formatRelativeDate value="${date}" /></span>
                    <span><op:translate key="DOCUMENT_METADATA_BY" /></span>
                    <span><ttc:user name="${author}" /></span>
                </p>
            </c:if>
            
            
            <!-- Separator -->
            <c:if test="${not status.last}">
                <hr>
            </c:if>
        </li>
    </c:forEach>
    
    
    <c:if test="${empty documents}">
        <li>
            <p class="text-center">
                <span class="text-muted"><op:translate key="LIST_NO_ITEMS" /></span>
            </p>
        </li>
    </c:if>
</ul>
