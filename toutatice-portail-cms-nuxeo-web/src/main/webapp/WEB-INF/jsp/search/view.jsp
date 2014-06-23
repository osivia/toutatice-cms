<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<c:set var="namespace">
    <portlet:namespace />
</c:set>


<script type="text/javascript">
function onSubmitSearch(form) {
	var searchUrl = "${searchUrl}";
	searchUrl = searchUrl.replace("__REPLACE_KEYWORDS__", form.keywords.value);
	form.action = searchUrl;
}
</script>


<div class="nuxeo-input-search-small">
    <form method="post" onsubmit="return onSubmitSearch(this);" class="form" role="search">
        <div class="form-group">
            <label class="sr-only" for="${namespace}-search-input">Search</label>
            <div class="input-group">
                <input id="${namespace}-search-input" type="text" name="keywords" class="form-control" placeholder='<is:getProperty key="SEARCH_PLACEHOLDER" />'>
                <span class="input-group-btn">
                    <button type="submit" class="btn btn-default" title='<is:getProperty key="SEARCH_TITLE" />' data-toggle="tooltip" data-placement="bottom">
                        <span class="glyphicons halflings search"></span>
                    </button>
                </span>
            </div>
        </div>
    </form>
</div>
