<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
    <!-- Path -->
    <div class="form-group">
        <label for="path" class="control-label col-sm-4"><op:translate key="DOCUMENT_PATH" /></label>
        <div class="col-sm-8">
            <input id="path" type="text" name="path" value="${path}" class="form-control" />
        </div>
    </div>
    
    <!-- Display indicators -->
    <div class="form-group">
        <label class="control-label col-sm-4"><op:translate key="DOCUMENT_DISPLAY" /></label>
        <div class="col-sm-8">
            <!-- Display only description indicator -->
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="onlyDescription"
                        <c:if test="${onlyDescription}">checked="checked"</c:if>
                    />
                    <span><op:translate key="DOCUMENT_DISPLAY_ONLY_DESCRIPTION" /></span>
                </label>
            </div>
            
            <!-- Display metadata indicator -->
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="metadata"
                        <c:if test="${metadata}">checked="checked"</c:if>
                    />
                    <span><op:translate key="DOCUMENT_DISPLAY_METADATA" /></span>
                </label>
            </div>
            
            <!-- Display attachments indicator -->
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="attachments"
                        <c:if test="${attachments}">checked="checked"</c:if>
                    />
                    <span><op:translate key="DOCUMENT_DISPLAY_ATTACHMENTS" /></span>
                </label>
            </div>
        </div>
    </div>
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-4 col-sm-8">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><op:translate key="SAVE" /></span>
            </button>
            <button type="button" class="btn btn-default" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
        </div>
    </div>
</form>
