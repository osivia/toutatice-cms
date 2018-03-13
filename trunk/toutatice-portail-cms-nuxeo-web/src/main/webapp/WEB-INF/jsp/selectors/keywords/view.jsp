<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<c:set var="namespace"><portlet:namespace /></c:set>

<portlet:actionURL var="actionURL" />


<c:choose>
    <c:when test="${selectorType eq '0'}">
        <c:set var="textValue" value="${keyword}" />
        <c:set var="name" value="add" />
        <c:set var="glyphicon" value="halflings halflings-plus" />
        <c:set var="title"><op:translate key="SELECTOR_MULTI_ADD" /></c:set>
        <c:set var="placeholder"><op:translate key="SELECTOR_KEYWORD_PLACEHOLDER" /></c:set>
    </c:when>
    
    <c:otherwise>
        <c:set var="textValue" value="${fn:join(keywords, ' ')}" />
        <c:set var="name" value="monoAdd" />
        <c:set var="glyphicon" value="halflings halflings-ok" />
        <c:set var="title"><op:translate key="SELECTOR_MONO_ADD" /></c:set>
        <c:set var="placeholder"><op:translate key="SELECTOR_KEYWORDS_PLACEHOLDER" /></c:set>
    </c:otherwise>
</c:choose>


<div class="keywords-selector ${selectorType eq '2' ? 'auto-submit' : ''}">
    <form action="${actionURL}" method="post" role="form">
        <!-- Label -->
        <c:if test="${not empty selectorLabel}">
            <label>${selectorLabel}</label>
        </c:if>
            
        <!-- Multi-valued items -->
        <c:if test="${selectorType eq '0'}">
            <c:forEach var="item" items="${keywords}" varStatus="status">
                <!-- Delete URL -->
                <portlet:actionURL var="deleteActionURL">
                    <portlet:param name="action" value="delete"/>
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
        <div class="form-group">
            <div class="${selectorType eq '2' ? '' : 'input-group'}">
                <input type="text" name="keyword" value="${textValue}" class="form-control" placeholder="${placeholder}">
                <span class="${selectorType eq '2' ? 'hidden' : 'input-group-btn'}">
                    <button type="submit" name="${name}" class="btn btn-default">
                        <i class="${glyphicon}"></i>
                        <span class="sr-only">${title}</span>
                    </button>
                </span>
            </div>
        </div>
    </form>
</div>
