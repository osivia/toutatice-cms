<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<c:if test="${cmsMenu}">
    <c:set var="cmsMenuChecked" value="checked" />
</c:if>


<!-- Nuxeo path -->
<div class="form-group">
    <label for="nuxeo-path" class="control-label col-sm-3"><op:translate key="FRAGMENT_NUXEO_PATH" /></label>
    <div class="col-sm-9">
        <input id="nuxeo-path" type="text" name="nuxeoPath" value="${nuxeoPath}" class="form-control" />
    </div>
</div>

<!-- Property name -->
<div class="form-group">
    <label for="property-name" class="control-label col-sm-3"><op:translate key="FRAGMENT_PROPERTY_NAME" /></label>
    <div class="col-sm-9">
        <input id="property-name" type="text" name="propertyName" value="${propertyName}" class="form-control" />
        <span class="help-block"><op:translate key="FRAGMENT_PROPERTY_NAME_HELP" /></span>
    </div>
</div>

<!-- Scope -->
<div class="form-group">
    <label for="cms-scope" class="control-label col-sm-3"><op:translate key="FRAGMENT_SCOPE" /></label>
    <div class="col-sm-9">
        <span>${scopes}</span>
    </div>
</div>

<!-- Target path -->
<div class="form-group">
    <label for="target-path" class="control-label col-sm-3"><op:translate key="FRAGMENT_TARGET_PATH" /></label>
    <div class="col-sm-9">
        <input id="target-path" type="text" name="targetPath" value="${targetPath}" class="form-control" />
        <span class="help-block"><op:translate key="FRAGMENT_TARGET_PATH_HELP" /></span>
    </div>
</div>
            