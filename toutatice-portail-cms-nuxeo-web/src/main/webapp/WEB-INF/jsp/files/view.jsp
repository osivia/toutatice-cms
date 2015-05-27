<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects />


<portlet:renderURL var="refreshURL" />

<portlet:renderURL var="sortByIndexURL">
    <portlet:param name="sort" value="index" />
    
    <c:if test="${(criteria.sort eq 'index') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>

<portlet:renderURL var="sortByNameURL">
    <portlet:param name="sort" value="name" />
    
    <c:if test="${(criteria.sort eq 'name') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>

<portlet:renderURL var="sortByDateURL">
    <portlet:param name="sort" value="date" />
    
    <c:if test="${(criteria.sort eq 'date') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>

<portlet:renderURL var="sortByContributorURL">
    <portlet:param name="sort" value="contributor" />
    
    <c:if test="${(criteria.sort eq 'contributor') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>

<portlet:renderURL var="sortBySizeURL">
    <portlet:param name="sort" value="size" />
    
    <c:if test="${(criteria.sort eq 'size') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>

<portlet:actionURL name="drop" var="dropActionURL" />

<portlet:actionURL name="fileUpload" var="fileUploadActionURL">
    <portlet:param name="parentId" value="${document.id}" />
</portlet:actionURL>


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="description" value="${document.properties['dc:description']}" />


<div class="file-browser" data-dropurl="${dropActionURL}" data-refreshurl="${refreshURL}">
    <div
        <c:if test="${editable}">class="drop-zone"</c:if>
    >
        <div class="table-responsive">
            <table class="table">
                <!-- Description -->
                <c:if test="${not empty description}">
                    <caption>${description}</caption>
                </c:if>
            
                <!-- Table head -->
                <thead>
                    <tr>
                        <c:if test="${ordered}">
                            <th class="table-col-small">
                                <a href="${sortByIndexURL}">
                                    <span>#</span>
                                    <c:if test="${criteria.sort eq 'index'}">
                                        <c:choose>
                                            <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                            <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </a>
                            </th>
                        </c:if>
                    
                        <th class="table-col-small"></th>
                        
                        <th>
                            <a href="${sortByNameURL}">
                                <span><is:getProperty key="FILE_BROWSER_NAME" /></span>
                                <c:if test="${criteria.sort eq 'name'}">
                                    <c:choose>
                                        <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </c:if>
                            </a>
                        </th>
                        
                        <th class="table-col-medium">
                            <a href="${sortByDateURL}">
                                <span><is:getProperty key="FILE_BROWSER_DATE" /></span>
                                <c:if test="${criteria.sort eq 'date'}">
                                    <c:choose>
                                        <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </c:if>
                            </a>
                        </th>
                        
                        <th class="table-col-medium">
                            <a href="${sortByContributorURL}">
                                <span><is:getProperty key="FILE_BROWSER_LAST_CONTRIBUTOR" /></span>
                                <c:if test="${criteria.sort eq 'contributor'}">
                                    <c:choose>
                                        <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </c:if>
                            </a>
                        </th>
                        
                        <th class="table-col-small">
                            <a href="${sortBySizeURL}">
                                <span><is:getProperty key="FILE_BROWSER_SIZE" /></span>
                                <c:if test="${criteria.sort eq 'size'}">
                                    <c:choose>
                                        <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </c:if>
                            </a>
                        </th>
                    </tr>
                </thead>
                
                <!-- Table body -->
                <tbody class="no-ajax-link">
                    <c:forEach var="document" items="${documents}">
                        <!-- Document properties -->
                        <ttc:documentLink document="${document}" displayContext="fileExplorer" var="link" />
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
                                    <c:set var="glyph" value="glyphicons glyphicons-folder-closed" />
                                </c:when>
                                
                                <c:otherwise>
                                    <c:set var="glyph" value="glyphicons glyphicons-file" />
                                </c:otherwise>
                            </c:choose>
                        </c:if>
 
                    
                        <tr class="draggable ${droppable}" data-id="${document.id}" data-type="${document.type.name}" data-acceptedTypes="${acceptedTypes}">
                            <!-- Index -->
                            <c:if test="${ordered}">
                                <td>${document.index}</td>
                            </c:if>
                            
                            <!-- Icon -->
                            <td class="icon text-gray-dark">
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
                                            <i class="${glyph}"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            
                            <!-- Display name -->
                            <td>
                                <div class="cell-container">
                                    <div class="content">
		                                <a href="${link.url}" target="${target}">${document.title}</a>
		                                
		                                <!-- External -->
		                                <c:if test="${link.external}">
                                            <small>
		                                        <i class="halflings halflings-new-window"></i>
                                            </small>
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
                            
                            <!-- Size -->
                            <td>
                                <c:choose>
                                    <c:when test="${size gt 0}"><ttc:formatFileSize size="${size}" /></c:when>
                                    <c:otherwise>&ndash;</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    
    
        <!-- File upload -->
        <c:if test="${editable}">
            <form action="${fileUploadActionURL}" method="post" enctype="multipart/form-data" class="file-upload" role="form">
                <input type="file" name="files[]" class="hidden" multiple="multiple">
            
                <div class="panel panel-default hidden">
                    <div class="panel-body">
                        <div class="form-group fileupload-buttonbar">
                            <button type="submit" class="btn btn-primary start">
                                <i class="halflings halflings-upload"></i>
                                <span><is:getProperty key="FILE_BROWSER_START_UPLOAD" /></span>
                            </button>
                            
                            <button type="reset" class="btn btn-default cancel">
                                <i class="halflings halflings-ban-circle"></i>
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
                <div class="text-center">
                    <p><is:getProperty key="FILE_BROWSER_DROP_ZONE_MESSAGE" /></p>
                    <p class="h1"><i class="glyphicons glyphicons-inbox"></i></p>
                </div>
            </div>
        </c:if>
    </div>
</div>
