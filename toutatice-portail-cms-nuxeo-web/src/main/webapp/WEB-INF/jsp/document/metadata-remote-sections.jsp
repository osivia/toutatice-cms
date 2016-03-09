<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="internationalization" prefix="is"%>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>

<c:if test="${not doc.type.folderish}">

	<c:set var="publishedDocuments"
		value="${doc.publishedDocuments}" />
		
		<c:if test="${not empty publishedDocuments}">
		    <!-- DCH: FIXME: add a DTO or CMSItemType variable? -->
		    <c:set var="isInLiveSpace" value="true" />
		    <c:forEach items="${publishedDocuments}" var="publishedDocument">
		          <c:set var="isInPublishSpace" value="${fn:startsWith(doc.path, publishedDocument.nxUrl)}" />
		          <c:set var="isInLiveSpace" value="${isInLiveSpace and not isInPublishSpace}" /> 
		    </c:forEach>
		
			<c:if test="${isInLiveSpace}"> 
			
				<dt><is:getProperty key="DOCUMENT_REMOTE_SECTIONS" /></dt>
				<c:forEach items="${publishedDocuments}" var="publishedDocument">
					<c:set var="publishedDocumentURL"><ttc:transformNxLink link="${publishedDocument.nxUrl}" /></c:set>
					<dd>
						<a href="${publishedDocumentURL}"><span>${publishedDocument.sectionTitle}</span></a>
					</dd>
				</c:forEach>
				
			</c:if>
			
        </c:if>

</c:if>