<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects/>

<portlet:actionURL name="save" var="saveUrl"/>

<c:set var="namespace"><portlet:namespace/></c:set>


<form action="${saveUrl}" method="post" class="form-horizontal" role="form">

    <!-- Label -->
    <div class="form-group">
        <label for="${namespace}-label" class="col-sm-3 col-form-label"><op:translate key="SELECTOR_LABEL"/></label>
        <div class="col-sm-9">
            <input id="${namespace}-label" type="text" name="label" value="${configuration.label}" class="form-control">
        </div>
    </div>


    <%--"all" label--%>
    <div class="form-group">
        <label for="${namespace}-all-label" class="col-sm-3 col-form-label"><op:translate
                key="SELECTOR_ALL_LABEL"/></label>
        <div class="col-sm-9">
            <input id="${namespace}-all-label" type="text" name="allLabel" value="${configuration.allLabel}"
                   class="form-control">
        </div>
    </div>

    <!-- Identifier -->
    <div class="form-group">
        <label for="${namespace}-id" class="col-sm-3 col-form-label"><op:translate key="SELECTOR_IDENTIFIER"/></label>
        <div class="col-sm-9">
            <input id="${namespace}-id" type="text" name="id" value="${configuration.id}" class="form-control">
        </div>
    </div>

    <!-- Vocabulary -->
    <div class="form-group">
        <label for="${namespace}-vocabulary" class="col-sm-3 col-form-label"><op:translate
                key="SELECTOR_VOCABULARY"/></label>
        <div class="col-sm-9">
            <input id="${namespace}-vocabulary" type="text" name="vocabulary" value="${configuration.vocabulary}"
                   class="form-control">
        </div>
    </div>

    <!-- Mono-valued indicator -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="monoValued"
                           <c:if test="${configuration.monoValued}">checked="checked"</c:if>
                    >
                    <span><op:translate key="SELECTOR_TYPE_MONO_VALUED"/></span>
                </label>
            </div>
        </div>
    </div>

    <!-- Buttons -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons glyphicons-floppy-disk"></i>
                <span><op:translate key="SAVE"/></span>
            </button>

            <button type="button" class="btn btn-secondary" onclick="closeFancybox()">
                <span><op:translate key="CANCEL"/></span>
            </button>
        </div>
    </div>

</form>
