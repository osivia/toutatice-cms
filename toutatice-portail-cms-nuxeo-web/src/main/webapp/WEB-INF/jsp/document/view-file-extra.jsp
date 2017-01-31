<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:set var="url">
    <ttc:documentLink document="${document}" displayContext="download" />
</c:set>
<c:set var="name" value="${document.properties['file:content']['name']}" />
<c:set var="size" value="${document.properties['file:content']['length']}" />

<!-- Glyph -->
<c:if test="${empty glyph}">
    <c:set var="glyph" value="${document.type.glyph}"></c:set>
</c:if>


<div class="panel panel-default">
    <div class="panel-body">
        <!-- Title -->
        <h3 class="h4 text-overflow">
            <i class="${glyph}"></i>
            <span>${name}</span>
        </h3>

        <!-- Size -->
        <p>
            <span><ttc:fileSize size="${size}" /></span>
        </p>

        <p>
            <!-- Drive edit -->
            <c:if test="${not empty driveEditUrl}">
                <a href="${driveEditUrl}" class="btn btn-primary btn-block no-ajax-link">
                    <span><op:translate key="OPEN" /></span>
                </a>
            </c:if>

            <!-- Download -->
            <a href="${url}" target="_blank" class="btn btn-default btn-block no-ajax-link">
                <span><op:translate key="DOWNLOAD" /></span>
            </a>
        </p>
    </div>
</div>


<ttc:include page="view-default-extra.jsp" />
