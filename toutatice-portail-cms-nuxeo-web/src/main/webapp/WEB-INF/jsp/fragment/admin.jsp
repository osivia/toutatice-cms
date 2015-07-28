<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="select" var="selectURL" />
<portlet:actionURL name="save" var="saveAdminURL" />


<c:if test="${forceNavigation}">
    <c:set var="forceNavigationChecked" value="checked" />
</c:if>


<div class="container">
    <form action="${selectURL}" method="post" class="form-horizontal" role="form">
        <fieldset>
            <legend><is:getProperty key="FRAGMENT_TYPE_SELECT" /></legend>
            
	        <!-- Fragment type -->
	        <div class="form-group">
	            <label for="fragment-type" class="control-label col-sm-3"><is:getProperty key="FRAGMENT_TYPE" /></label>
	            <div class="col-sm-9">
	                <select id="fragment-type" name="fragmentTypeId" onchange="this.form.submit()" class="form-control">
	                    <option value=""></option>
	                
	                    <c:forEach var="type" items="${fragmentTypes}">
	                        <c:remove var="selected" />
	                        <c:if test="${type.key eq fragmentTypeId}">
	                            <c:set var="selected" value="selected" />
	                        </c:if>
	                    
	                        <option value="${type.key}" ${selected}>${type.label}</option>
	                    </c:forEach>
	                </select>
	            </div>
	        </div>
	        
	        <!-- Select button -->
            <div class="form-group hidden-script">
                <div class="col-sm-offset-3 col-sm-9">
                    <button type="submit" class="btn btn-default btn-primary"><is:getProperty key="SELECT" /></button>
                </div>
            </div>
        </fieldset>
    </form>
    
    <c:if test="${not empty fragmentType.key}">
        <form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
            <input type="hidden" name="fragmentTypeId" value="${fragmentType.key}">
        
            <fieldset>
                <legend>${fragmentType.label}</legend>
            
                <c:choose>
                    <c:when test="${empty fragmentType.module.adminJSPName}">
                        <p><is:getProperty key="FRAGMENT_MESSAGE_NO_CONFIGURATION" /></p>
                    </c:when>
                    
                    <c:otherwise>
                        <ttc:custom-include page="admin-${fragmentType.module.adminJSPName}.jsp" />
                    </c:otherwise>
                </c:choose>
            </fieldset>
        
            <!-- Buttons -->
	        <div class="form-group">
	            <div class="col-sm-offset-3 col-sm-9">
	                <button type="submit" class="btn btn-default btn-primary"><is:getProperty key="SAVE" /></button>
	                <button type="button" class="btn btn-default" onclick="closeFancybox()"><is:getProperty key="CANCEL" /></button>
	            </div>
	        </div>
        </form>
    </c:if>
</div>
