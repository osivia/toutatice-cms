<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<script type="text/javascript" src="/toutatice-portail-cms-nuxeo/components/PDFViewer/preview.js"></script>

<c:set var="previewUrl"><ttc:filePreview document="${document}"/></c:set>
<c:set var="fileName">${document.title}</c:set>

<div class="file">
    <c:choose>
        <c:when test="${empty previewUrl}">
            <div>
                <div class="alert alert-warning" role="alert"><span><op:translate key="DOCUMENT_FILE_NO_PREVIEW" /></span></div>
            </div>
        </c:when>
        
        <c:otherwise>
            
            <div class="progress">
			  <div class="progress-bar progress-bar-striped active loadBar" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;">
			    <span class="sr-only"><span><op:translate key="DOCUMENT_FILE_LOADING" /></span></span>
			  </div>
			</div>
			<div class="progress">
				<div class="progress-bar progress-bar-striped progress-bar-success active downloadBar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
	                 <span class="sr-only">0%</span><span class="sr-only"> <op:translate key="DOCUMENT_FILE_DOWNLOADED" /></span>
                </div>
            </div>
        
        
            <!-- Preview in iframe -->
            <iframe src="/toutatice-portail-cms-nuxeo/components/PDFViewer/web/viewer.html" width="100%" height="800" webkitallowfullscreen="" allowfullscreen="" class="pdf-preview-iframe hidden" data-preview-url="${previewUrl}" data-filename="${fileName}" onload="downloadPreview();"></iframe>
            
            <div class="file-preview-unavailable hidden">
                <div class="alert alert-warning" role="alert"><span><op:translate key="DOCUMENT_FILE_PREVIEW_UNAVAILABLE" /></span></div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
