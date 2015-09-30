<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<c:set var="namespace"><portlet:namespace /></c:set>

<portlet:actionURL var="actionURL">
    <portlet:param name="action" value="add"/>
</portlet:actionURL>


<!-- Datepicker language -->
<c:set var="datepickerLanguage" value="${pageContext.response.locale.language}" />
<c:if test="${'en' ne datepickerLanguage}">
    <script type="text/javascript" src="${contextPath}/components/jquery-ui-1.11.2.custom/i18n/datepicker-${datepickerLanguage}.js"></script>
</c:if>


<c:choose>
    <c:when test="${datesMonoValued eq '1'}">
        <c:set var="interval" value="${fn:split(dates[0], '%')}" />
        <c:set var="dateFrom" value="${interval[0]}" />
        <c:set var="dateTo" value="${interval[1]}" />
    
        <c:set var="glyphicon" value="halflings halflings-ok" />
        <c:set var="title"><op:translate key="SELECTOR_MONO_ADD" /></c:set>
    </c:when>
    
    <c:otherwise>
        <c:set var="glyphicon" value="halflings halflings-plus" />
        <c:set var="title"><op:translate key="SELECTOR_MULTI_ADD" /></c:set>
    </c:otherwise>
</c:choose>


<c:set var="placeholderBegin"><op:translate key="BEGIN" /></c:set>
<c:set var="placeholderEnd"><op:translate key="END" /></c:set>


<div class="dates-selector">
    <form action="${actionURL}" method="post" role="form">
        <!-- Label -->
        <c:if test="${not empty libelle}">
            <label>${libelle}</label>
        </c:if>
        
        <!-- Multi-valued items -->
        <c:if test="${datesMonoValued ne '1'}">
            <c:forEach var="item" items="${dates}" varStatus="status">
                <!-- Delete URL -->
                <portlet:actionURL var="deleteActionURL">
                    <portlet:param name="action" value="delete"/>
                    <portlet:param name="occ" value="${status.count}"/>
                </portlet:actionURL>
                
                <!-- Dates interval -->
                <c:set var="interval" value="${fn:split(item, '%')}" />
                
                <!-- Item -->
                <p class="text-right clearfix">
                    <span class="form-control-static pull-left">${interval[0]} - ${interval[1]}</span>
                    
                    <a href="${deleteActionURL}" class="btn btn-default">
                        <i class="halflings halflings-trash"></i>
                        <span class="sr-only"><op:translate key="DELETE" /></span>
                    </a>
                </p>
            </c:forEach>
        </c:if>
        
        <!-- Inputs -->
        <div class="form-group">
            <div class="media">
                <div class="media-left">
                    <input id="${namespace}-date-from" type="text" name="${namespace}-date-from" value="${dateFrom}" placeholder="${placeholderBegin}" class="form-control">
                </div>
            
                <div class="media-body">
                    <input id="${namespace}-date-to" type="text" name="${namespace}-date-to" value="${dateTo}" placeholder="${placeholderEnd}" class="form-control">
                </div>
                
                <div class="media-right">
                    <button type="submit" class="btn btn-default">
                        <i class="${glyphicon}"></i>
                        <span class="sr-only">${title}</span>
                    </button>
                </div>
            </div>
        </div>
    </form>
</div>
