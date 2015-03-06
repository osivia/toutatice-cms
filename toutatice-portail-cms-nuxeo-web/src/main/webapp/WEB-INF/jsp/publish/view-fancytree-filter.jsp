<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<c:set var="filterLabel"><is:getProperty key="FILTER" /></c:set>
<c:set var="clearFilterLabel"><is:getProperty key="CLEAR_FILTER" /></c:set>


<div class="menu">
    <div class="fancytree fancytree-links">
        <div class="form-group">
            <div class="input-group input-group-sm">
                <span class="input-group-addon">
                    <i class="halflings halflings-filter"></i>
                </span>
                
                <input type="text" class="form-control" placeholder="${filterLabel}">
                
                <span class="input-group-btn">
                    <button type="button" class="btn btn-default" title="${clearFilterLabel}" data-toggle="tooltip" data-placement="bottom">
                        <i class="halflings halflings-erase"></i>
                        <span class="sr-only">${clearFilterLabel}</span>
                    </button>
                </span>
            </div>
        </div>
    

        <c:set var="parent" value="${displayItem}" scope="request" />
        <jsp:include page="display-fancytree-items.jsp" />
    </div>
</div>
