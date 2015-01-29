<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<script type="text/javascript">
$JQry(document).ready(function() {
    $JQry("input[name=othersOption]").change(function() {
    	var $this = $JQry(this);
        var $form = $this.parents("form");
        var $label = $form.find("input[name=othersLabel]");
        
        $label.prop("disabled", !$this.is(":checked"));
    });
});
</script>


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
    
    <!-- Option "others" -->
    <div class="form-group">
        <label for="selector-other" class="control-label col-sm-3"><is:getProperty key="SELECTOR_OTHERS" /></label>
        <div class="col-sm-9">
            <div class="checkbox">
                <p>
                    <label>
                        <input id="selector-other" type="checkbox" name="othersOption" value="1"
                            <c:if test="${othersOption eq '1'}">checked="checked"</c:if>
                        >
                        <span><is:getProperty key="SELECTOR_OTHERS_ACTIVATE" /></span>
                    </label>
                </p>
            </div>
            <input type="text" name="othersLabel" value="${othersLabel}" placeholder="${othersLabelPlaceholder}" class="form-control"
                <c:if test="${othersOption ne '1'}">disabled="disabled"</c:if>
            >
        </div>
    </div>
    
    <!-- Vocabulary level 1 -->
    <div class="form-group">
        <label for="vocabulary-1" class="control-label col-sm-3"><is:getProperty key="SELECTOR_VOCABULARY_LEVEL" args="1" /></label>
        <div class="col-sm-9">
            <input id="vocabulary-1" type="text" name="vocabName1" value="${vocabName1}" class="form-control">
        </div>
    </div>
    
    <!-- Vocabulary level 1 preselection -->
    <div class="form-group">
        <label for="vocabulary-1-preselection" class="control-label col-sm-3"><is:getProperty key="SELECTOR_VOCABULARY_LEVEL_1_PRESELECTION" /></label>
        <div class="col-sm-9">
            <input id="vocabulary-1-preselection" type="text" name="preselect1" value="${preselect1}" class="form-control">
            <span class="help-block"><is:getProperty key="SELECTOR_VOCABULARY_LEVEL_1_PRESELECTION_HELP" /></span>
        </div>
    </div>
    
    <!-- Vocabulary level 2 -->
    <div class="form-group">
        <label for="vocabulary-2" class="control-label col-sm-3"><is:getProperty key="SELECTOR_VOCABULARY_LEVEL" args="2" /></label>
        <div class="col-sm-9">
            <input id="vocabulary-2" type="text" name="vocabName2" value="${vocabName2}" class="form-control">
        </div>
    </div>
    
    <!-- Vocabulary level 3 -->
    <div class="form-group">
        <label for="vocabulary-3" class="control-label col-sm-3"><is:getProperty key="SELECTOR_VOCABULARY_LEVEL" args="3" /></label>
        <div class="col-sm-9">
            <input id="vocabulary-3" type="text" name="vocabName3" value="${vocabName3}" class="form-control">
        </div>
    </div>
    
    
    <!-- Buttons -->
    <div class="row">
        <div class="col-sm-offset-3 col-sm-9">
            <button type="submit" class="btn btn-primary">
                <i class="glyphicons halflings floppy_disk"></i>
                <span><is:getProperty key="SAVE" /></span>
            </button>
            
            <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
        </div>
    </div>
</form>
