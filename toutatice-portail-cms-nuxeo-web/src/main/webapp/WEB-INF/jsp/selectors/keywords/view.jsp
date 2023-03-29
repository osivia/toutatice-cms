<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<c:set var="namespace"><portlet:namespace /></c:set>

<portlet:actionURL var="actionURL" />


<%--@elvariable id="keyword" type="java.lang.String"--%>
<%--@elvariable id="keywords" type="java.util.List"--%>
<%--@elvariable id="selectorDisplayResetButton" type="java.lang.Boolean"--%>
<%--@elvariable id="selectorHtmlIdentifier" type="java.lang.String"--%>
<%--@elvariable id="selectorLabel" type="java.lang.String"--%>
<%--@elvariable id="selectorPlaceholder" type="java.lang.String"--%>
<%--@elvariable id="selectorType" type="java.lang.String"--%>
<c:choose>
    <c:when test="${selectorType eq '0'}">
        <c:set var="textValue" value="${keyword}" />
        <c:set var="name" value="add" />
        <c:set var="icon" value="glyphicons glyphicons-basic-plus" />
        <c:set var="title"><op:translate key="SELECTOR_MULTI_ADD" /></c:set>
    </c:when>
    
    <c:otherwise>
        <c:set var="textValue" value="${fn:join(keywords, ' ')}" />
        <c:set var="name" value="monoAdd" />
        <c:set var="icon" value="glyphicons glyphicons-basic-check" />
        <c:set var="title"><op:translate key="SELECTOR_MONO_ADD" /></c:set>
    </c:otherwise>
</c:choose>


<div id="${empty selectorHtmlIdentifier ? '' : selectorHtmlIdentifier}" class="keywords-selector ${selectorType eq '2' ? 'auto-submit' : ''}">
    <form action="${actionURL}" method="post" role="form">
        <!-- Label -->
        <c:if test="${not empty selectorLabel}">
            <label class="form-label">${selectorLabel}</label>
        </c:if>
            
        <!-- Multi-valued items -->
        <c:if test="${selectorType eq '0'}">
            <c:forEach var="item" items="${keywords}" varStatus="status">
                <!-- Delete URL -->
                <portlet:actionURL name="delete" var="deleteActionURL">
                    <portlet:param name="occ" value="${status.count}"/>
                </portlet:actionURL>
                
                <!-- Item -->
                <p class="text-right clearfix">
                    <span class="form-control-static pull-left">${item}</span>
                    
                    <a href="${deleteActionURL}" class="btn btn-default">
                        <i class="halflings halflings-trash"></i>
                        <span class="sr-only"><op:translate key="DELETE" /></span>
                    </a>
                </p>
            </c:forEach>
        </c:if>
            
        <!-- Input -->
        <div>
            <div class="${selectorDisplayResetButton or selectorType ne '2' ? 'input-group' : ''}">
                <input type="text" name="keyword" value="${textValue}" class="form-control" placeholder="${selectorPlaceholder}">
                <c:if test="${selectorDisplayResetButton}">
                    <c:set var="resetTitle"><op:translate key="SELECTOR_KEYWORDS_RESET"/></c:set>
                    <button type="button" title="${resetTitle}" class="btn btn-outline-secondary" data-action="reset">
                        <i class="glyphicons glyphicons-basic-times"></i>
                        <span class="visually-hidden">${resetTitle}</span>
                    </button>
                </c:if>
                <c:if test="${selectorType ne '2'}">
                    <button type="submit" name="${name}" class="btn btn-primary">
                        <i class="${icon}"></i>
                        <span class="visually-hidden">${title}</span>
                    </button>
                </c:if>
            </div>
            <c:if test="${selectorType eq '2'}">
                <button type="submit" name="${name}" class="d-none"></button>
            </c:if>
        </div>
    </form>
</div>
