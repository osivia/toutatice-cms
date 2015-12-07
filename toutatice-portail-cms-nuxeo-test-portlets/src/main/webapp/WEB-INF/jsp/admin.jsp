<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects />

<portlet:actionURL name="save" var="url" />

<c:set var="namespace"><portlet:namespace/></c:set>


<form action="${url}" method="post" class="form-horizontal" role="form">
    <!-- Default tab -->
    <div class="form-group">
        <label for="${namespace}-default-tab" class="control-label col-sm-3"><op:translate key="DEFAULT_TAB" /></label>
        <div class="col-sm-9">
            <select id="${namespace}-default-tab" name="defaultTab" class="form-control">
                <c:forEach var="tab" items="${tabs}">
                    <option value="${tab.id}"
                        <c:if test="${configuration.defaultTab eq tab}">selected</c:if>
                    ><op:translate key="${tab.key}" /></option>
                </c:forEach>
            </select>
        </div>
    </div>


    <fieldset>
        <legend><op:translate key="TAGS" /></legend>
        
        <!-- Document path -->
        <div class="form-group">
            <label for="${namespace}-path" class="control-label col-sm-3"><op:translate key="DOCUMENT_PATH" /></label>
            <div class="col-sm-9">
                <input id="${namespace}-path" type="text" name="path" value="${configuration.path}" class="form-control" />
            </div>
        </div>
        
        <!-- User name -->
        <div class="form-group">
            <label for="${namespace}-user" class="control-label col-sm-3"><op:translate key="USER_NAME" /></label>
            <div class="col-sm-9">
                <input id="${namespace}-user" type="text" name="user" value="${configuration.user}" class="form-control" />
            </div>
        </div>
    </fieldset>

    
    <fieldset>
        <legend><op:translate key="ATTRIBUTES_STORAGE" /></legend>
        
        <!-- Selection identifier -->
        <div class="form-group">
            <label for="${namespace}-selection-id" class="control-label col-sm-3"><op:translate key="SELECTION_IDENTIFIER" /></label>
            <div class="col-sm-9">
                <input id="${namespace}-selection-id" type="text" name="selectionId" value="${configuration.selectionId}" class="form-control">
            </div>
        </div>
    </fieldset>
    
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><op:translate key="SAVE" /></span>
            </button>
            
            <button type="button" class="btn btn-default" onclick="closeFancybox()">
                <span><op:translate key="CANCEL" /></span>
            </button>
        </div>
    </div>
</form>
