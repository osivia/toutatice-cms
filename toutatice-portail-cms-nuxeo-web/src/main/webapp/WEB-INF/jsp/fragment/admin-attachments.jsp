<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<c:set var="namespace"><portlet:namespace /></c:set>


<!-- Document path -->
<div class="form-group">
    <label for="${namespace}-path" class="control-label col-sm-3"><op:translate key="FRAGMENT_NUXEO_PATH" /></label>
    <div class="col-sm-9">
        <input id="${namespace}-path" type="text" name="path" value="${path}" class="form-control">
    </div>
</div>
