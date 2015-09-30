<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:forEach var="document" items="${documents}">
	<!-- Document properties -->
    
    <!-- Vignette -->
    <c:set var="vignetteURL"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
    
    <!-- Author -->
    <c:set var="author" value="${document.properties['dc:creator']}" />

    <!-- Description -->
    <c:set var="description" value="${document.properties['dc:description']}" />
    
    <!-- Date -->
    <c:set var="date" value="${document.properties['dc:modified']}" />
	<c:if test="${empty date}">
		<c:set var="date" value="${document.properties['dc:created']}" />
	</c:if>


	<div class="media">
		<!-- Vignette -->
        <c:if test="${not empty vignetteURL}">
            <div class="media-left">
                <img src="${vignetteURL}" alt="" class="media-object">
            </div>
        </c:if>

		<div class="media-body">
			<!-- Title -->
			<h3 class="h4 media-heading"><ttc:title document="${document}" /></h3>

			<!-- Description -->
            <c:if test="${not empty description}">
                <p>${description}</p>
            </c:if>

			<!-- Last edition informations -->
			<p class="small">
				<span><op:translate key="EDITED_BY" /></span>
				<ttc:user name="${author}" />
				<span><op:translate key="DATE_ARTICLE_PREFIX" /></span>
				<span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
			</p>
		</div>
	</div>
</c:forEach>
