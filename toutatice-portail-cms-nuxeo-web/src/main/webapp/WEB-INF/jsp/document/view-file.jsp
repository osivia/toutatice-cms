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


<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${documentURL}" glyphicon="halflings halflings-download-alt" />

<div class="file">
    <p>
        <img src="${iconURL}" alt="${typeName}">
        <a href="${documentURL}">${fileName}</a>
        <span>(<ttc:formatFileSize size="${fileSize}" />)</span>
    </p>
</div>
