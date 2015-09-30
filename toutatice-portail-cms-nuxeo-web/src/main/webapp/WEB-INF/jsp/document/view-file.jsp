<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:choose>
    <c:when test="${'Picture' eq document.type.name}">
        <ttc:documentLink document="${document}" picture="true" var="link" />
    </c:when>
    
    <c:otherwise>
        <ttc:documentLink document="${document}" displayContext="download" var="link" />
    </c:otherwise>
</c:choose>

<c:set var="description" value="${document.properties['dc:description']}" />

<c:set var="fileName" value="${document.properties['file:content']['name']}" />
<c:set var="fileSize" value="${document.properties['file:content']['length']}" />
<c:set var="mimeType" value="${document.properties['file:content']['mime-type']}" />


<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${link.url}" glyphicon="glyphicons glyphicons-download-alt" />

<div class="file">
    <!-- Description -->
    <c:if test="${not empty description}">
        <p>${description}</p>
    </c:if>

    <p>
        <!-- Title -->
        <i class="${document.type.glyph}"></i>
        <a href="${link.url}" class="no-ajax-link">${fileName}</a>
        
        <!-- Size -->
        <span>(<ttc:fileSize size="${fileSize}" />)</span>
    </p>

    
    <c:if test="${('Picture' eq document.type.name)}">
        <!-- Picture -->
        <ttc:documentLink document="${document}" picture="true" displayContext="Medium" var="pictureLink" />
        
        <hr>
        
        <a href="${link.url}" class="thumbnail fancybox no-ajax-link">
            <img src="${pictureLink.url}" alt="">
        </a>
    </c:if>
    
    
    <c:if test="${('Audio' eq document.type.name) or (('File' eq document.type.name) and fn:startsWith(mimeType, 'audio/'))}">
        <!-- Audio player -->
        <hr>
        
        <div>
            <audio src="${documentURL}" controls="controls" preload="metadata" class="img-responsive">
                <source src="${documentURL}" type="${mimeType}">
            </audio>
        </div>
    </c:if>
    
    
    <c:if test="${('Video' eq document.type.name) or (('File' eq document.type.name) and fn:startsWith(mimeType, 'video/'))}">
        <!-- Video player -->
        <hr>
    
        <div class="embed-responsive embed-responsive-16by9">
            <video src="${documentURL}" controls="controls" preload="metadata" class="embed-responsive-item">
                <source src="${documentURL}" type="${mimeType}">
            </video>
        </div>
    </c:if>
</div>
