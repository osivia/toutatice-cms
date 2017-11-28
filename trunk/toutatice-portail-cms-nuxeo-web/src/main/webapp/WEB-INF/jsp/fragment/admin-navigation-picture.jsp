<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<!-- Property name -->
<div class="form-group">
    <label for="property-name" class="control-label col-sm-3"><op:translate key="FRAGMENT_PROPERTY_NAME" /></label>
    <div class="col-sm-9">
        <input id="property-name" type="text" name="propertyName" value="${propertyName}" class="form-control" />
        <span class="help-block"><op:translate key="FRAGMENT_PICTURE_PROPERTY_NAME_HELP" /></span>
    </div>
</div>
