<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="reorder" var="reorderURL" />


<p class="lead">
    <i class="glyphicons glyphicons-sorting"></i>
    <span><op:translate key="REORDER_DOCUMENTS_TITLE" /></span>
</p>


<div class="alert alert-info">
    <div class="media">
        <div class="media-left media-middle">
            <strong><i class="glyphicons glyphicons-info-sign"></i></strong>
        </div>
        
        <div class="media-body">
            <span><op:translate key="REORDER_DOCUMENTS_MESSAGE_INFO" /></span>
        </div>
    </div>
</div>


<form action="${reorderURL}" method="post" class="form-horizontal no-ajax-link" role="form">
    <input type="hidden" name="order">
    
    
    <!-- Parent -->
    <div class="form-group">
        <label class="control-label col-sm-3"><op:translate key="PARENT" /></label>
        <div class="col-sm-9">
            <p class="form-control-static">${parentTitle}</p>
        </div>
    </div>
    
    
    <!-- Children -->
    <div class="form-group">
        <label class="control-label col-sm-3"><op:translate key="REORDER_DOCUMENTS_LABEL" /></label>
        <div class="col-sm-9">
            <c:choose>
                <c:when test="${empty documents}">
                    <p class="form-control-static text-muted"><op:translate key="NO_ITEMS" /></p>
                </c:when>
                
                <c:otherwise>
                    <ul class="list-sortable reorder-sortable">
                        <c:forEach items="${documents}" var="document">
                            <li data-id="${document.id}">
                                <i class="${document.type.icon}"></i>
                                <span>${document.title}</span>
                            </li>
                        </c:forEach>
                    </ul>
                    <div class="help-block"><op:translate key="REORDER_DOCUMENTS_HELP" /></div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    
    
    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><op:translate key="REORDER" /></span>
            </button>
            <button type="button" class="btn btn-secondary" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
        </div>
    </div>
</form>
