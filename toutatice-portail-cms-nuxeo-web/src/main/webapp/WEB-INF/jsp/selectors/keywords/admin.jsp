<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<form action="${saveAdminURL}" method="post" role="form">
    <fieldset>
        <legend><op:translate key="SELECTOR_KEYWORDS_REQUIRED_SETTINGS_LEGEND"/></legend>

        <%--Identifier--%>
        <div class="form-group required">
            <label for="selector-id" class="control-label"><op:translate key="SELECTOR_IDENTIFIER" /></label>
            <input id="selector-id" type="text" name="selectorId" value="${selectorId}" class="form-control">
        </div>

        <%--Selector type--%>
        <div class="form-group required">
            <label class="control-label"><op:translate key="SELECTOR_TYPE"/></label>

            <%--Multi-valued--%>
            <div class="radio">
                <label>
                    <input type="radio" name="selectorType" value="0" ${selectorType eq '0' ? 'checked' : ''}>
                    <span><op:translate key="SELECTOR_TYPE_MULTI_VALUED"/></span>
                </label>
            </div>

            <%--Mono-valued--%>
            <div class="radio">
                <label>
                    <input type="radio" name="selectorType" value="1" ${selectorType eq '1' ? 'checked' : ''}>
                    <span><op:translate key="SELECTOR_TYPE_MONO_VALUED"/></span>
                </label>
            </div>

            <%--Mono-valued with auto-submit--%>
            <div class="radio">
                <label>
                    <input type="radio" name="selectorType" value="2" ${selectorType eq '2' ? 'checked' : ''}>
                    <span><op:translate key="SELECTOR_TYPE_MONO_VALUED_AUTO_SUBMIT"/></span>
                </label>
            </div>
        </div>
    </fieldset>

    <fieldset>
        <legend><op:translate key="SELECTOR_KEYWORDS_DISPLAY_SETTINGS_LEGEND"/></legend>

        <%--Label--%>
        <div class="form-group">
            <label for="selector-label" class="control-label"><op:translate key="SELECTOR_LABEL" /></label>
            <input id="selector-label" type="text" name="selectorLabel" value="${selectorLabel}" class="form-control">
        </div>

        <%--Placeholder--%>
        <div class="form-group">
            <label for="placeholder" class="control-label"><op:translate key="SELECTOR_PLACEHOLDER"/></label>
            <input id="placeholder" type="text" name="selectorPlaceholder" value="${selectorPlaceholder}" class="form-control">
        </div>

        <%--HTML identifier--%>
        <div class="form-group">
            <label for="html-identifier" class="control-label"><op:translate key="SELECTOR_HTML_IDENTIFIER"/></label>
            <input id="html-identifier" type="text" name="selectorHtmlIdentifier" value="${selectorHtmlIdentifier}" class="form-control">
            <p class="help-block"><op:translate key="SELECTOR_HTML_IDENTIFIER_HELP"/></p>
        </div>
    </fieldset>

    <%--Buttons--%>
    <div>
        <button type="submit" class="btn btn-primary no-ajax-link">
            <i class="halflings halflings-floppy-disk"></i>
            <span><op:translate key="SAVE" /></span>
        </button>

        <button type="button" class="btn btn-default" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
    </div>
</form>
