<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>


<c:set var="namespace"><portlet:namespace /></c:set>


<div class="file-browser-thumbnails">
    <!-- Empty indicator -->
    <c:if test="${empty documents}">
        <p class="text-muted text-center"><op:translate key="NO_ITEMS" /></p>
    </c:if>
    
    <!-- Sortable indicator -->
    <c:set var="sortable" value="${canUpload ? 'sortable' : ' '}" />


    <!-- Folders -->
    <ul class="list-unstyled row ${sortable} folders" data-ordered="${ordered}" data-placeholderclasses="col-xs-6 col-sm-4 col-md-3 col-lg-2">
        <c:forEach var="document" items="${documents}">
            <c:if test="${document.type.folderish}">
                <c:set var="folders" value="true" />
            
                <!-- Document properties -->
                <c:set var="url"><ttc:documentLink document="${document}" /></c:set>
                <c:choose>                    
                    <c:when test="${not empty document.type.icon}"><c:set var="icon" value="${document.type.icon}" /></c:when>
                    <c:otherwise><c:set var="icon" value="glyphicons glyphicons-folder-closed" /></c:otherwise>
                </c:choose>
            
            
                <li class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                    <div class="data" data-id="${document.id}">
                        <a href="${url}" class="thumbnail sortable-handle no-ajax-link" ondragstart="return false;">
                            <span class="caption text-overflow">
                                <span class="document-icon">
                                    <i class="${icon}"></i>
                                </span>
                                
                                <!-- Sharing -->
                                <c:if test="${document.sharing}">
                                    <span class="document-indicator">
                                        <i class="glyphicons glyphicons-group"></i>
                                    </span>
                                </c:if>
                                
                                <span>${document.title}</span>
                            </span>
                        </a>
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
    <ul class="list-unstyled row ${sortable} files" data-ordered="${ordered}" data-placeholderclasses="col-xs-6 col-sm-4 col-md-3 col-lg-2">
        <c:forEach var="document" items="${documents}">
            <c:if test="${not document.type.folderish}">
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
                
                
                <li class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                    <div class="data" data-id="${document.id}">
                        <div class="sortable-handle">
                            <a href="${link.url}" target="${link.external ? '_blank' : ''}" class="thumbnail no-ajax-link" ondragstart="return false;"
                                <c:if test="${'Picture' eq document.type.name}">data-fancybox="${namespace}-gallery" data-caption="${document.title}" data-type="image"</c:if>
                            >
                                <span class="img-container">
                                    <c:choose>
                                        <c:when test="${not empty vignetteUrl}"><img src="${vignetteUrl}" alt="" class="img-responsive text-middle"></c:when>
                                        <c:otherwise><i class="${icon}"></i></c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                            
                            <div class="clearfix">
                                <!-- Icon -->
                                <div class="document-icon">
                                    <i class="${icon}"></i>
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
                        </div>
                    </div>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>
