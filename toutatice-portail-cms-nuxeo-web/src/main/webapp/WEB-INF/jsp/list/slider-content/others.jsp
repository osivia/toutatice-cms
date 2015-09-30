<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<c:set var="description" value="${doc.properties['dc:description']}" />
<c:set var="thumbnailURL">
	<ttc:pictureLink document="${doc}" property="ttc:vignette" />
</c:set>
<ttc:documentLink document="${doc}" var="link" />
<c:set var="date" value="${doc.properties['dc:issued']}" />
<c:if test="${empty date}">
	<c:set var="date" value="${doc.properties['dc:modified']}" />
</c:if>
<c:if test="${empty date}">
	<c:set var="date" value="${doc.properties['dc:created']}" />
</c:if>

<div class="media">
	<c:if test="${not empty thumbnailURL}">
		<div class="media-left">
			<img src="${thumbnailURL}" alt="" class="media-object">
		</div>
	</c:if>

	<div class="media-body">
		<!-- Title -->
		<h3 class="media-heading">
			<a href="${link.url}"
				<c:if test="${link.external}">target="_blank"</c:if>> <span>${doc.title}</span>
			</a>

			<c:if test="${link.external}">
				<i class="glyphicons halflings new_window"></i>
			</c:if>
		</h3>

		<!-- Date -->
		<p class="text-muted">
			<fmt:formatDate value="${date}" type="date" dateStyle="long" />
		</p>

	</div>
</div>


