<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<c:if test="${onlyDescription}">
    <c:set var="onlyDescriptionChecked" value="checked" />
</c:if>

<c:if test="${metadata}">
    <c:set var="metadataChecked" value="checked" />
</c:if>


<div class="container">
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <!-- Path -->
        <div class="form-group">
            <label for="path" class="control-label col-sm-4"><is:getProperty key="DOCUMENT_PATH" /></label>
            <div class="col-sm-8">
                <input id="path" type="text" name="path" value="${path}" class="form-control" />
            </div>
        </div>
        
        <!-- Display indicators -->
        <div class="form-group">
            <label class="control-label col-sm-4"><is:getProperty key="DOCUMENT_DISPLAY" /></label>
            <div class="col-sm-8">
                <!-- Display only description indicator -->
                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="onlyDescription" ${onlyDescriptionChecked} />
                        <span><is:getProperty key="DOCUMENT_DISPLAY_ONLY_DESCRIPTION" /></span>
                    </label>
                </div>
                
                <!-- Display metadata indicator -->
                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="metadata" ${metadataChecked} />
                        <span><is:getProperty key="DOCUMENT_DISPLAY_METADATA" /></span>
                    </label>
                </div>
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
