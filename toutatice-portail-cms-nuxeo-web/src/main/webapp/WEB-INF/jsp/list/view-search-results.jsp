<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<p class="text-muted">
    <c:choose>
        <c:when test="${totalSize eq 0}">
            <span><op:translate key="LIST_TEMPLATE_SEARCH_RESULTS_NO_RESULT" /></span>
        </c:when>
        
        <c:when test="${totalSize eq 1}">
            <span><op:translate key="LIST_TEMPLATE_SEARCH_RESULTS_ONE_RESULT" /></span>
        </c:when>
        
        <c:otherwise>
            <span><op:translate key="LIST_TEMPLATE_SEARCH_RESULTS_MULTIPLE_RESULTS" args="${totalSize}" /></span>
        </c:otherwise>
    </c:choose>
</p>

<c:if test="${not empty documents}">
    <hr>
</c:if>

<ol class="list-unstyled">
    <c:forEach var="document" items="${documents}" varStatus="status">
        <!-- Document properties -->
        <c:set var="vignetteUrl"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
        <c:set var="description" value="${document.properties['dc:description']}" />
        <c:set var="author" value="${document.properties['dc:lastContributor']}" />
        <c:set var="date" value="${empty document.properties['dc:modified'] ? document.properties['dc:created'] : document.properties['dc:modified']}" />
    
        <li class="media">
            <!-- Vignette -->
            <c:if test="${not empty vignetteUrl}">
                <div class="media-left media-middle hidden-xs">
                    <img src="${vignetteUrl}" alt="" class="media-object">
                </div>
            </c:if>
            
            <div class="media-body media-middle">
                <!-- Title -->
                <h3 class="h4 media-heading">
                    <i class="${document.icon}"></i>
                    <span><ttc:title document="${document}" /></span>
                </h3>
    
                <!-- Description -->
                <c:if test="${not empty description}">
                    <p class="text-pre-wrap">${description}</p>
                </c:if>
    
                <!-- Last edition informations -->
                <c:if test="${not document.type.rootType}">
                    <p class="small text-muted">
                        <span><op:translate key="DOCUMENT_METADATA_MODIFIED_ON" /></span>
                        <span><op:formatRelativeDate value="${date}" /></span>
                        <c:if test="${not empty author}">
                            <span><op:translate key="DOCUMENT_METADATA_BY" /></span>
                            <span><ttc:user name="${author}" /></span>
                        </c:if>
                    </p>
                </c:if>
            </div>
        </li>
    </c:forEach>
</ol>
