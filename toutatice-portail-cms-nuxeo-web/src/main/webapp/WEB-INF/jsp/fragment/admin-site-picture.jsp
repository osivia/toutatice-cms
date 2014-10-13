<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false" %>


<!-- Nuxeo path -->
<div class="form-group">
    <label for="nuxeo-path" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_NUXEO_PATH" /></label>
    <div class="col-sm-9">
        <input id="nuxeo-path" type="text" name="nuxeoPath" value="${nuxeoPath}" class="form-control" />
    </div>
</div>

<!-- Target path -->
<div class="form-group">
    <label for="target-path" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_TARGET_PATH" /></label>
    <div class="col-sm-9">
        <input id="target-path" type="text" name="targetPath" value="${targetPath}" class="form-control" />
        <span class="help-block"><is:getProperty key="FRAGMENT_TARGET_PATH_HELP" /></span>
    </div>
</div>
            