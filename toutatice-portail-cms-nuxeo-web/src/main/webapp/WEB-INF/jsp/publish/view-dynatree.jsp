<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="drop" var="dropActionURL" />


<div class="nuxeo-publish-navigation" data-dropurl="${dropActionURL}">
    <div class="dynatree">
        <c:set var="parent" value="${displayItem}" scope="request" />
        <jsp:include page="display-dynatree-items.jsp" />
    </div>
</div>
