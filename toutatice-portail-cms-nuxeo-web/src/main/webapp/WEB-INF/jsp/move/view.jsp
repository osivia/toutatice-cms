<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:renderURL var="changeSpaceURL">
    <portlet:param name="osivia.move.mode" value="space" />
</portlet:renderURL>

<portlet:resourceURL id="fancytreeLazyLoading" var="lazyLoadingURL">
    <portlet:param name="live" value="true" />

    <c:if test="${not empty ignoredPaths}">
        <portlet:param name="ignoredPaths" value="${ignoredPaths}" />
    </c:if>

    <c:if test="${not empty cmsBasePath}">
        <portlet:param name="cmsBasePath" value="${cmsBasePath}" />
    </c:if>

    <c:if test="${not empty acceptedTypes}">
        <portlet:param name="acceptedTypes" value="${acceptedTypes}" />
    </c:if>
</portlet:resourceURL>

<portlet:actionURL name="move" var="moveURL"></portlet:actionURL>


<c:set var="namespace"><portlet:namespace /></c:set>


<form action="${moveURL}" method="post" class="form-horizontal" role="form">
    <p class="lead">
        <i class="glyphicons glyphicons-move"></i>
        <span><op:translate key="DOCUMENT_MOVE_TITLE" /></span>
    </p>

    <!-- Target path -->
    <div class="form-group">
        <label for="${namespace}-target-path" class="col-sm-3 control-label"><op:translate key="DOCUMENT_MOVE_TARGET_PATH" /></label>
        <div class="col-sm-9">
            <div class="selector">
                <p>
                    <input id="${namespace}-target-path" type="text" name="targetPath" class="form-control selector-value">
                </p>
                
                <div class="panel panel-default">
                    <div class="panel-body">
                        <div class="fancytree fancytree-selector fixed-height" data-lazyloadingurl="${lazyLoadingURL}">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Space -->
    <div class="form-group">
        <label class="col-sm-3 control-label"><op:translate key="DOCUMENT_MOVE_SPACE" /></label>
        <div class="col-sm-9">
            <div class="form-inline">
                <p class="form-control-static">${spaceDocument.title}</p>
                <c:if test="${spaceDocument.type.name=='Workspace'}">
	                <a href="${changeSpaceURL}" class="btn btn-default btn-sm">
	                    <span><op:translate key="DOCUMENT_MOVE_CHANGE_SPACE" /></span>
	                </a>
                </c:if>
            </div>
        </div>
    </div>
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><op:translate key="MOVE" /></span>
            </button>
            <button type="button" class="btn btn-default" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
        </div>
    </div>
</form>
