<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects />


<!-- Refresh render URL -->
<portlet:renderURL var="refreshURL">
    <portlet:param name="view" value="${view}" />
</portlet:renderURL>
<!-- Drop action URL -->
<portlet:actionURL name="drop" var="dropURL">
    <portlet:param name="view" value="${view}" />
</portlet:actionURL>
<!-- Sort action URL -->
<portlet:actionURL name="sort" var="sortURL">
    <portlet:param name="view" value="${view}" />
</portlet:actionURL>
<!-- File upload action URL -->
<portlet:actionURL name="fileUpload" var="fileUploadURL">
    <portlet:param name="parentId" value="${document.id}" />
    <portlet:param name="view" value="${view}" />
</portlet:actionURL>


<div class="file-browser" data-refreshurl="${refreshURL}" data-dropurl="${dropURL}" data-sorturl="${sortURL}">
    <div
        <c:if test="${editable}">class="drop-zone"</c:if>
    >
        <jsp:include page="view-${view}.jsp" />
        
        
        <!-- File upload -->
        <c:if test="${editable}">
            <form action="${fileUploadURL}" method="post" enctype="multipart/form-data" class="file-upload" role="form">
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
