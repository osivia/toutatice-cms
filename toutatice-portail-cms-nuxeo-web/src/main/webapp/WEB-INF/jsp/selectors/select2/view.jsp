<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects/>

<portlet:actionURL name="save" var="saveUrl"/>

<portlet:resourceURL id="select2-vocabulary" var="select2Url">
    <portlet:param name="vocabulary" value="${configuration.vocabulary}"/>
    <portlet:param name="tree" value="true"/>
    <portlet:param name="allLabel" value="${configuration.allLabel}"/>
</portlet:resourceURL>

<c:set var="namespace"><portlet:namespace/></c:set>

<c:set var="searching"><op:translate key="SELECT2_SEARCHING"/></c:set>
<c:set var="noResults"><op:translate key="SELECT2_NO_RESULTS"/></c:set>


<div class="selector vocabulary-selector">
    <form action="${saveUrl}" method="post" role="form">
        <div class="form-group">
            <!-- Label -->
            <c:if test="${not empty configuration.label}">
                <label for="${namespace}-select">${configuration.label}</label>
            </c:if>

            <!-- Selector -->
            <div class="input-group">
            
                <c:if test="${not configuration.monoValued}">
                    <c:set var="placeholder">${configuration.allLabel}</c:set>
                </c:if> 
            
                <select id="${namespace}-select" name="vocabulary" class="form-control select2 select2-default"
                        data-url="${select2Url}" data-placeholder="${placeholder}"
                        data-searching="${searching}" data-onchange="submit"
                        <c:if test="${not configuration.monoValued}">multiple="multiple"</c:if>
                >
                    <c:if test="${configuration.monoValued}">
                        <option value="" ${empty selectedItems ? 'selected="selected"' : ''}>${empty configuration.allLabel ? '' : configuration.allLabel}</option>
                    </c:if> 
                    <c:forEach var="selectedItem" items="${selectedItems}">
                        <option value="${selectedItem.key}" selected="selected">${selectedItem.value}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="form-group d-none">
            <!-- Submit button -->
            <button type="submit" name="save" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><op:translate key="SAVE"/></span>
            </button>
        </div>
    </form>
</div>
