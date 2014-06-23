<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<portlet:actionURL var="selectorActionURL" />


<c:if test="${not empty libelle}">
    <span class="selector-libelle">${libelle}</span>
</c:if>

<div class="nuxeo-keywords-selector">
    <form method="post" action="${selectorActionURL}">
        <div class="table">
            <c:choose>
                <c:when test='${keywordMonoValued == "1"}'>
                    <!-- Mono-valued -->
                    <c:set var="textValue" value="${fn:join(keywords, ' ')}" />
                    <c:set var="name" value="monoAdd" />
                    <c:set var="glyphicon" value="halflings ok" />
                    <c:set var="title"><is:getProperty key="SELECTOR_MONO_ADD" /></c:set>
                    <c:set var="placeholder"><is:getProperty key="SELECTOR_KEYWORDS_PLACEHOLDER" /></c:set>
                </c:when>
                
                <c:otherwise>
                    <!-- Multi-valued -->
                    <c:set var="textValue" value="${keyword}" />
                    <c:set var="name" value="add" />
                    <c:set var="glyphicon" value="halflings plus" />
                    <c:set var="title"><is:getProperty key="SELECTOR_MULTI_ADD" /></c:set>
                    <c:set var="placeholder"><is:getProperty key="SELECTOR_KEYWORD_PLACEHOLDER" /></c:set>
                        
                    <c:forEach var="item" items="${keywords}" varStatus="status">
                        <portlet:actionURL var="deleteActionURL">
                            <portlet:param name="action" value="delete"/>
                            <portlet:param name="occ" value="${status.count}"/>
                        </portlet:actionURL>
                    
                        <div class="form-group">
                            <div class="input-group">
                                <input type="text" value="${item}" class="form-control" disabled="disabled">
                                <span class="input-group-btn">
                                    <a href="${deleteActionURL}" class="btn btn-default" title='<is:getProperty key="SELECTOR_DELETE" />' data-toggle="tooltip" data-placement="bottom">
                                        <span class="glyphicons halflings remove"></span>
                                    </a>
                                </span>
                            </div>
                        </div>
                    </c:forEach>                
                </c:otherwise>
            </c:choose>
            
            
            <div class="form-group">
                <label class="sr-only" for="${namespace}-selector-input">Select</label>
                <div class="input-group">
                    <input id="${namespace}-selector-input" type="text" name="keyword" value="${textValue}" class="form-control" placeholder="${placeholder}">
                    <span class="input-group-btn">
                        <button type="submit" name="${name}" class="btn btn-default" title="${title}" data-toggle="tooltip" data-placement="bottom">
                            <span class="glyphicons ${glyphicon}"></span>
                        </button>
                        
                        <c:if test='${keywordMonoValued == "1"}'>
                            <button type="submit" name="clear" class="btn btn-default" title='<is:getProperty key="SELECTOR_CLEAR" />' data-toggle="tooltip" data-placement="bottom">
                                <span class="glyphicons delete"></span>
                            </button>
                        </c:if>
                    </span>
                </div>
            </div>   
        </div>
    </form>
</div>
