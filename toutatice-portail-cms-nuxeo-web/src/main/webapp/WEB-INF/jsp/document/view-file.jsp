<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="documentURL"><ttc:documentLink document="${document}" displayContext="download" /></c:set>
<c:set var="iconURL"><ttc:getDocumentIconURL document="${document}" /></c:set>
<c:set var="typeName"><is:getProperty key="${fn:toUpperCase(document.type.name)}" /></c:set>
<c:set var="fileName" value="${document.properties['file:filename']}" />
<c:set var="fileSize" value="${document.properties['file:content']['length']}" />
<c:set var="description" value="${document.properties['dc:description']}" />
<c:set var="mimeType" value="${document.properties['file:content']['mime-type']}" />


<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${documentURL}" glyphicon="halflings halflings-download-alt" />

<div class="file">
    <c:if test="${not empty description}">
        <p>${description}</p>
    </c:if>

    <div class="media">
        <div class="media-left">
            <img src="${iconURL}" alt="${typeName}" class="media-object">
        </div>
        
        <div class="media-body">
            <p>
                <a href="${documentURL}">${fileName}</a>
                <span>(<ttc:formatFileSize size="${fileSize}" />)</span>
            </p>
        </div>
    </div>
    
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
