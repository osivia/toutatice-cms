<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="search" var="searchActionURL" />


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="searchTitle"><op:translate key="SEARCH_TITLE" /></c:set>
<c:set var="searchPlaceholder"><op:translate key="SEARCH_PLACEHOLDER" /></c:set>


<div class="nuxeo-results-search">
    <!-- Search form -->
    <form action="${searchActionURL}" method="post" class="form" role="search">
        <div class="form-group">
            <label class="sr-only" for="${namespace}-search-input"><op:translate key="SEARCH" /></label>
            <div class="input-group">
                <input id="${namespace}-search-input" type="text" name="keywords" value="${keywords}" class="form-control" placeholder="${searchPlaceholder}">
                <span class="input-group-btn">
                    <button type="submit" class="btn btn-secondary" title="${searchTitle}" data-toggle="tooltip" data-placement="bottom">
                        <span class="halflings halflings-search"></span>
                    </button>
                </span>
            </div>
        </div>
    </form>

    <!-- Search result -->
    <div class="panel panel-default">
        <div class="panel-body">
            <!-- Indicator -->
            <p class="text-muted">
                <span>${totalSize} </span>
                <c:choose>
                    <c:when test="${totalSize > 1}">
                        <op:translate key="SEARCH_RESULTS_INDICATOR" />
                    </c:when>
                    
                    <c:otherwise>
                        <op:translate key="SEARCH_RESULT_INDICATOR" />
                    </c:otherwise>
                </c:choose>
            </p>
            
            <!-- List -->
            <ul class="list-unstyled no-ajax-link">
                <c:forEach var="document" items="${documents}">
                    <!-- Document properties -->
    
                    <!-- Link -->
                    <ttc:documentLink document="${document}" var="link" />
            
                    <!-- Vignette -->
                    <c:set var="vignetteURL"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
            
                    <!-- Description -->
                    <c:set var="description" value="${document.properties['dc:description']}" />
                
                
                
                    <li class="media">
                        <c:if test="${not empty vignetteURL}">
                            <div class="media-left">
                                <img src="${vignetteURL}" alt="">
                            </div>
                        </c:if>
                        
                        <div class="media-body media-middle">
                            <!-- Title -->
                            <div>
                                <a href="${link.url}"
                                    <c:if test="${link.external}">target="_blank"</c:if>
                                >
                                    <i class="${document.type.icon}"></i>
                                    <span>${document.title}</span>
                                </a>
                                
                                <!-- Downloadable -->
                                <c:if test="${link.downloadable}">
                                    <i class="halflings halflings-download-alt"></i>
                                </c:if>
                                
                                <!-- External -->
                                <c:if test="${link.external}">
                                    <i class="halflings halflings-new-window"></i>
                                </c:if>
                            </div>
                            
                            <!-- Description -->
                            <div class="text-pre-wrap">${description}</div>
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
                                        <portlet:param name="results" value="true"/>
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

