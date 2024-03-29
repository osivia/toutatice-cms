<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
    <!-- Label -->
    <div class="form-group">
        <label for="selector-label" class="control-label col-sm-3"><op:translate key="SELECTOR_LABEL" /></label>
        <div class="col-sm-9">
            <input id="selector-label" type="text" name="selectorLabel" value="${selectorLabel}" class="form-control">
        </div>
    </div>
    
    <!-- Identifier -->
    <div class="form-group">
        <label for="selector-id" class="control-label col-sm-3"><op:translate key="SELECTOR_IDENTIFIER" /></label>
        <div class="col-sm-9">
            <input id="selector-id" type="text" name="selectorId" value="${selectorId}" class="form-control">
        </div>
    </div>
    
    <!-- Selector type -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <!-- Multi-valued -->
            <div class="radio">
                <label>
                    <input type="radio" name="selectorType" value="0" ${selectorType eq '0' ? 'checked' : ''}>
                    <span><op:translate key="SELECTOR_TYPE_MULTI_VALUED"/></span>
                </label>
            </div>
            
            <!-- Mono-valued -->
            <div class="radio">
                <label>
                    <input type="radio" name="selectorType" value="1" ${selectorType eq '1' ? 'checked' : ''}>
                    <span><op:translate key="SELECTOR_TYPE_MONO_VALUED"/></span>
                </label>
            </div>
            
            <!-- Mono-valued with auto-submit -->
            <div class="radio">
                <label>
                    <input type="radio" name="selectorType" value="2" ${selectorType eq '2' ? 'checked' : ''}>
                    <span><op:translate key="SELECTOR_TYPE_MONO_VALUED_AUTO_SUBMIT"/></span>
                </label>
            </div>
        </div>
    </div>
    
    <!-- Buttons -->
    <div class="row">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="halflings halflings-floppy-disk"></i>
                <span><op:translate key="SAVE" /></span>
            </button>
            
        </div>
    </div>
</form>
