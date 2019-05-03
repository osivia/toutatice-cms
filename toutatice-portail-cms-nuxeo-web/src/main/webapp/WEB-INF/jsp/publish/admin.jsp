<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<c:if test="${forceNavigation}">
    <c:set var="forceNavigationChecked" value="checked" />
</c:if>


<div class="container">
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <!-- Open levels -->
        <div class="form-group">
            <label for="open-levels" class="control-label col-sm-4"><op:translate key="MENU_OPEN_LEVELS" /></label>
            <div class="col-sm-8">
                <input id="open-levels" type="number" name="openLevels" value="${openLevels}" class="form-control" />
                <span class="help-block"><op:translate key="MESSAGE_MENU_OPEN_LEVELS_HELP" args="${defaultOpenLevels}" /></span>
            </div>
        </div>
        
        <!-- Start level -->
        <div class="form-group">
            <label for="start-level" class="control-label col-sm-4"><op:translate key="MENU_START_LEVEL" /></label>
            <div class="col-sm-8">
                <input id="start-level" type="number" name="startLevel" value="${startLevel}" class="form-control" />
                <span class="help-block"><op:translate key="MESSAGE_MENU_START_LEVEL_HELP" /></span>
            </div>
        </div>
        
        <!-- Max levels -->
        <div class="form-group">
            <label for="max-levels" class="control-label col-sm-4"><op:translate key="MENU_MAX_LEVELS" /></label>
            <div class="col-sm-8">
                <input id="max-levels" type="number" name="maxLevels" value="${maxLevels}" class="form-control" />
                <span class="help-block"><op:translate key="MESSAGE_MENU_MAX_LEVELS_HELP" args="${defaultMaxLevels}" /></span>
            </div>
        </div>

        <!-- Template -->
        <div class="form-group">
            <label for="template" class="control-label col-sm-4"><op:translate key="MENU_TEMPLATE" /></label>
            <div class="col-sm-8">
                <select id="template" name="template" class="form-control">
                    <c:forEach var="template" items="${templates}">
                        <c:remove var="selected" />
                        <c:if test="${template.key eq selectedTemplate}">
                            <c:set var="selected" value="selected" />
                        </c:if>
                    
                        <option value="${template.key}" ${selected}>${template.value}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        
        <!-- Force navigation -->
        <div class="form-group">
            <label for="force-navigation" class="control-label col-sm-4"><op:translate key="MENU_FORCE_NAVIGATION" /></label>
            <div class="col-sm-8">
                <div class="checkbox">
                    <label>
                        <input id="force-navigation" type="checkbox" name="forceNavigation" ${forceNavigationChecked} />
                        <span><op:translate key="MENU_FORCE_CURRENT_SPACE_NAVIGATION" /></span>
                    </label>
                </div>
                <span class="help-block"><op:translate key="MENU_FORCE_CURRENT_SPACE_NAVIGATION_HELP" /></span>
            </div>
        </div>
        
        <!-- Buttons -->
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <button type="submit" class="btn btn-secondary btn-primary"><op:translate key="SAVE" /></button>
                <button type="button" class="btn btn-secondary" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
            </div>
        </div>
    </form>
</div>
	