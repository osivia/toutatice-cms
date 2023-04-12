<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="reorder" var="reorderURL" />


<h3 class="h5 mb-3">
    <i class="glyphicons glyphicons-basic-sort"></i>
    <span><op:translate key="REORDER_DOCUMENTS_TITLE" /></span>
</h3>


<div class="alert alert-info">
    <span><op:translate key="REORDER_DOCUMENTS_MESSAGE_INFO" /></span>
</div>


<form action="${reorderURL}" method="post" role="form">
    <input type="hidden" name="order">
    
    
    <!-- Parent -->
    <div class="mb-3">
        <label class="form-label"><op:translate key="PARENT" /></label>
        <div class="form-control-plaintext">${parentTitle}</div>
    </div>
    
    
    <!-- Children -->
    <div class="mb-3">
        <label class="form-label"><op:translate key="REORDER_DOCUMENTS_LABEL" /></label>
        <c:choose>
            <c:when test="${empty documents}">
                <div class="form-text"><op:translate key="NO_ITEMS" /></div>
            </c:when>

            <c:otherwise>
                <ul class="list-unstyled sortable-default">
                    <c:forEach items="${documents}" var="document">
                        <li data-id="${document.id}">
                            <i class="${document.type.icon}"></i>
                            <span>${document.title}</span>
                        </li>
                    </c:forEach>
                </ul>
                <div class="form-text"><op:translate key="REORDER_DOCUMENTS_HELP" /></div>
            </c:otherwise>
        </c:choose>
    </div>
    
    
    <!-- Buttons -->
    <button type="submit" class="btn btn-primary">
        <span><op:translate key="REORDER" /></span>
    </button>
    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
        <span><op:translate key="CANCEL" /></span>
    </button>
</form>
