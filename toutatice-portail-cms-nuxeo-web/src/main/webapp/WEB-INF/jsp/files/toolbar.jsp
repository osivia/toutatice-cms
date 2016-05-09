<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>


<!-- Delete document URLS -->
<portlet:actionURL name="delete" var="deleteURL" />

<!-- Load document publication infos URL -->
<portlet:resourceURL id="infos" var="infosUrl" />


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="messageSingleSelection"><op:translate key="FILE_BROWSER_ONE_ELEMENT_SELECTED" /></c:set>
<c:set var="messageMultipleSelection"><op:translate key="FILE_BROWSER_N_ELEMENTS_SELECTED" /></c:set>


<div class="file-browser-affix-container">
    <div class="file-browser-affix">
        <div class="btn-toolbar" role="toolbar" data-infosurl="${infosUrl}">
            <!-- Selection indicator -->
            <div class="btn-group btn-group-sm hidden-xs">
                <span class="btn btn-primary disabled">
                    <span class="message-selection" data-message-single-selection="${messageSingleSelection}" data-message-multiple-selection="${messageMultipleSelection}">
                        <span class="badge"></span>
                        <span class="text hidden-xs"></span>
                    </span>
                </span>
            </div>
            
            <div class="btn-group btn-group-sm" role="group">
                <!-- Deselect -->
                <c:set var="title"><op:translate key="DESELECT" /></c:set>
                <button type="button" onclick="deselect(this)" class="btn btn-default" title="${title}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-unchecked"></i>
                    <span class="sr-only">${title}</span>
                </button>
            </div>
        
            <div class="btn-group btn-group-sm single-selection" role="group">
                <!-- Gallery -->
                <c:set var="title"><op:translate key="VIEW_PICTURE_GALLERY" /></c:set>
                <button type="button" onclick="gallery(this)" class="btn btn-default gallery" title="${title}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-blackboard"></i>
                    <span class="sr-only">${title}</span>
                </button>
            
                <!-- Download -->
                <c:set var="title"><op:translate key="DOWNLOAD" /></c:set>
                <a href="" class="btn btn-default no-ajax-link download" title="${title}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-download-alt"></i>
                    <span class="sr-only">${title}</span>
                </a>
            
                <!-- Edit -->
                <c:set var="title"><op:translate key="EDIT" /></c:set>
                <a href="" data-url="${editURL}" onclick="setCallbackFromEcmParams('${callbackURL}', '${ecmBaseURL}')" class="btn btn-default fancyframe_refresh no-ajax-link edit" title="${title}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-pencil"></i>
                    <span class="sr-only">${title}</span>
                </a>
            </div>
            
            <div class="btn-group btn-group-sm multiple-selection" role="group">
                <!-- Move -->
                <c:set var="title"><op:translate key="MOVE" /></c:set>
                <a href="" data-url="${moveURL}" class="btn btn-default fancyframe_refresh no-ajax-link move" title="${title}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-move"></i>
                    <span class="sr-only">${title}</span>
                </a>
            
                <!-- Delete -->
                <c:set var="title"><op:translate key="DELETE" /></c:set>
                <a href="#${namespace}-delete" class="btn btn-default fancybox_inline no-ajax-link delete" title="${title}" data-toggle="tooltip" data-placement="bottom">
                    <i class="halflings halflings-trash"></i>
                    <span class="sr-only">${title}</span>
                </a>
            </div>
            
            <!-- Ajax waiter -->
            <div class="btn-group btn-group-sm ajax-waiter">
                <span class="btn btn-default disabled">
                    <i class="halflings halflings-refresh"></i>
                    <span class="sr-only"><op:translate key="REFRESH" /></span>
                </span>
            </div>
        </div>
    </div>
</div>


<div class="hidden">
    <!-- Delete confirmation -->
    <div id="${namespace}-delete">
        <form action="${deleteURL}" method="post" role="form">
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
