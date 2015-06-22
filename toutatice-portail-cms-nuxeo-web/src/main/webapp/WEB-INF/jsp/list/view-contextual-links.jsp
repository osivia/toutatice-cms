<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<div class="contextual-links">
    <div class="row">
        <c:forEach var="document" items="${documents}" varStatus="status">
            <!-- Document properties -->
            
            <!-- Link -->
            <ttc:documentLink document="${document}" var="link" />
            
            <!-- Vignette -->
            <c:set var="vignetteURL"><ttc:getImageURL document="${document}" property="ttc:vignette" /></c:set>
            
            <!-- Description -->
            <c:set var="description" value="${document.properties['dc:description']}" />
            
            
            
            <div class="col-sm-6 col-md-4 col-lg-3">
                <a href="${link.url}" class="thumbnail no-ajax-link"
                    <c:if test="${link.external}">target="_blank"</c:if>
                >
                    <span class="media">
                        <!-- Vignette -->
                        <c:if test="${not empty vignetteURL}">
                            <span class="media-left">
                                <img src="${vignetteURL}" alt="" class="media-object">
                            </span>
                        </c:if>
                        
                        <span class="caption media-body">
                            <!-- Title -->
                            <span class="media-heading">
                                <span>${document.title}</span>
                                
                                <c:if test="${link.external}">
                                    <small>
                                        <i class="glyphicons glyphicons-new-window-alt"></i>
                                    </small>
                                </c:if>
                            </span>
                
                            <!-- Description -->
                            <c:if test="${not empty description}">
                                <span class="text-muted">${description}</span>
                            </c:if>
                        </span>
                    </span>
                </a>
            </div>
            
            
            <!-- Responsive column reset -->
            <c:choose>
                <c:when test="${status.count % 4 == 0}">
                    <div class="clearfix visible-sm visible-lg"></div>
                </c:when>
                
                <c:when test="${status.count % 3 == 0}">
                    <div class="clearfix visible-md"></div>
                </c:when>
                
                <c:when test="${status.count % 2 == 0}">
                    <div class="clearfix visible-sm"></div>
                </c:when>
            </c:choose>
        </c:forEach>
    </div>
</div>
