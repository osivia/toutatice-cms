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

<!-- Download zip of selected documents -->
<portlet:resourceURL id="zipDownload" var="zipDownloadUrl" >
	<portlet:param name="paths" value="_PATHS_"/>
</portlet:resourceURL>


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="messageSingleSelection"><op:translate key="FILE_BROWSER_ONE_ELEMENT_SELECTED" /></c:set>
<c:set var="messageMultipleSelection"><op:translate key="FILE_BROWSER_N_ELEMENTS_SELECTED" /></c:set>

<c:set var="messageNotFile"><op:translate key="FILE_BROWSER_NOT_FILE" /></c:set>
<c:set var="messageTooLarge"><op:translate key="FILE_BROWSER_TOO_LARGE" /></c:set>

<div class="contextual-toolbar hidden-print" data-infos-url="${infosUrl}" data-drive-enabled="${driveEnabled}" data-ajax-shadowbox="#shadowbox-${namespace}-toolbar">
    <nav class="navbar navbar-default" role="toolbar">
        <h3 class="sr-only"><op:translate key="FILE_BROWSER_TOOLBAR_TITLE"/></h3>
    
        <div class="relative">
            <div id="shadowbox-${namespace}-toolbar" class="ajax-shadowbox">
                <div class="progress">
                    <div class="progress-bar progress-bar-striped active" role="progressbar">
                        <strong><op:translate key="AJAX_REFRESH" /></strong>
                    </div>
                </div>
            </div>
            
            <div class="container-fluid">
                <!-- Information text -->
                <p class="navbar-text hidden-xs hidden-sm">
                    <span class="message-selection label label-primary" data-message-single-selection="${messageSingleSelection}" data-message-multiple-selection="${messageMultipleSelection}">
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
                    
                    <!-- Edit when drive edition is enabled  -->
                    <c:set var="title"><op:translate key="EDIT" /></c:set>
                    <a href="#" data-url="${editUrl}" onclick="setCallbackFromEcmParams('${callbackUrl}', '${ecmBaseUrl}')" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn fancyframe_refresh no-ajax-link edit-drive-enabled">
                        <i class="halflings halflings-pencil"></i>
                        <span class="sr-only">${title}</span>
                    </a>
                    
                    <!-- Download -->
                    <c:set var="title"><op:translate key="DOWNLOAD" /></c:set>
                    <a href="#" target="_blank" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn no-ajax-link download">
                        <i class="halflings halflings-download-alt"></i>
                        <span class="sr-only">${title}</span>
                    </a>
                    
                    <!-- Gallery -->
                    <c:set var="title"><op:translate key="VIEW_PICTURE_GALLERY" /></c:set>
                    <button type="button" onclick="gallery(this)" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn gallery">
                        <i class="halflings halflings-fullscreen"></i>
                        <span class="sr-only">${title}</span>
                    </button>
                    
                    <!-- Copy -->
                    <c:set var="title"><op:translate key="COPY" /></c:set>
                    <a href="#" data-url="${copyUrl}" title="${title}" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn copy">
                        <i class="halflings halflings-duplicate"></i>
                        <span class="sr-only">${title}</span>
                    </a>
                </div>

                <!-- Bulk Download -->
                <c:set var="title"><op:translate key="DOWNLOAD" /></c:set>
                <div title="${title}" class="btn-group btn-group-sm bulk-download" data-toggle="tooltip" data-placement="top" data-message-not-file="${messageNotFile}" data-message-too-large="${messageTooLarge}" data-message-ok="${title}" role="group">
                    <a href="#" target="_blank" data-url="${zipDownloadUrl}" class="btn btn-default navbar-btn no-ajax-link">
                        <i class="halflings halflings-download-alt"></i>
                        <span class="sr-only">${title}</span>
                    </a>
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
                    <a href="javascript:;" title="${title}" data-fancybox data-src="#${namespace}-delete" data-toggle="tooltip" data-placement="top" class="btn btn-default navbar-btn delete">
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
        </div>
    </nav>
</div>


<div class="hidden">
    <!-- Delete confirmation -->
    <div id="${namespace}-delete">
        <form action="${deleteUrl}" method="post" role="form">
            <input type="hidden" name="identifiers" value="">
            <input type="hidden" name="paths" value="">
            
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
