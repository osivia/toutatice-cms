<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.publish.MenuPortlet"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />


<c:if test='${jstree == "1"}'>
    <c:set var="jstreeChecked" value="checked" />
</c:if>


<div class="container">
    <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
        <!-- Open levels -->
        <div class="form-group">
            <label for="open-levels" class="control-label col-sm-4"><is:getProperty key="MENU_OPEN_LEVELS" /></label>
            <div class="col-sm-8">
                <input id="open-levels" type="text" name="openLevels" value="${openLevels}" class="form-control" placeholder='<is:getProperty key="MENU_OPEN_LEVELS" />' />
                <span class="help-block"><is:getProperty key="MESSAGE_MENU_OPEN_LEVELS_HELP" args="${defaultOpenLevels}" /></span>
            </div>
        </div>
        
        <!-- Max levels -->
        <div class="form-group">
            <label for="max-levels" class="control-label col-sm-4"><is:getProperty key="MENU_MAX_LEVELS" /></label>
            <div class="col-sm-8">
                <input id="max-levels" type="text" name="maxLevels" value="${maxLevels}" class="form-control" placeholder='<is:getProperty key="MENU_MAX_LEVELS" />' />
                <span class="help-block"><is:getProperty key="MESSAGE_MENU_MAX_LEVELS_HELP" args="${defaultMaxLevels}" /></span>
            </div>
        </div>
        
        <!-- JSTree display -->
        <div class="form-group">
            <label for="jstree-display" class="control-label col-sm-4"><is:getProperty key="MENU_JSTREE_DISPLAY" /></label>
            <div class="col-sm-8">
                <div class="checkbox">
                    <input id="jstree-display" type="checkbox" name="jstree" value="1" ${jstreeChecked} />
                </div>
                <span class="help-block"><is:getProperty key="MESSAGE_MENU_JSTREE_DISPLAY_HELP" /></span>
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
	