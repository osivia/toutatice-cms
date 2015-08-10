<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="reorder" var="reorderURL" />


<form action="${reorderURL}" method="post" class="form-horizontal no-ajax-link" role="form">
    <p class="lead">
        <i class="glyphicons glyphicons-sorting"></i>
        <span><is:getProperty key="REORDER_DOCUMENTS_TITLE" /></span>
    </p>


    <input type="hidden" name="order">
    
    
    <!-- Documents -->
    <div class="form-group">
        <label class="control-label col-sm-3"><is:getProperty key="REORDER_DOCUMENTS_LABEL" /></label>
        <div class="col-sm-9">
            <ul class="list-sortable reorder-sortable">
                <c:forEach items="${documents}" var="document">
                    <li data-id="${document.id}">
                        <i class="${document.type.glyph}"></i>
                        <span>${document.title}</span>
                    </li>
                </c:forEach>
            </ul>
            <div class="help-block"><is:getProperty key="REORDER_DOCUMENTS_HELP" /></div>
        </div>
    </div>
    
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><is:getProperty key="REORDER" /></span>
            </button>
            <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
        </div>
    </div>
</form>
