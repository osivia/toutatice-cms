<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="url"><ttc:documentLink document="${document}" displayContext="download" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />
<c:set var="name" value="${document.properties['file:content']['name']}" />
<c:set var="size" value="${document.properties['file:content']['length']}" />
<c:set var="mimeType" value="${document.properties['file:content']['mime-type']}" />


<!-- Download menubar item -->
<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${url}" target="_blank" glyphicon="glyphicons glyphicons-download-alt" />


<div class="file">
    <!-- Description -->
    <c:if test="${not empty description}">
        <p>${description}</p>
    </c:if>

    <p>
        <!-- Title -->
        <i class="${document.type.glyph}"></i>
        <a href="${url}" target="_blank" class="no-ajax-link">${name}</a>
        
        <!-- Size -->
        <span>(<ttc:fileSize size="${size}" />)</span>
    </p>
</div>
