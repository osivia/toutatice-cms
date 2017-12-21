<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>


<!-- Sort by index render URL -->
<portlet:renderURL var="sortIndexUrl">
    <portlet:param name="sort" value="index" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'index') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>
<!-- Sort by name render URL -->
<portlet:renderURL var="sortNameUrl">
    <portlet:param name="sort" value="name" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'name') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>
<!-- Sort by date render URL -->
<portlet:renderURL var="sortDateUrl">
    <portlet:param name="sort" value="date" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'date') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>
<!-- Sort by size render URL -->
<portlet:renderURL var="sortSizeUrl">
    <portlet:param name="sort" value="size" />
    <portlet:param name="view" value="lines" />
    
    <c:if test="${(criteria.sort eq 'size') and not criteria.alternative}">
        <portlet:param name="alt" value="true" />
    </c:if>
</portlet:renderURL>


<div class="file-browser-lines">
    <div class="table">
        <!-- Header -->
        <div class="table-header table-row">
            <!-- Header contextual toolbar -->
            <jsp:include page="contextual-toolbar.jsp" />
        
            <div class="row">
                <div class="col-sm-5 col-md-6 col-lg-7">
                    <c:if test="${ordered}">
                        <div class="document-index text-overflow">
                            <a href="${sortIndexUrl}">
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
                
                    <div class="text-overflow">
                        <a href="${sortNameUrl}">
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
                
                <div class="col-sm-7 col-md-6 col-lg-5">
                    <div class="row">                    
                        <div class="col-xs-9">
                            <div class="text-overflow">
                                <a href="${sortDateUrl}">
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
                            <div class="text-overflow">
                                <a href="${sortSizeUrl}">
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
        
        
        <div class="table-body portlet-filler">
            <c:choose>
                <c:when test="${not empty documents}">
                    <ul class="list-unstyled selectable sortable" data-ordered="${ordered}" data-axis="y" data-alternative="${criteria.alternative}">
                        <c:forEach var="document" items="${documents}">
                            <!-- Document properties -->

                            <!-- Glyph -->
                            <c:choose>
                                <c:when test="${'File' eq document.type.name}">
                                    <c:set var="glyph" value="${document.icon}" />
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
                            
                            <!-- Download URL -->
                            <c:choose>
                                <c:when test="${'Picture' eq document.type.name}">
                                    <c:set var="downloadUrl"><ttc:documentLink document="${document}" picture="true" /></c:set>
                                </c:when>
                            
                                <c:when test="${document.type.file}">
                                    <c:set var="downloadUrl"><ttc:documentLink document="${document}" displayContext="download" /></c:set>
                                </c:when>
                                
                                <c:otherwise>
                                    <c:remove var="downloadUrl" />
                                </c:otherwise>
                            </c:choose>
                        
                        
                            <li>
                                <div class="data" data-id="${document.id}" data-path="${document.path}" data-draft-path="${document.properties['draftPath']}" data-type="${document.type.name}" data-file="${document.type.file}" data-size="${size}" data-editable="${document.type.supportsPortalForms}" data-icon="${glyph}" data-download-url="${downloadUrl}" data-live-edit-url="${document.properties['liveEditUrl']}">
                                    <div class="${document.type.folderish ? 'droppable' : ''}" data-accepted-types="${document.type.folderish ? fn:join(document.acceptedTypes, ',') : ''}">
                                        <div class="table-row">
                                            <div class="row">
                                                <div class="col-sm-5 col-md-6 col-lg-7">
                                                    <!-- Order -->
                                                    <c:if test="${ordered}">
                                                        <div class="document-index">
                                                            <span class="text-muted">${document.index}</span>
                                                        </div>
                                                    </c:if>
                                                
                                                    <!-- Icon -->
                                                    <div class="document-icon draggable">
                                                        <div
                                                            <c:if test="${document.type.folderish}">class="folderish"</c:if>
                                                        >
                                                            <i class="${glyph}"></i>
                                                        </div>
                                                    </div>
                                                    
                                                    <!-- Lock -->
                                                    <c:set var="lockOwner" value="${document.properties['ottc:lockOwner']}" /> 
                                                    <c:if test="${not empty lockOwner}">
                                                        <div class="document-lock">
                                                            <c:choose>
                                                                <c:when test="${lockOwner eq pageContext.request.remoteUser}">
                                                                    <i class="glyphicons glyphicons-user-lock"></i>
                                                                </c:when>
                                                                
                                                                <c:otherwise>
                                                                    <i class="glyphicons glyphicons-lock"></i>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </c:if>
                                                    
                                                    <!-- Subscription -->
                                                    <c:if test="${document.subscription}">
                                                        <div class="document-subscription">
                                                            <i class="glyphicons glyphicons-flag"></i>
                                                        </div>
                                                    </c:if>
                                                
                                                    <!-- Title -->
                                                    <div class="document-title">
                                                        <span class="draggable">
                                                            <ttc:title document="${document}" displayContext="fileExplorer" />
                                                        </span>
                                                    </div>
                                                    
                                                    <!-- Fancybox gallery link -->
                                                    <c:if test="${'Picture' eq document.type.name}">
                                                        <ttc:documentLink document="${document}" picture="true" var="pictureLink" />
                                                        
                                                        <a href="${pictureLink.url}" data-title="${document.title}" rel="gallery" class="fancybox thumbnail no-ajax-link"></a>
                                                    </c:if>
                                                    
                                                    <!-- Sortable handle -->
                                                    <c:if test="${ordered and (criteria.sort eq 'index') and canUpload}">
                                                        <div class="sortable-handle text-muted text-center hidden">
                                                            <i class="glyphicons glyphicons-sorting"></i>
                                                        </div>
                                                    </c:if>
                                                </div>
                                                
                                                <div class="col-sm-7 col-md-6 col-lg-5">
                                                    <div class="row">
                                                        <div class="col-xs-9">
                                                            <!-- Last contribution -->
                                                            <div class="text-overflow">
                                                                <span><op:formatRelativeDate value="${date}" capitalize="true" /></span>
                                                                <small class="text-muted"><ttc:user name="${document.properties['dc:lastContributor']}" linkable="false" /></small>
                                                            </div>
                                                        </div>
                                                        
                                                        
                                                        <div class="col-xs-3">
                                                            <!-- Size -->
                                                            <div class="text-overflow">
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
                                <c:if test="${document.type.supportsPortalForms}">
                                    <div class="draggable draggable-shadowbox border-primary"></div>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                
                <c:otherwise>
                    <div class="table-row">
                        <div class="text-center text-muted"><op:translate key="NO_ITEMS" /></div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
