<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>


<portlet:actionURL name="changeView" var="changeViewUrl">
    <portlet:param name="view" value="thumbnails" />
</portlet:actionURL>


<div class="file-browser-thumbnails reorganization">
    <div class="relative">
        <!-- Contextual toolbar -->
        <jsp:include page="contextual-toolbar.jsp" />
    
        <!-- Alert -->
        <div class="alert alert-info">
            <div class="media">
                <div class="media-left media-middle">
                    <i class="glyphicons glyphicons-info-sign"></i>
                </div>
                
                <div class="media-body">
                    <span><op:translate key="FILE_BROWSER_REORGANIZATION_MESSAGE" /></span>
                    <a href="${changeViewUrl}" class="alert-link no-ajax-link"><op:translate key="FILE_BROWSER_EXIT_REORGANIZATION" /></a>
                </div>
            </div>
        </div>
    </div>
    

    <!-- Empty indicator -->
    <c:if test="${empty documents}">
        <p class="text-muted text-center"><op:translate key="NO_ITEMS" /></p>
    </c:if>
 
    
    <div class="portlet-filler">
        <div class="selectable">
            <!-- Folders -->
            <ul class="list-unstyled row sortable folders" data-ordered="${ordered}" data-placeholderclasses="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                <c:forEach var="document" items="${documents}">
                    <c:if test="${document.type.folderish}">
                        <c:set var="folders" value="true" />
                    
                        <!-- Document properties -->
                        <c:choose>                    
                            <c:when test="${not empty document.type.icon}"><c:set var="icon" value="${document.type.icon}" /></c:when>
                            <c:otherwise><c:set var="icon" value="glyphicons glyphicons-folder-closed" /></c:otherwise>
                        </c:choose>
                    
                    
                        <li class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                            <div class="data" data-id="${document.id}" data-path="${document.path}" data-type="${document.type.name}" data-editable="${document.type.editable}" data-movable="${document.type.movable}" data-icon="${icon}">
                                <div class="droppable" data-accepted-types="${document.type.folderish ? fn:join(document.acceptedTypes, ',') : ''}">
                                    <div class="thumbnail">
                                        <div class="caption text-overflow">
                                            <!-- Icon -->
                                            <div class="document-icon draggable">
                                                <div class="folderish">
                                                    <i class="${icon}"></i>
                                                </div>
                                            </div>
                                            
                                            <!-- Sharing -->
                                            <c:if test="${document.sharing}">
                                                <div class="document-indicator">
                                                    <i class="glyphicons glyphicons-group"></i>
                                                </div>
                                            </c:if>
                                            
                                            <!-- Title -->
                                            <div class="document-title">
                                                <span class="draggable"><ttc:title document="${document}" displayContext="fileExplorer" /></span>
                                            </div>
                                        </div>
                                        
                                        <c:if test="${ordered}">
                                            <div class="sortable-handle text-muted text-center hidden">
                                                <i class="glyphicons glyphicons-sorting"></i>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                                
                                <!-- Draggable -->
                                <div class="draggable draggable-shadowbox"></div>
                            </div>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
            
            
            <!-- Horizontal row -->
            <c:if test="${folders}">
                <hr>
            </c:if>
            
            
            <!-- Files -->
            <ul class="list-unstyled row sortable files" data-ordered="${ordered}" data-placeholderclasses="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                <c:forEach var="document" items="${documents}">
                    <!-- Document properties -->
                    <c:choose>
                        <c:when test="${'Picture' eq document.type.name}"><ttc:documentLink document="${document}" picture="true" var="link" /></c:when>
                        <c:when test="${document.type.file}"><ttc:documentLink document="${document}" displayContext="download" var="link" /></c:when>
                        <c:otherwise><ttc:documentLink document="${document}" var="link" /></c:otherwise>
                    </c:choose>
                    <c:choose>                    
                        <c:when test="${'File' eq document.type.name}"><c:set var="icon" value="${document.icon}" /></c:when>
                        <c:when test="${not empty document.type.icon}"><c:set var="icon" value="${document.type.icon}" /></c:when>
                        <c:otherwise><c:set var="icon" value="glyphicons glyphicons-file" /></c:otherwise>
                    </c:choose>
                    <c:set var="vignetteUrl"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
                    <c:if test="${empty vignetteUrl and 'Picture' eq document.type.name}">
                        <c:set var="vignetteUrl"><ttc:documentLink document="${document}" picture="true" displayContext="Small" /></c:set>
                    </c:if>
                    <c:set var="lockOwner" value="${document.properties['ttc:lockOwner']}" />
                
                
                    <c:if test="${not document.type.folderish}">
                        <li class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                            <div class="data" data-id="${document.id}" data-path="${document.path}" data-type="${document.type.name}" data-editable="${document.type.editable}" data-movable="${document.type.movable}" data-icon="${icon}">
                                <div class="thumbnail">
                                    <div class="img-container">
                                        <c:choose>
                                            <c:when test="${not empty vignetteUrl}"><img src="${vignetteUrl}" alt="" class="img-fluid text-middle"></c:when>
                                            <c:otherwise><i class="${icon}"></i></c:otherwise>
                                        </c:choose>
                                    </div>
                                    
                                    <c:if test="${ordered}">
                                        <div class="sortable-handle text-muted text-center hidden">
                                            <i class="glyphicons glyphicons-sorting"></i>
                                        </div>
                                    </c:if>
                                </div>
                                
                                <div class="clearfix draggable">
                                    <!-- Icon -->
                                    <div class="document-icon">
                                        <div>
                                            <i class="${icon}"></i>
                                        </div>
                                    </div>
                                    
                                    <!-- Lock -->
                                    <c:if test="${not empty lockOwner}">
                                        <div class="document-indicator">
                                            <c:choose>
                                                <c:when test="${lockOwner eq pageContext.request.remoteUser}"><i class="glyphicons glyphicons-user-lock"></i></c:when>
                                                <c:otherwise><i class="glyphicons glyphicons-lock"></i></c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:if>
                                    
                                    <!-- Subscription -->
                                    <c:if test="${document.subscription}">
                                        <div class="document-indicator">
                                            <i class="glyphicons glyphicons-flag"></i>
                                        </div>
                                    </c:if>
                                    
                                    <!-- Sharing -->
                                    <c:if test="${document.sharing}">
                                        <div class="document-indicator">
                                            <i class="glyphicons glyphicons-group"></i>
                                        </div>
                                    </c:if>
                                    
                                    <!-- Title -->
                                    <div class="document-title">
                                        <span><ttc:title document="${document}" displayContext="fileExplorer" /></span>
                                    </div>
                                </div>
                                
                                <!-- Draggable -->
                                <div class="draggable draggable-shadowbox"></div>
                            </div>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
    </div>
</div>
