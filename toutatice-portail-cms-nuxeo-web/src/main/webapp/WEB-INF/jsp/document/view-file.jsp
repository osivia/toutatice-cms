<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="url"><ttc:documentLink document="${document}" displayContext="download" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />
<c:set var="previewURL"><ttc:filePreview document="${document}" /></c:set>


<!-- Download menubar item -->
<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${url}" target="_blank" glyphicon="glyphicons glyphicons-download-alt" />


<div class="file">
    <c:if test="${not empty previewURL}">
         <iframe src="${previewURL}" width="100%" height="800" webkitallowfullscreen="" allowfullscreen=""></iframe>
    </c:if>
</div>
