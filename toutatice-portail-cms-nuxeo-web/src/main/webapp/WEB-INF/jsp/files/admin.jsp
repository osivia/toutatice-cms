<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveActionURL" />


<form action="${saveActionURL}" method="post" class="form-horizontal" role="form">
    <!-- Path -->
    <div class="form-group">
        <label for="path" class="control-label col-sm-3"><is:getProperty key="DOCUMENT_PATH" /></label>
        <div class="col-sm-9">
            <input id="path" type="text" name="path" value="${path}" class="form-control" >
        </div>
    </div>
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary"><is:getProperty key="SAVE" /></button>
            <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
        </div>
    </div>
</form>
