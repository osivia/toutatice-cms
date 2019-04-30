<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />

<div>
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <div class="form-horizontal panel-default">     
            <div class="form-group">
                 <label for="procedureModelId" class="control-label col-sm-3"><op:translate key="LIST_TEMPLATE_PROCEDURE_MODEL_ID" /></label>
			    <div class="col-sm-9">
			        <input id="procedureModelId" type="text" name="procedureModelId" value="${configuration.procedureModelId}" class="form-control">
			    </div>
            </div>
            <div class="form-group">
                 <label for="dashboardId" class="control-label col-sm-3"><op:translate key="LIST_TEMPLATE_PROCEDURE_DASHBOARD_ID" /></label>
                <div class="col-sm-9">
                    <input id="dashboardId" type="text" name="dashboardId" value="${configuration.dashboardId}" class="form-control">
                </div>
            </div>
            
            <!-- Buttons -->
	        <div class="form-group">
                <div class="col-sm-offset-3 col-sm-9">
                    <button type="submit" class="btn btn-primary navbar-btn">
                        <i class="halflings halflings-floppy-disk"></i>
                        <span><op:translate key="SAVE" /></span>
                    </button>
                    <button type="button" class="btn btn-secondary navbar-btn" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
	            </div>
	        </div>
        </div>
    </form>
</div>