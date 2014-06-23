<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<c:if test='${keywordMonoValued == "1"}'>
    <c:set var="monoValuedChecked" value="checked" />
</c:if>


<div class="container">
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <!-- Label -->
        <div class="form-group">
            <label for="selector-label" class="control-label col-sm-4"><is:getProperty key="SELECTOR_LABEL" /></label>
            <div class="col-sm-8">
                <input id="selector-label" type="text" name="libelle" value="${libelle}" class="form-control" placeholder='<is:getProperty key="SELECTOR_LABEL" />' />
            </div>
        </div>

        <!-- Identifier -->
        <div class="form-group">
            <label for="selector-identifier" class="control-label required col-sm-4"><is:getProperty key="SELECTOR_IDENTIFIER" /></label>
            <div class="col-sm-8">
                <input id="selector-identifier" type="text" name="selectorId" value="${selectorId}" required="required" class="form-control"
                    placeholder='<is:getProperty key="SELECTOR_IDENTIFIER" />' />
            </div>
        </div>

        <!-- Mono-valued indicator -->
        <div class="form-group">
            <label for="selector-mono-valued" class="control-label col-sm-4"><is:getProperty key="SELECTOR_MONO_VALUED" /></label>
            <div class="col-sm-8">
                <div class="checkbox">
                    <input id="selector-mono-valued" type="checkbox" name="keywordMonoValued" value="1" ${monoValuedChecked} />
                </div>
            </div>
        </div>

        <!-- Buttons -->
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <button type="submit" class="btn btn-default btn-primary"><is:getProperty key="SAVE" /></button>
                <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
            </div>
        </div>

    </form>
</div>
