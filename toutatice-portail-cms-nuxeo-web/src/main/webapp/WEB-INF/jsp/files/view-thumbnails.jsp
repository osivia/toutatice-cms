<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>


<div class="file-browser-thumbnails">
    <div class="selectable">
    
        <!-- Folders -->
        <ul class="list-unstyled row file-browser-folders">
            <c:forEach var="document" items="${documents}">
                <c:if test="${document.type.folderish}">
                    <!-- Document properties -->
                    
                    <!-- Links -->
                    <ttc:documentLink document="${document}" displayContext="document" var="detailLink" />

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
                
                
                    <li class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                        <div>
                            <div class="data" data-id="${document.id}" data-path="${document.path}" data-type="${document.type.name}" data-editable="${document.type.supportsPortalForms}">
                                <div
                                    <c:if test="${document.type.browsable}">class="droppable" data-acceptedtypes="${fn:join(document.acceptedTypes, ',')}"</c:if>
                                >
                                    <div class="thumbnail">
                                        <div class="caption text-overflow">
                                            <div class="document-icon">
                                                <i class="${glyph}"></i>
                                            </div>
                                            
                                            <div class="text-overflow">
                                                <ttc:title document="${document}" displayContext="fileExplorer" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="draggable border-primary"></div>
                        </div>
                    </li>
                </c:if>
            </c:forEach>
        </ul>
        
        
        <!-- Files -->
        <ul class="list-unstyled row file-browser-files">
            <c:forEach var="document" items="${documents}">
                <c:if test="${not document.type.folderish}">
                    <!-- Document properties -->
                    
                    <!-- Links -->
                    <ttc:documentLink document="${document}" displayContext="document" var="detailLink" />
                    <ttc:documentLink document="${document}" displayContext="download" var="downloadLink" />
                    
                    <!-- Vignette -->
                    <c:set var="vignetteURL"><ttc:getImageURL document="${document}" property="ttc:vignette" /></c:set>                    
                    
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
                    
                    
                    <li class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                        <div>
                            <div class="data" data-id="${document.id}" data-path="${document.path}" data-type="${document.type.name}" data-editable="${document.type.supportsPortalForms}"
                                <c:if test="${('File' eq document.type.name) or ('Audio' eq document.type.name) or ('Video' eq document.type.name)}">data-downloadurl="${downloadLink.url}"</c:if>
                                <c:if test="${'ContextualLink' eq document.type.name}">data-detailurl="${detailLink.url}"</c:if>
                            >
                                <div class="thumbnail">
                                    <div class="img-container">
                                        <c:choose>
                                            <c:when test="${not empty vignetteURL}">
                                                <img src="${vignetteURL}" alt="" class="img-responsive text-middle">
                                            </c:when>
                                            
                                            <c:when test="${'Picture' eq document.type.name}">
                                                <ttc:documentLink document="${document}" picture="true" displayContext="Medium" var="mediumPictureLink" />
                                                
                                                <img src="${mediumPictureLink.url}" alt="" class="img-responsive text-middle">
                                            </c:when>

                                            <c:otherwise>
                                                <div class="text-center">
                                                    <i class="${glyph}"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        
                                        <!-- Fancybox gallery link -->
                                        <c:if test="${'Picture' eq document.type.name}">
                                            <ttc:documentLink document="${document}" picture="true" var="pictureLink" />
                                            
                                            <a href="${pictureLink.url}" rel="gallery" class="fancybox-gallery no-ajax-link"></a>
                                        </c:if>
                                    </div>
                                
                                    <div class="caption text-overflow">
                                        <div class="document-icon">
                                            <i class="${glyph}"></i>
                                        </div>
                                        
                                        <div class="text-overflow">
                                            <ttc:title document="${document}" displayContext="fileExplorer" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="draggable border-primary"></div>
                        </div>
                    </li>
                </c:if>
            </c:forEach>
        </ul>
        
    </div>
</div>
