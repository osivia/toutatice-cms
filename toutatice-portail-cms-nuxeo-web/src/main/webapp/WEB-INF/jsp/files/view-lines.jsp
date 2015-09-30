<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>


<!-- Sort by index render URL -->
<portlet:renderURL var="sortIndexURL">
    <portlet:param name="sort" value="index" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'index') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>
<!-- Sort by name render URL -->
<portlet:renderURL var="sortNameURL">
    <portlet:param name="sort" value="name" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'name') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>
<!-- Sort by date render URL -->
<portlet:renderURL var="sortDateURL">
    <portlet:param name="sort" value="date" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'date') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>
<!-- Sort by size render URL -->
<portlet:renderURL var="sortSizeURL">
    <portlet:param name="sort" value="size" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'size') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>


<!-- Description -->
<c:set var="description" value="${document.properties['dc:description']}" />


<div class="file-browser-lines">
    <div class="row">
        <!-- Description -->
        <div class="col-sm-5 col-md-6 col-lg-7 ">
            <c:if test="${not empty description}">
                <p class="text-muted">${description}</p>
            </c:if>
        </div>
        
        
        <!-- Toolbar -->
        <div class="col-sm-7 col-md-6 col-lg-5">
            <jsp:include page="toolbar.jsp" />
        </div>
    </div>


    <!-- Header -->
    <div class="table-header table-row">
        <div class="row">
            <div class="col-sm-5 col-md-6 col-lg-7">
                <div class="table-cell clearfix">
                    <c:if test="${ordered}">
                        <div class="document-index">
                            <a href="${sortIndexURL}">
                                <span>#</span>
                            </a>
                            
                            <c:if test="${criteria.sort eq 'index'}">
                                <small class="text-muted">
                                    <c:choose>
                                        <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </small>
                            </c:if>
                        </div>
                    </c:if>
                
                    <div>
                        <a href="${sortNameURL}">
                            <span><op:translate key="FILE_BROWSER_NAME" /></span>
                        </a>
                        
                        <c:if test="${criteria.sort eq 'name'}">
                            <small class="text-muted">
                                <c:choose>
                                    <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                    <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                </c:choose>
                            </small>
                        </c:if>
                    </div>
                </div>
            </div>
            
            <div class="col-sm-7 col-md-6 col-lg-5">
                <div class="row">
                    <div class="col-xs-9">
                        <div class="table-cell">
                            <a href="${sortDateURL}">
                                <span><op:translate key="FILE_BROWSER_LAST_CONTRIBUTION" /></span>
                            </a>
                            
                            <c:if test="${criteria.sort eq 'date'}">
                                <small class="text-muted">
                                    <c:choose>
                                        <c:when test="${not criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </small>
                            </c:if>
                        </div>
                    </div>
                    
                    <div class="col-xs-3">
                        <div class="table-cell">
                            <a href="${sortSizeURL}">
                                <span><op:translate key="FILE_BROWSER_SIZE" /></span>
                            </a>
                            
                            <c:if test="${criteria.sort eq 'size'}">
                                <small class="text-muted">
                                    <c:choose>
                                        <c:when test="${criteria.alternative}"><i class="halflings halflings-sort-by-attributes-alt"></i></c:when>
                                        <c:otherwise><i class="halflings halflings-sort-by-attributes"></i></c:otherwise>
                                    </c:choose>
                                </small>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    
    <div class="table-body">
        <ul class="list-unstyled selectable sortable" data-ordered="${ordered}" data-axis="y" data-alternative="${criteria.alternative}">
            <c:forEach var="document" items="${documents}">
                <!-- Document properties -->

                <!-- Download link -->
                <ttc:documentLink document="${document}" displayContext="download" var="downloadLink" />

                <!-- Glyph -->
                <c:choose>
                    <c:when test="${'File' eq document.type.name}">
                        <c:set var="glyph" value="flaticon flaticon-${document.properties['mimeTypeIcon']}" />
                    </c:when>
                    
                    <c:when test="${not empty document.type.glyph}">
                        <c:set var="glyph" value="${document.type.glyph}" />
                    </c:when>
                    
                    <c:when test="${document.type.navigable}">
                        <c:set var="glyph" value="glyphicons glyphicons-folder-closed" />
                    </c:when>
                    
                    <c:otherwise>
                        <c:set var="glyph" value="glyphicons glyphicons-file" />
                    </c:otherwise>
                </c:choose>
            
                <!-- Date -->
                <c:set var="date" value="${document.properties['dc:modified']}" />
                <c:if test="${empty date}">
                    <c:set var="date" value="${document.properties['dc:created']}" />
                </c:if>
                
                <!-- Size -->
                <c:set var="size" value="${document.properties['common:size']}" />
            
            
                <li>
                    <div class="data" data-id="${document.id}" data-path="${document.path}" data-type="${document.type.name}" data-editable="${document.type.supportsPortalForms}"
                        <c:if test="${('File' eq document.type.name) or ('Audio' eq document.type.name) or ('Video' eq document.type.name)}">data-downloadurl="${downloadLink.url}"</c:if>
                    >
                        <div
                            <c:if test="${document.type.folderish}">class="droppable" data-acceptedtypes="${fn:join(document.acceptedTypes, ',')}"</c:if>
                        >
                            <div class="table-row">
                                <div class="row">
                                    <div class="col-sm-5 col-md-6 col-lg-7">
                                        <!-- Title -->
                                        <div class="table-cell">
                                            <c:if test="${ordered}">
                                                <div class="document-index">
                                                    <span class="text-muted">${document.index}</span>
                                                </div>
                                            </c:if>
                                        
                                            <div>
                                                <div class="document-icon">
                                                    <div
                                                        <c:if test="${document.type.folderish}">class="folderish"</c:if>
                                                    >
                                                        <i class="${glyph}"></i>
                                                    </div>
                                                </div>
                                            
                                                <div class="text-overflow">
                                                    <ttc:title document="${document}" displayContext="fileExplorer" />
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <!-- Fancybox gallery link -->
                                        <c:if test="${'Picture' eq document.type.name}">
                                            <ttc:documentLink document="${document}" picture="true" var="pictureLink" />
                                            
                                            <a href="${pictureLink.url}" data-title="${document.title}" rel="gallery" class="fancybox thumbnail no-ajax-link"></a>
                                        </c:if>
                                        
                                        <!-- Sortable handle -->
                                        <c:if test="${ordered and (criteria.sort eq 'index')}">
                                            <div class="sortable-handle text-muted text-center hidden">
                                                <i class="glyphicons glyphicons-sorting"></i>
                                            </div>
                                        </c:if>
                                    </div>
                                    
                                    <div class="col-sm-7 col-md-6 col-lg-5">
                                        <div class="row">
                                            <div class="col-xs-9">
                                                <!-- Last contribution -->
                                                <div class="table-cell text-overflow">
                                                    <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
                                                    <small class="text-muted"><ttc:user name="${document.properties['dc:lastContributor']}" linkable="false" /></small>
                                                </div>
                                            </div>
                                            
                                            <div class="col-xs-3">
                                                <!-- Size -->
                                                <div class="table-cell text-overflow">
                                                    <c:choose>
                                                        <c:when test="${size gt 0}">
                                                            <span><ttc:fileSize size="${size}" /></span>
                                                        </c:when>
                                                        
                                                        <c:otherwise>
                                                            <span>&ndash;</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Draggable -->
                    <div class="draggable border-primary"></div>
                </li>
            </c:forEach>
        </ul>
    </div>
    
    
    <c:if test="${empty documents}">
        <p class="text-muted text-center"><op:translate key="NO_ITEMS" /></p>
    </c:if>
</div>
