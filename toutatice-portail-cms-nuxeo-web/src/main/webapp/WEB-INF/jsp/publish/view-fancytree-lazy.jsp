<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="drop" var="dropActionURL" />
<portlet:resourceURL var="lazyLoadingURL" />


<div class="menu" data-dropurl="${dropActionURL}" data-lazyloadingurl="${lazyLoadingURL}">
    <c:if test="${empty displayItem.children}">
        <p class="text-muted text-center"><op:translate key="NO_ITEMS" /></p>
    </c:if>

    <div class="fancytree fancytree-lazy">
        <c:set var="parent" value="${displayItem}" scope="request" />
        <c:set var="lazy" value="true" scope="request" />
        <jsp:include page="display-fancytree-items.jsp" />
    </div>
</div>
