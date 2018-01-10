<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:set var="url">
    <ttc:documentLink document="${document}" displayContext="download" />
</c:set>
<c:set var="name" value="${document.properties['file:content']['name']}" />
<c:set var="size" value="${document.properties['file:content']['length']}" />

<c:choose>
	<c:when test="${isEditableByUser}">
		<c:set var="onlyOfficeLabel" value="LIVE_EDIT" />
	</c:when>
	<c:otherwise>
		<c:set var="onlyOfficeLabel" value="LIVE_VIEW" />
	</c:otherwise>
</c:choose>

<div class="panel panel-default">
    <div class="panel-body">
        <!-- Title -->
        <h3 class="h4 text-overflow">
            <i class="${document.icon}"></i>
            <span>${name}</span>
        </h3>

        <!-- Size -->
        <p>
            <span><ttc:fileSize size="${size}" /></span>
        </p>

        <div>
            <!-- Drive edit and Live edit-->
            <c:choose>
            	<c:when test="${not empty onlyofficeEditUrl}">
            		<c:choose>
            			<c:when test="${not empty driveEditUrl}">
            				<div class="btn-group flex-display" role="group">
	            				<a href="${onlyofficeEditUrl}" class="btn btn-primary no-ajax-link">
			                        <span><op:translate key="LIVE_EDIT" /></span>
			                    </a>
			                   	<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
							    	<span class="caret"></span>
							    </button>
			                    <ul class="dropdown-menu dropdown-menu-right" aria-labelledby="dLabel">
			                    	<li>
				                    	<a href="${onlyofficeEditUrl}" class="no-ajax-link">
				                    		<i class="halflings halflings-pencil"></i>
					                        <span><op:translate key="${onlyOfficeLabel}" /></span>
					                    </a>
			                    	</li>
			                    	<li>
					                    <a href="${driveEditUrl}" class="no-ajax-link">
					                    	<i class="halflings halflings-folder-open"></i>
					                        <span><op:translate key="DRIVE_EDIT" /></span>
					                    </a>
				                    </li>
			                    </ul>
		                    </div>
            			</c:when>
            			<c:when test="${driveEnabled}">
            				<div class="btn-group flex-display" role="group">
	            				<a href="${onlyofficeEditUrl}" class="btn btn-primary no-ajax-link">
			                        <span><op:translate key="LIVE_EDIT" /></span>
			                    </a>
			                   	<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
							    	<span class="caret"></span>
							    </button>
			                    <ul class="dropdown-menu dropdown-menu-right" aria-labelledby="dLabel">
			                    	<li>
				                    	<a href="${onlyofficeEditUrl}" class="no-ajax-link">
				                    		<i class="halflings halflings-pencil"></i>
					                        <span><op:translate key="${onlyOfficeLabel}" /></span>
					                    </a>
			                    	</li>
			                    	<li class="disabled" data-toggle="tooltip" title="<op:translate key='MESSAGE_DRIVE_CLIENT_NOT_STARTED' />">
				                    	<a href="#" class="disabled">
				                    		<i class="halflings halflings-folder-open"></i>
					                        <span><op:translate key="DRIVE_EDIT" /></span>
					                    </a>
			                    	</li>
			                    </ul>
		                    </div>
            			</c:when>
            			<c:otherwise>
            				<a href="${onlyofficeEditUrl}" class="btn btn-primary btn-block no-ajax-link">
		                        <span><op:translate key="${onlyOfficeLabel}" /></span>
		                    </a>
            			</c:otherwise>
            		</c:choose>
            	</c:when>
            	<c:otherwise>
            		<c:choose>
	            		<c:when test="${not empty driveEditUrl}">
		                    <a href="${driveEditUrl}" class="btn btn-primary btn-block no-ajax-link">
		                        <span><op:translate key="DRIVE_EDIT" /></span>
		                    </a>
		                </c:when>
		                
		                <c:when test="${driveEnabled}">
		                    <div class="alert alert-warning">
		                        <span><op:translate key="MESSAGE_DRIVE_CLIENT_NOT_STARTED" /></span>
		                    </div>
		                
		                    <a href="#" class="btn btn-primary btn-block disabled">
		                        <span><op:translate key="DRIVE_EDIT" /></span>
		                    </a>
		                </c:when>
            		</c:choose>
	                
            	</c:otherwise>
            </c:choose>

            <!-- Download -->
            <a href="${url}" target="_blank" class="btn btn-default btn-block no-ajax-link">
                <span><op:translate key="DOWNLOAD" /></span>
            </a>
        </div>
    </div>
</div>


<ttc:include page="view-default-extra.jsp" />
