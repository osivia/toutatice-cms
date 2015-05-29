<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="internationalization" prefix="is"%>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>

<c:if test="${not doc.type.folderish}">

	<c:set var="remote_sections"
		value="${doc.properties['rsi:remoteSections']}" />
	
	<c:if test="${not empty remote_sections}">
		<p>
			<strong><is:getProperty key="DOCUMENT_REMOTE_SECTIONS" /></strong> <span>
				: </span>
			<span>
				<c:forEach items="${remote_sections}" var="remote_section">
					<c:set var="sectionURL"><ttc:transformNxLink link="${remote_section.sectionURL}" /></c:set>
					<p>
						<a href="${sectionURL}"><span
							class="label label-default">${remote_section.sectionPath}</span></a>
						<span class="label label-primary">${remote_section.version}</span>
						<c:if test="${remote_section.pending}">
							<i class="glyphicons clock"></i>
						</c:if>
					</p>
				</c:forEach>
			</span>
		</p>
	</c:if>

</c:if>
