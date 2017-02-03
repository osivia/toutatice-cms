<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>


<!-- Delete document URLS -->
<portlet:actionURL name="delete" var="deleteUrl" />

<!-- Copy document URL -->
<portlet:actionURL name="copy" var="copyUrl">
    <portlet:param name="sourcePath" value="_PATH_" />
</portlet:actionURL>

<!-- Load document publication informations URL -->
<portlet:resourceURL id="infos" var="infosUrl" />


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="messageSingleSelection"><op:translate key="FILE_BROWSER_ONE_ELEMENT_SELECTED" /></c:set>
<c:set var="messageMultipleSelection"><op:translate key="FILE_BROWSER_N_ELEMENTS_SELECTED" /></c:set>


<div class="contextual-toolbar" data-infos-url="${infosUrl}" data-drive-enabled="${driveEnabled}">
    <nav class="navbar navbar-default" role="toolbar">
        <h3 class="sr-only"><op:translate key="FILE_BROWSER_TOOLBAR_TITLE"/></h3>
    
        <div class="container-fluid">
            <!-- Information text -->
            <p class="navbar-text hidden-xs hidden-sm">
                <span class="message-selection small" data-message-single-selection="${messageSingleSelection}" data-message-multiple-selection="${messageMultipleSelection}">
                    <span></span>
                </span>
            </p>
            
            <div class="btn-group btn-group-sm single-selection" role="group">
                <!-- Edit -->
                <a href="#" data-url="${editUrl}" onclick="setCallbackFromEcmParams('${callbackUrl}', '${ecmBaseUrl}')" class="btn btn-default navbar-btn fancyframe_refresh no-ajax-link edit">
                    <i class="halflings halflings-pencil"></i>
                    <span><op:translate key="EDIT" /></span>
                </a>
                
                <!-- Drive edit -->
                <a href="#" class="btn btn-default navbar-btn drive-edit">
                    <i class=""></i>
                    <span><op:translate key="DRIVE_EDIT" /></span>
                </a>
                
                <!-- Copy -->
                <c:set var="title"><op:translate key="COPY" /></c:set>
                <a href="#" data-url="${copyUrl}" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn copy">
                    <i class="halflings halflings-duplicate"></i>
                    <span class="sr-only">${title}</span>
                </a>
                
                <!-- Gallery -->
                <c:set var="title"><op:translate key="VIEW_PICTURE_GALLERY" /></c:set>
                <button type="button" onclick="gallery(this)" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn gallery">
                    <i class="halflings halflings-fullscreen"></i>
                    <span class="sr-only">${title}</span>
                </button>
            </div>
            
            <div class="btn-group btn-group-sm multiple-selection" role="group">
                <!-- Move -->
                <c:set var="title"><op:translate key="MOVE" /></c:set>
                <a href="#" data-url="${moveUrl}" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn fancyframe_refresh no-ajax-link move">
                    <i class="halflings halflings-move"></i>
                    <span class="sr-only">${title}</span>
                </a>
            
                <!-- Delete -->
                <c:set var="title"><op:translate key="DELETE" /></c:set>
                <a href="#${namespace}-delete" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn fancybox_inline no-ajax-link delete">
                    <i class="halflings halflings-trash"></i>
                    <span class="sr-only">${title}</span>
                </a>
            </div>
            
            <!-- Unselect -->
            <button type="button" onclick="deselect(this)" class="btn btn-link btn-sm navbar-btn hidden-xs">
                <span><op:translate key="DESELECT" /></span>
            </button>
            
            <!-- Ajax waiter -->
            <div class="btn-group pull-right ajax-waiter">
                <span class="btn btn-link navbar-btn disabled">
                    <i class="halflings halflings-refresh"></i>
                    <span class="sr-only"><op:translate key="REFRESH" /></span>
                </span>
            </div>
        </div>
    </nav>
</div>


<div class="hidden">
    <!-- Delete confirmation -->
    <div id="${namespace}-delete">
        <form action="${deleteUrl}" method="post" role="form">
            <input type="hidden" name="identifiers" value="">
            
            <div class="form-group text-center"><op:translate key="CMS_DELETE_CONFIRM_MESSAGE" /></div>
            
            <div class="form-group text-center">
                <button type="submit" class="btn btn-warning">
                    <i class="halflings halflings-alert"></i>
                    <span><op:translate key="YES" /></span>
                </button>
                
                <button type="button" onclick="closeFancybox()" class="btn btn-default">
                    <span><op:translate key="NO" /></span>
                </button>
            </div>
        </form>
    </div>
</div>
