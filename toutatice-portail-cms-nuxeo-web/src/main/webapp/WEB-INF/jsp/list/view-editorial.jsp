<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="internationalization" prefix="is"%>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:forEach var="document" items="${documents}">
	<!-- Document properties -->
	<ttc:documentLink document="${document}" var="link" />
	<c:remove var="target" />
	<c:if test="${link.external}">
		<c:set var="target" value="_blank" />
	</c:if>
	<c:set var="vignetteURL"><ttc:getImageURL document="${document}" property="ttc:vignette" /></c:set>
	<c:set var="iconURL"><ttc:getDocumentIconURL document="${document}" /></c:set>
	<c:set var="typeName"><is:getProperty key="${fn:toUpperCase(document.type.name)}" /></c:set>
	<c:set var="description" value="${document.properties['dc:description']}" />
	<c:set var="author" value="${document.properties['dc:creator']}" />
	<c:set var="date" value="${document.properties['dc:modified']}" />
	<c:if test="${empty date}">
		<c:set var="date" value="${document.properties['dc:created']}" />
	</c:if>


	<div class="media">
		<!-- Vignette -->
		<c:if test="${not empty vignetteURL}">
			<a href="${link.url}" target="${target}" class="pull-left">
			    <img src="${vignetteURL}" alt="" class="media-object" />
			</a>
		</c:if>

		<div class="media-body">
			<!-- Title -->
			<h3 class="h4 media-heading">
				<a href="${link.url}" target="${target}">
				    <span>${document.title}</span>
				</a>

				<!-- Downloadable -->
				<c:if test="${link.downloadable}">
					<i class="halflings halflings-download-alt"></i>
				</c:if>

				<!-- External -->
				<c:if test="${link.external}">
					<i class="halflings halflings-new-window"></i>
				</c:if>
			</h3>

			<!-- Description -->
			<p>${description}</p>

			<!-- Last edition informations -->
			<p class="small">
				<span><is:getProperty key="EDITED_BY" /></span>
				<ttc:user name="${author}" />
				<span><is:getProperty key="DATE_ARTICLE_PREFIX" /></span>
				<span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
			</p>
		</div>
	</div>
</c:forEach>
