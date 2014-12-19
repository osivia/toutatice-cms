<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects />


<portlet:renderURL var="refreshURL" />

<portlet:actionURL name="drop" var="dropActionURL" />

<portlet:actionURL name="fileUpload" var="fileUploadActionURL">
    <portlet:param name="parentId" value="${document.id}" />
</portlet:actionURL>


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="description" value="${document.properties['dc:description']}" />


<div class="file-browser" data-dropurl="${dropActionURL}" data-refreshurl="${refreshURL}">
    <div class="drop-zone">
        <div class="table-responsive no-ajax-link">
            <table class="table">
                <!-- Description -->
                <c:if test="${not empty description}">
                    <caption>${description}</caption>
                </c:if>
            
                <!-- Table head -->
                <thead>
                    <tr>
                        <th></th>
                        <th><is:getProperty key="FILE_BROWSER_NAME" /></th>
                        <th><is:getProperty key="FILE_BROWSER_DATE" /></th>
                        <th><is:getProperty key="FILE_BROWSER_LAST_CONTRIBUTOR" /></th>
                    </tr>
                </thead>
                
                <!-- Table body -->
                <tbody class="no-ajax-link">
                    <c:forEach var="document" items="${documents}">
                        <!-- Document properties -->
                        <ttc:documentLink document="${document}" var="link" />
                        <ttc:documentLink document="${document}" displayContext="download" var="fileLink" />
                        <c:remove var="target" />
                        <c:if test="${link.external}">
                            <c:set var="target" value="_blank" />
                        </c:if>
                        <c:set var="size" value="${document.properties['common:size']}" />
                        <c:set var="lastContributor"><ttc:user name="${document.properties['dc:lastContributor']}" /></c:set>
                        
                        <!-- Date -->
                        <c:set var="date" value="${document.properties['dc:modified']}" />
                        <c:if test="${empty date}">
                            <c:set var="date" value="${document.properties['dc:created']}" />
                        </c:if>
                        <c:set var="date"><fmt:formatDate value="${date}" type="date" dateStyle="long" /></c:set>
                        
                        <!-- Folderish item ? -->
                        <c:remove var="folderish" />
                        <c:if test="${document.type.folderish}">
                            <c:set var="folderish" value="folderish bg-gray-dark" />
                        </c:if>
                        
                        <!-- Browsable item ? -->
                        <c:remove var="droppable" />
                        <c:if test="${document.type.browsable}">
                            <c:set var="droppable" value="droppable" />
                        </c:if>
                    
                        <!-- Accepted types -->
                        <c:remove var="acceptedTypes" />
                        <c:forEach var="acceptedType" items="${document.type.portalFormSubTypes}" varStatus="status">
                            <c:if test="${not status.first}">
                                <c:set var="acceptedTypes" value="${acceptedTypes}," />
                            </c:if>
                            
                            <c:set var="acceptedTypes" value="${acceptedTypes}${acceptedType}" />
                        </c:forEach>
                        
                        <!-- Glyph -->
                        <c:set var="glyph" value="${document.type.glyph}" />
                        <c:if test="${empty glyph}">
                            <c:choose>
                                <c:when test="${document.type.navigable}">
                                    <c:set var="glyph" value="folder_closed" />
                                </c:when>
                                
                                <c:otherwise>
                                    <c:set var="glyph" value="file" />
                                </c:otherwise>
                            </c:choose>
                        </c:if>
 
                    
                        <tr class="draggable ${droppable}" data-id="${document.id}" data-type="${document.type.name}" data-acceptedTypes="${acceptedTypes}">
                            <!-- Icon -->
                            <td class="text-gray-dark">
                                <c:choose>
                                    <c:when test="${'File' eq document.type.name}">
                                        <div class="file">
                                            <a href="${fileLink.url}" class="thumbnail">
                                                <i class="flaticon flaticon-${document.properties['mimeTypeIcon']}"></i>
                                            </a>
                                        </div>
                                    </c:when>
                                    
                                    <c:otherwise>
                                        <div class="${folderish}">
                                            <i class="glyphicons ${glyph}"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            
                            <!-- Display name -->
                            <td>
                                <div class="cell-container">
                                    <div class="content">
		                                <a href="${link.url}" target="${target}">${document.title}</a>
		                                
		                                <!-- Size -->
		                                <c:if test="${not empty size}">
		                                    <span>(<ttc:formatFileSize size="${size}" />)</span>
		                                </c:if>
		                                
		                                <!-- External -->
		                                <c:if test="${link.external}">
		                                    <i class="glyphicons halflings new_window"></i>
		                                </c:if>
	                                </div>
	                                
	                                <div class="hidden-xs">
		                                <div class="spacer">${document.title}</div>
		                                <span>&nbsp;</span>
	                                </div>
                                </div>
                            </td>
                            
                            <!-- Last modification -->
                            <td>
                                <div class="cell-container">
                                    <div class="content">${date}</div>
                                    
                                    <div class="hidden-xs">
	                                    <div class="spacer">${date}</div>
	                                    <span>&nbsp;</span>
                                    </div>
                                </div>
                            </td>
                            
                            <!-- Last contributor -->
                            <td>
                                <div class="cell-container">
                                    <div class="content">${lastContributor}</div>
                                    
                                    <div class="hidden-xs">
	                                    <div class="spacer">${lastContributor}</div>
	                                    <span>&nbsp;</span>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    
    
        <!-- File upload -->
        <form action="${fileUploadActionURL}" method="post" enctype="multipart/form-data" class="file-upload" role="form">
            <input type="file" name="files[]" class="hidden" multiple="multiple">
        
            <div class="panel panel-default hidden">
                <div class="panel-body">
                    <div class="form-group fileupload-buttonbar">
                        <button type="submit" class="btn btn-primary start">
                            <i class="glyphicons halflings upload"></i>
                            <span><is:getProperty key="FILE_BROWSER_START_UPLOAD" /></span>
                        </button>
                        
                        <button type="reset" class="btn btn-default cancel">
                            <i class="glyphicons halflings ban-circle"></i>
                            <span><is:getProperty key="CANCEL" /></span>
                        </button>
                    </div>
                        
                    <div class="form-group">
                        <div class="progress">
                            <div class="progress-bar" role="progressbar"></div>
                        </div>
                    </div>
                </div>
                
                <ul class="file-upload-list list-group files"></ul>
            </div>
        </form>
        
        <div class="file-upload-shadowbox jumbotron bg-info-hover">
            <div class="text-center text-middle">
                <p><is:getProperty key="FILE_BROWSER_DROP_ZONE_MESSAGE" /></p>
                <p class="h1"><i class="glyphicons inbox"></i></p>
            </div>
        </div>
    </div>
</div>
