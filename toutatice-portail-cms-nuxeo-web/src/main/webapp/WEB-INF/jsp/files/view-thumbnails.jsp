<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>


<!-- Description -->
<c:set var="description" value="${document.properties['dc:description']}" />


<div class="file-browser-thumbnails">

    <!-- Description -->
    <c:if test="${not empty description}">
        <p class="text-muted">${description}</p>
    </c:if>


    <!-- Empty indicator -->
    <c:if test="${empty documents}">
        <p class="text-muted text-center"><is:getProperty key="NO_ITEMS" /></p>
    </c:if>


    <!-- Folders -->
    <ul class="list-unstyled row sortable" data-ordered="${ordered}" data-placeholderclasses="col-xs-6 col-sm-4 col-md-3 col-lg-2">
        <c:forEach var="document" items="${documents}">
            <c:if test="${document.type.folderish}">
                <!-- Document properties -->

                <!-- Link -->
                <ttc:documentLink document="${document}" var="link" />

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
                    <div class="data" data-id="${document.id}">
                        <a href="${link.url}" class="thumbnail sortable-handle no-ajax-link" ondragstart="return false;">
                            <span class="caption text-overflow">
                                <span class="document-icon">
                                    <i class="${glyph}"></i>
                                </span>
                                
                                <span>${document.title}</span>
                            </span>
                        </a>
                    </div>
                </li>
            </c:if>
        </c:forEach>
    </ul>
    
    
    <!-- Files -->
    <ul class="list-unstyled row sortable" data-ordered="${ordered}" data-placeholderclasses="col-xs-6 col-sm-4 col-md-3 col-lg-2">
        <c:forEach var="document" items="${documents}">
            <c:if test="${not document.type.folderish}">
                <!-- Document properties -->
                    
                <!-- Link -->
                <c:remove var="fancyboxClass" />
                <c:choose>
                    <c:when test="${'Picture' eq document.type.name}">
                        <ttc:documentLink document="${document}" picture="true" var="link" />
                        <c:set var="fancyboxClass" value="fancybox" />
                    </c:when>
                    
                    <c:when test="${('File' eq document.type.name) or ('Audio' eq document.type.name) or ('Video' eq document.type.name)}">
                        <ttc:documentLink document="${document}" displayContext="download" var="link" />
                    </c:when>
                    
                    <c:otherwise>
                        <ttc:documentLink document="${document}" var="link" />
                    </c:otherwise>
                </c:choose>

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
                    <div class="data" data-id="${document.id}">
                        <div class="sortable-handle">
                            <a href="${link.url}" class="thumbnail no-ajax-link ${fancyboxClass}" ondragstart="return false;"
                                <c:if test="${link.external}">target="_blank"</c:if>
                                <c:if test="${'Picture' eq document.type.name}">data-title="${document.title}" rel="gallery"</c:if>
                            >
                                <span class="img-container">
                                    <c:choose>
                                        <c:when test="${not empty vignetteURL}">
                                            <img src="${vignetteURL}" alt="" class="img-responsive text-middle">
                                        </c:when>
                                        
                                        <c:when test="${'Picture' eq document.type.name}">
                                            <ttc:documentLink document="${document}" picture="true" displayContext="Medium" var="mediumPictureLink" />
                                            
                                            <img src="${mediumPictureLink.url}" alt="" class="img-responsive text-middle">
                                        </c:when>
    
                                        <c:otherwise>
                                            <i class="${glyph}"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                            
                            <div class="text-overflow">
                                <div class="document-icon">
                                    <i class="${glyph}"></i>
                                </div>
                                
                                <ttc:title document="${document}" displayContext="fileExplorer" />
                            </div>
                        </div>
                    </div>
                </li>
            </c:if>
        </c:forEach>
    </ul>

</div>
