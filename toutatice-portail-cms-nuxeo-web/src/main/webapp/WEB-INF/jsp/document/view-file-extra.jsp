<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:set var="url">
    <ttc:documentLink document="${document}" displayContext="download" />
</c:set>
<c:set var="name" value="${document.properties['file:content']['name']}" />
<c:set var="size" value="${document.properties['file:content']['length']}" />


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

		<c:choose>
	        <c:when test="${driveEnabled and empty driveEditUr}">
	            <div class="alert alert-warning">
	                <span><op:translate key="MESSAGE_DRIVE_CLIENT_NOT_STARTED" /></span>
	            </div>
	       </c:when>
	    </c:choose>

        <div>
            <c:choose>
            	<c:when test="${not isEditableByUser}">
            		<!-- readonly -->
            		<c:choose>
            			<c:when test="${not empty onlyofficeEditCollabUrl}">
            			
		            		<!-- onlyoffice in read mode -->
		            		<a href="${onlyofficeEditCollabUrl}" class="btn btn-primary btn-block no-ajax-link">
				            	<span><op:translate key="ONLYOFFICE_VIEW" /></span>
				            </a>
				     	</c:when>
            		</c:choose>

            	</c:when> 
            	<c:otherwise>
            		<!-- write -->
            		
            		<c:choose>
            			
            			<c:when test="${not empty onlyofficeEditCollabUrl}">
            				<!-- onlyoffice in write mode -->
            				<div class="btn-group flex-display" role="group">
	            				<a href="${onlyofficeEditLockUrl}" class="btn btn-primary no-ajax-link">
			                        <span><op:translate key="ONLYOFFICE_EDIT_LOCK" /></span>
			                    </a>
			                   	<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
							    	<span class="caret"></span>
							    </button>
			                    <ul class="dropdown-menu dropdown-menu-right" aria-labelledby="dLabel">
			                    	<li>
			                    		<!--  onlyoffice in write mode with lock -->
				                    	<a href="${onlyofficeEditLockUrl}" class="no-ajax-link">
				                    		<i class="halflings halflings-pencil"></i>
					                        <span><op:translate key="ONLYOFFICE_EDIT_LOCK" /></span>
					                    </a>
			                    	</li>
			                    	<li>
			                    		<!--  onlyoffice in write mode collaborative -->
				                    	<a href="${onlyofficeEditCollabUrl}" class="no-ajax-link">
				                    		<i class="halflings halflings-group"></i>
					                        <span><op:translate key="ONLYOFFICE_EDIT_COLLAB" /></span>
					                    </a>
			                    	</li>
			                    	
			                    	
			                    	<c:choose>
			                    		<c:when test="${not empty driveEditUrl}">
			                    			<!-- Nuxeo drive online -->
					                    	<li>
							                    <a href="${driveEditUrl}" class="no-ajax-link">
							                    	<i class="halflings halflings-folder-open"></i>
							                        <span><op:translate key="DRIVE_EDIT" /></span>
							                    </a>
						                    </li>			                    		
			                    		
			                    		</c:when>
			                    		<c:when test="${driveEnabled}">
			                 				<!-- Nuxeo drive offline -->
					                 		<li class="disabled" data-toggle="tooltip" title="<op:translate key='MESSAGE_DRIVE_CLIENT_NOT_STARTED' />">
						                    	<a href="#" class="disabled">
						                    		<i class="halflings halflings-folder-open"></i>
							                        <span><op:translate key="DRIVE_EDIT" /></span>
							                    </a>
					                    	</li>
			                    		</c:when>
			                    	</c:choose>
			                    	<!-- end of nuxeo drive -->
			                    	
			                    </ul>
		                    </div>
            			
            				<!-- end of onlyoffice -->
            			</c:when>
            			<c:otherwise>
            			
            				<!-- nuxeo drive withour onlyoffice -->
            				<c:choose>
			            		<c:when test="${not empty driveEditUrl}">
				                    <a href="${driveEditUrl}" class="btn btn-primary btn-block no-ajax-link">
				                        <span><op:translate key="DRIVE_EDIT" /></span>
				                    </a>
				                </c:when>
				                <c:when test="${driveEnabled}">		                
				                    <a href="#" class="btn btn-primary btn-block disabled">
				                        <span><op:translate key="DRIVE_EDIT" /></span>
				                    </a>
				                </c:when>
            				</c:choose>
            			
            			</c:otherwise>
            			
            			
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
