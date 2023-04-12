<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:renderURL var="changeSpaceURL">
    <portlet:param name="osivia.move.mode" value="space" />
</portlet:renderURL>

<portlet:resourceURL id="fancytreeLazyLoading" var="lazyLoadingURL">
    <portlet:param name="cmsBasePath" value="${cmsBasePath}" />
    <portlet:param name="cmsNavigationPath" value="${cmsNavigationPath}" />
    <portlet:param name="live" value="true" />
    <portlet:param name="ignoredPaths" value="${ignoredPaths}" />
    <portlet:param name="acceptedTypes" value="${acceptedTypes}" />
    <portlet:param name="excludedTypes" value="${excludedTypes}" />
</portlet:resourceURL>

<portlet:actionURL name="move" var="moveURL"/>


<c:set var="namespace"><portlet:namespace /></c:set>


<form action="${moveURL}" method="post" role="form">
    <h3 class="h5 mb-3">
        <i class="glyphicons glyphicons-basic-block-move"></i>
        <span><op:translate key="DOCUMENT_MOVE_TITLE" /></span>
    </h3>

    <!-- Target path -->
    <div class="mb-3">
        <label class="form-label"><op:translate key="DOCUMENT_MOVE_TARGET_PATH" /></label>
        <div class="selector">
            <input type="hidden" name="targetPath" class="selector-value">

            <div class="card ${error eq 'emptyTargetPath' or error eq '403' ? 'border-danger' : ''}">
                <div class="card-body">
                    <div class="fancytree fancytree-selector fixed-height" data-lazyloadingurl="${lazyLoadingURL}">
                    </div>
                </div>
            </div>

            <c:choose>
                <c:when test="${error eq 'emptyTargetPath'}">
                    <div class="invalid-feedback"><op:translate key="DOCUMENT_MOVE_EMPTY_TARGET_PATH_MESSAGE" /></div>
                </c:when>

                <c:when test="${error eq '403'}">
                    <div class="invalid-feedback"><op:translate key="DOCUMENT_MOVE_FORBIDDEN_MESSAGE" /></div>
                </c:when>
            </c:choose>
        </div>
    </div>
    
    <!-- Space change -->
    <c:if test="${enableSpaceChange}">
        <div class="mb-3">
            <label class="form-label"><op:translate key="DOCUMENT_MOVE_SPACE" /></label>
            <div class="form-inline">
                <p class="form-control-static">${spaceDocument.title}</p>
                <c:if test="${spaceDocument.type.name=='Workspace'}">
                    <a href="${changeSpaceURL}" class="btn btn-default btn-sm">
                        <span><op:translate key="DOCUMENT_MOVE_CHANGE_SPACE" /></span>
                    </a>
                </c:if>
            </div>
        </div>
    </c:if>
    
    <!-- Buttons -->
    <button type="submit" class="btn btn-primary">
        <span><op:translate key="MOVE" /></span>
    </button>
    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
        <span><op:translate key="CANCEL" /></span>
    </button>
</form>
