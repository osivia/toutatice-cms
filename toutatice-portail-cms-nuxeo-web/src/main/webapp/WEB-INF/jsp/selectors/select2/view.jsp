<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveUrl" />
<portlet:resourceURL id="select2-vocabulary" var="select2Url">
    <portlet:param name="vocabulary" value="${configuration.vocabulary}" />
    <portlet:param name="tree" value="true" />
</portlet:resourceURL>

<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="loadingLabel"><op:translate key="LOADING_LABEL"/></c:set>
<c:set var="clearLabel"><op:translate key="CLEAR"/></c:set>


<form action="${saveUrl}" method="post" role="form">
    <div class="form-group">
        <!-- Label -->
        <c:if test="${not empty configuration.label}">
            <label for="${namespace}-select" class="control-label">${configuration.label}</label>    
        </c:if>
        
        <!-- Selector -->
        <div class="input-group select2-bootstrap-append">
            <select id="${namespace}-select" name="vocabulary" class="form-control select2 select2-default" data-url="${select2Url}" data-loading-label="${loadingLabel}" data-onchange="submit"
                <c:if test="${not empty configuration.label}">data-placeholder="${configuration.label}"</c:if>
                <c:if test="${not configuration.monoValued}">multiple="multiple"</c:if>
            >
                <c:forEach var="selectedItem" items="${selectedItems}">
                    <option value="${selectedItem.key}" selected="selected">${selectedItem.value}</option>
                </c:forEach>
            </select>
            
            <!-- Clear -->
            <span class="input-group-btn">
                <button type="submit" name="clear" class="btn btn-secondary" title="${clearLabel}" data-toggle="tooltip" data-placement="bottom">
                    <i class="glyphicons glyphicons-delete"></i>
                    <span class="sr-only">${clearLabel}</span>
                </button>
            </span>
        </div>
    </div>
    
    <div class="form-group hidden-script">
        <!-- Submit button -->
        <button type="submit" name="save" class="btn btn-primary">
            <i class="glyphicons glyphicons-floppy-disk"></i>
            <span><op:translate key="SAVE" /></span>
        </button>
    </div>
</form>
