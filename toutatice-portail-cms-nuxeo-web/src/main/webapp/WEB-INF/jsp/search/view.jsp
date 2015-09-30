<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<c:set var="namespace"><portlet:namespace /></c:set>


<form method="post" onsubmit="return onSubmitSearch(this);" class="form" role="search" data-url="${searchUrl}">
    <div class="form-group">
        <label class="sr-only" for="${namespace}-search-input">Search</label>
        <div class="input-group">
            <input id="${namespace}-search-input" type="text" name="keywords" class="form-control" placeholder='<op:translate key="SEARCH_PLACEHOLDER" />'>
            <span class="input-group-btn">
                <button type="submit" class="btn btn-default" title='<op:translate key="SEARCH_TITLE" />' data-toggle="tooltip" data-placement="bottom">
                    <span class="halflings halflings-search"></span>
                </button>
            </span>
        </div>
    </div>
</form>
