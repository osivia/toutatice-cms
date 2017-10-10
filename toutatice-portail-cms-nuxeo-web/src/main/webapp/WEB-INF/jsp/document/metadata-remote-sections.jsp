<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal"
	prefix="op"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice"
	prefix="ttc"%>

<%@ page isELIgnored="false"%>

<c:if test="${not document.type.folderish}">

	<c:set var="publishedDocuments" value="${document.publishedDocuments}" />

	<c:if test="${not empty publishedDocuments}">
		<p>
		<dt>
			<op:translate key="DOCUMENT_REMOTE_SECTIONS" />
		</dt>

		<c:forEach items="${publishedDocuments}" var="publishedDocument">
			<dd>
				<c:set var="publishedDocumentURL">
					<ttc:transformNuxeoUrl url="${publishedDocument.nxUrl}" />
				</c:set>
				<a href="${publishedDocumentURL}"><span>${publishedDocument.sectionTitle}</span></a>

				<span class="label label-info"><op:translate
						key="DOCUMENT_REMOTE_VERSION_PREFIX" />${publishedDocument.versionLabel}</span>
			</dd>
		</c:forEach>
		</p>
	</c:if>

</c:if>
