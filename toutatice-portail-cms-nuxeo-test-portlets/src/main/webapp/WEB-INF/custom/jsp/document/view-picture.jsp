<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="documentURL"><ttc:documentLink document="${document}" /></c:set>
<c:set var="pictureURL"><ttc:documentLink document="${document}" picture="true" /></c:set>
<c:set var="mediumPictureURL"><ttc:documentLink document="${document}" picture="true" displayContext="Medium" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />

<ttc:addMenubarItem id="DOWNLOAD" labelKey="DOWNLOAD" order="20" url="${pictureURL}" glyphicon="download_alt" />

<div>
	PLUGIN OVERLOADED JSP [ADDED BY <b>CMS-TEST-PORTLETS</b>]
</div>


<div>
    <!-- Description -->
    <c:if test="${not empty description}">
        <p class="lead">${description}</p>
    </c:if>

    <!-- Image -->
    <img src="${mediumPictureURL}" alt="${document.title}" class="img-thumbnail">
</div>
