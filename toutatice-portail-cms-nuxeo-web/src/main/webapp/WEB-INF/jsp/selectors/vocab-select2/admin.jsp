<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<c:set var="othersLabelPlaceholder"><is:getProperty key="SELECTOR_OTHERS_LABEL" /></c:set>


<form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
    <!-- Label -->
    <div class="form-group">
        <label for="selector-label" class="control-label col-sm-3"><is:getProperty key="SELECTOR_LABEL" /></label>
        <div class="col-sm-9">
            <input id="selector-label" type="text" name="libelle" value="${libelle}" class="form-control">
        </div>
    </div>
    
    <!-- Identifier -->
    <div class="form-group">
        <label for="selector-id" class="control-label col-sm-3"><is:getProperty key="SELECTOR_IDENTIFIER" /></label>
        <div class="col-sm-9">
            <input id="selector-id" type="text" name="selectorId" value="${selectorId}" class="form-control">
        </div>
    </div>
    
    <!-- Mono-valued indicator -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="selectorMonoValued" value="1"
                        <c:if test="${selectorMonoValued eq '1'}">checked="checked"</c:if>
                    >
                    <span><is:getProperty key="SELECTOR_MONO_VALUED" /></span>
                </label>
            </div>
        </div>
    </div>
    
    <!-- Multi level vocabulary -->
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="selectorMultiLevel" value="1"
                        <c:if test="${selectorMultiLevel eq '1'}">checked="checked"</c:if>
                    >
                    <span><is:getProperty key="SELECTOR_MULTI_LEVEL_VOCAB" /></span>
                </label>
            </div>
        </div>
    </div>    
    
    <!-- Vocabulary -->
    <div class="form-group">
        <label for="vocabulary-1" class="control-label col-sm-3"><is:getProperty key="SELECTOR_VOCABULARY" /></label>
        <div class="col-sm-9">
            <input id="vocabulary" type="text" name="vocabName" value="${vocabName}" class="form-control">
        </div>
    </div>
   
    
    <!-- Buttons -->
    <div class="row">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="halflings halflings-floppy-disk"></i>
                <span><is:getProperty key="SAVE" /></span>
            </button>
            
            <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
        </div>
    </div>
</form>
