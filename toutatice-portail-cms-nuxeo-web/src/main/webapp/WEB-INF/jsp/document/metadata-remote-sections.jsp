<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="internationalization" prefix="is"%>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>

<c:if test="${not doc.type.folderish}">

	<c:set var="remote_sections"
		value="${doc.properties['rsi:remoteSections']}" />
		
		<c:if test="${not empty remote_sections}">
		    <!-- DCH: FIXME: add a DTO or CMSItemType variable? -->
		    <c:set var="isInLiveSpace" value="true" />
		    <c:forEach items="${remote_sections}" var="remote_section">
		          <c:set var="isInPublishSpace" value="${fn:startsWith(doc.path, remote_section.sectionPath)}" />
		          <c:set var="isInLiveSpace" value="${isInLiveSpace and not isInPublishSpace}" /> 
		    </c:forEach>
		
		<c:if test="${isInLiveSpace}">
		
			<dt><is:getProperty key="DOCUMENT_REMOTE_SECTIONS" /></dt>
			<span>
				<c:forEach items="${remote_sections}" var="remote_section">
					<c:set var="proxyURL"><ttc:transformNxLink link="${remote_section.proxyURL}" /></c:set>
					<dd>
						<a href="${proxyURL}"><span>${remote_section.sectionTitle}</span></a>
						<c:if test="${remote_section.pending}">
							<i class="glyphicons clock"></i>
						</c:if>
					</dd>
				</c:forEach>
			</span>
			
		</c:if>
			
        </c:if>

</c:if>
