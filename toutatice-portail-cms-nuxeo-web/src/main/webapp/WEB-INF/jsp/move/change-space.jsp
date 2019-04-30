<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<portlet:renderURL var="viewURL" />

<portlet:resourceURL id="fancytreeLazyLoading" var="lazyLoadingURL">
    <portlet:param name="live" value="true" />
    <portlet:param name="workspaces" value="true" />
</portlet:resourceURL>

<portlet:actionURL name="changeSpace" var="changeSpaceURL"></portlet:actionURL>


<c:set var="filterLabel"><op:translate key="FILTER" /></c:set>
<c:set var="clearFilterLabel"><op:translate key="CLEAR_FILTER" /></c:set>


<form action="${changeSpaceURL}" method="post" class="form-horizontal" role="form">
    <p class="lead">
        <i class="glyphicons glyphicons-move"></i>
        <span><op:translate key="DOCUMENT_MOVE_TITLE" /></span>
        <span>&ndash;</span>
        <span><op:translate key="DOCUMENT_MOVE_CHANGE_SPACE_TITLE" /></span>
    </p>
    
    <!-- Space path -->
    <div class="form-group">
        <label for="${namespace}-space-path" class="col-sm-3 control-label"><op:translate key="DOCUMENT_MOVE_SPACE" /></label>
        <div class="col-sm-9">        
            <div class="selector">
                <input id="${namespace}-space-path" type="hidden" name="spacePath" value="${cmsBasePath}" class="selector-value">
                
                <div class="panel panel-default">
                    <div class="panel-body">
                        <div class="fancytree fancytree-selector fixed-height" data-lazyloadingurl="${lazyLoadingURL}">
                            <p class="input-group input-group-sm">
                                <span class="input-group-addon">
                                    <i class="halflings halflings-filter"></i>
                                </span>
                                
                                <input type="text" class="form-control" placeholder="${filterLabel}">
                                
                                <span class="input-group-btn">
                                    <button type="button" class="btn btn-secondary" title="${clearFilterLabel}" data-toggle="tooltip" data-placement="bottom">
                                        <i class="halflings halflings-erase"></i>
                                        <span class="sr-only">${clearFilterLabel}</span>
                                    </button>
                                </span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="halflings halflings-ok"></i>
                <span><op:translate key="VALIDATE" /></span>
            </button>
            <a href="${viewURL}" class="btn btn-secondary">
                <span><op:translate key="BACK" /></span>
            </a>
        </div>
    </div>
</form>
