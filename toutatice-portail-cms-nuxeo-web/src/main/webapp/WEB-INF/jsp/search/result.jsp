<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="search" var="searchActionURL" />


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="searchTitle"><is:getProperty key="SEARCH_TITLE" /></c:set>
<c:set var="searchPlaceholder"><is:getProperty key="SEARCH_PLACEHOLDER" /></c:set>


<div class="row nuxeo-results-search">
    <!-- Search form -->
    <div class="col-lg-4">
        <form action="${searchActionURL}" method="post" class="form" role="search">
            <div class="form-group">
                <label class="sr-only" for="${namespace}-search-input"><is:getProperty key="SEARCH" /></label>
                <div class="input-group">
                    <input id="${namespace}-search-input" type="text" name="keywords" value="${keywords}" class="form-control" placeholder="${searchPlaceholder}">
                    <span class="input-group-btn">
                        <button type="submit" class="btn btn-default" title="${searchTitle}" data-toggle="tooltip" data-placement="bottom">
                            <span class="halflings halflings-search"></span>
                        </button>
                    </span>
                </div>
            </div>
        </form>
    </div>

    <!-- Search result -->
    <div class="col-lg-8">
        <div class="panel panel-default">
            <div class="panel-body">
                <!-- Indicator -->
                <p>
                    <span>${totalSize} </span>
                    <c:choose>
                        <c:when test="${totalSize > 1}">
                            <is:getProperty key="SEARCH_RESULTS_INDICATOR" />
                        </c:when>
                        
                        <c:otherwise>
                            <is:getProperty key="SEARCH_RESULT_INDICATOR" />
                        </c:otherwise>
                    </c:choose>
                </p>
            
                <!-- List -->
                <ul class="list-unstyled no-ajax-link">
                    <c:forEach var="result" items="${results}">
                        <li>
                            <img src="${result.icon}" alt="icon" class="pull-left">
                            <div>
                                <a href="${result.link.url}">
                                    <span>${result.title}</span>
                                </a>
                                
                                <c:if test="${result.link.downloadable}">
                                    <i class="halflings halflings-download-alt"></i>
                                </c:if>
                            
                                <c:if test="${result.link.external}">
                                    <i class="halflings halflings-new-window"></i>
                                </c:if>
                                
                                <p>${result.description}</p>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
                
                <!-- Pagination -->
                <c:if test="${maxPage > 0}">
                    <div class="text-center">
                        <ul class="pagination pagination-sm">
                            <c:forEach var="index" begin="${minPage}" end="${maxPage}">
                                <c:choose>
                                    <c:when test="${index == currentPage}">
                                        <li class="active">
                                            <a>
                                                <span>${index + 1}</span>
                                                <span class="sr-only">(current)</span>
                                            </a>
                                        </li>
                                    </c:when>
                                    
                                    <c:otherwise>
                                        <portlet:renderURL var="pageURL">
                                            <portlet:param name="keywords" value="${keywords}" />
                                            <portlet:param name="currentPage" value="${index}" />
                                        </portlet:renderURL>
                                        
                                        <li>
                                            <a href="${pageURL}">
                                                <span>${index + 1}</span>
                                            </a>
                                        </li>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>
