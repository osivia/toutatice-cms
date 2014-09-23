<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="documentURL"><ttc:getDocumentURL displayContext="download" /></c:set>
<c:set var="iconURL"><ttc:getDocumentIconURL /></c:set>
<c:set var="typeName"><is:getProperty key="${document.type}" /></c:set>
<c:set var="fileName" value="${document.properties['file:filename']}" />
<c:set var="fileSize"><ttc:getFileSize /></c:set>


<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${documentURL}" glyphicon="download_alt" />

<div class="file">
    <p>
        <img src="${iconURL}" alt="${typeName}">
        <a href="${documentURL}">${fileName}</a>
        <span>(${fileSize})</span>
    </p>
</div>
