<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<div class="container">
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <!-- Path -->
        <div class="form-group">
            <label for="search-path" class="control-label col-sm-4"><op:translate key="SEARCH_PATH" /></label>
            <div class="col-sm-8">
                <input id="search-path" type="text" name="path" value="${path}" class="form-control" placeholder='<op:translate key="SEARCH_PATH" />' />
            </div>
        </div>
        <!-- Buttons -->
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <button type="submit" class="btn btn-default btn-primary"><op:translate key="SAVE" /></button>
                <button type="button" class="btn btn-default" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
            </div>
        </div>
    </form>
</div>

