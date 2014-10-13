<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false" %>


<c:if test="${nuxeoLink}">
    <c:set var="nuxeoLinkChecked" value="checked" />
</c:if>


<!-- Link name -->
<div class="form-group">
    <label for="link-name" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_LINK_NAME" /></label>
    <div class="col-sm-9">
        <input id="link-name" type="text" name="name" value="${name}" class="form-control" />
        <span class="help-block"><is:getProperty key="FRAGMENT_LINK_NAME_HELP" /></span>
    </div>
</div>

<!-- Link target path & Nuxeo link indicator -->
<div class="form-group">
    <label for="target-path" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_LINK_TARGET_PATH" /></label>
    <div class="col-sm-9">
        <input id="target-path" type="text" name="targetPath" value="${targetPath}" class="form-control" />
    </div>
    <div class="col-sm-offset-3 col-sm-9">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="nuxeoLink" ${nuxeoLinkChecked}>
                <span><is:getProperty key="FRAGMENT_LINK_NUXEO_INDICATOR" /></span>
            </label>
        </div>
    </div>
</div>

<!-- CSS classes -->
<div class="form-group">
    <label for="css-classes" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_LINK_CSS_CLASSES" /></label>
    <div class="col-sm-9">
        <input id="css-classes" type="text" name="cssClasses" value="${cssClasses}" class="form-control" />
        <span class="help-block"><is:getProperty key="FRAGMENT_LINK_CSS_CLASSES_HELP" /></span>
    </div>
</div>
