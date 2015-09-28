<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<c:set var="imageURL">
	<ttc:pictureLink document="${doc}" property="annonce:image" />
</c:set>
<ttc:documentLink document="${doc}" var="link" />

<div class="media">
	<c:if test="${not empty imageURL}">
		<div class="media-left">
			<img src="${imageURL}" alt="" class="media-object">
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
	</div>
</div>