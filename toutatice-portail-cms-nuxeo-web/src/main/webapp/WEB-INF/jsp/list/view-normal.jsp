<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled">
    <c:forEach var="document" items="${documents}">
        <!-- Document properties -->
        
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
            <p>
                <!-- Title -->
                <span><ttc:title document="${document}" icon="true" /></span>
                
                <!-- Last edition informations -->
                <c:if test="${not document.type.rootType}">
                    <br>
                    
                    <small class="text-muted">
                        <span><op:translate key="DOCUMENT_METADATA_MODIFIED_ON" /></span>
                        <span><op:formatRelativeDate value="${date}" /></span>
                        <span><op:translate key="DOCUMENT_METADATA_BY" /></span>
                        <span><ttc:user name="${author}" /></span>
                    </small>
                </c:if>
            </p>
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
