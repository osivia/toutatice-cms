<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<div class="container">
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <!-- Path -->
        <div class="form-group">
            <label for="search-path" class="control-label col-sm-4"><is:getProperty key="SEARCH_PATH" /></label>
            <div class="col-sm-8">
                <input id="search-path" type="text" name="path" value="${path}" class="form-control" placeholder='<is:getProperty key="SEARCH_PATH" />' />
            </div>
        </div>
        
        <!-- Version -->
        <div class="form-group">
            <label for="cms-scope" class="control-label col-sm-4"><is:getProperty key="SEARCH_VERSION" /></label>
            <div class="col-sm-8">
                <c:out value="${displayLiveVersionInput}" escapeXml="false" />
            </div>
        </div>

        <!-- Buttons -->
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <button type="submit" class="btn btn-default btn-primary"><is:getProperty key="SAVE" /></button>
                <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
            </div>
        </div>
    </form>
</div>

