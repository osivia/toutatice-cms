<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false" %>


<c:if test="${cmsMenu}">
    <c:set var="cmsMenuChecked" value="checked" />
</c:if>


<!-- Nuxeo path -->
<div class="form-group">
    <label for="nuxeo-path" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_NUXEO_PATH" /></label>
    <div class="col-sm-9">
        <input id="nuxeo-path" type="text" name="nuxeoPath" value="${nuxeoPath}" class="form-control" />
    </div>
</div>

<!-- Property name -->
<div class="form-group">
    <label for="property-name" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_PROPERTY_NAME" /></label>
    <div class="col-sm-9">
        <input id="property-name" type="text" name="propertyName" value="${propertyName}" class="form-control" />
        <span class="help-block"><is:getProperty key="FRAGMENT_PROPERTY_NAME_HELP" /></span>
    </div>
</div>

<!-- Scope -->
<div class="form-group">
    <label for="cms-scope" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_SCOPE" /></label>
    <div class="col-sm-9">
        <span>${scopes}</span>
    </div>
</div>

<!-- CMS menu display indicator -->
<div class="form-group">
    <div class="col-sm-offset-3 col-sm-9">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="cmsMenu" ${cmsMenuChecked}>
                <span><is:getProperty key="FRAGMENT_CMS_MENU" /></span>
            </label>
        </div>
    </div>
</div>
            