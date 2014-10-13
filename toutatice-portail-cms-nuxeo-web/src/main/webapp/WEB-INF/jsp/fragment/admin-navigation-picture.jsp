<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false" %>


<!-- Property name -->
<div class="form-group">
    <label for="property-name" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_PROPERTY_NAME" /></label>
    <div class="col-sm-9">
        <input id="property-name" type="text" name="propertyName" value="${propertyName}" class="form-control" />
        <span class="help-block"><is:getProperty key="FRAGMENT_PICTURE_PROPERTY_NAME_HELP" /></span>
    </div>
</div>
