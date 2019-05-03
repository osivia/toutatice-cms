<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveActionURL" />


<form action="${saveActionURL}" method="post" class="form-horizontal" role="form">
    <!-- Path -->
    <div class="form-group">
        <label for="path" class="control-label col-sm-3"><op:translate key="DOCUMENT_PATH" /></label>
        <div class="col-sm-9">
            <input id="path" type="text" name="path" value="${path}" class="form-control" >
        </div>
    </div>
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary"><op:translate key="SAVE" /></button>
            <button type="button" class="btn btn-secondary" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
        </div>
    </div>
</form>
