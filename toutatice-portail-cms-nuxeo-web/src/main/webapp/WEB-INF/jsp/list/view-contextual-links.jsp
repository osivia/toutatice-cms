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
            
            <!-- URL -->
            <c:set var="url"><ttc:documentLink document="${document}" displayContext="document" /></c:set>
            
            <!-- Vignette -->
            <c:set var="vignetteURL"><ttc:getImageURL document="${document}" property="ttc:vignette" /></c:set>
            
            <!-- Description -->
            <c:set var="description" value="${document.properties['dc:description']}" />
            
            
            
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="panel panel-default">
                    <div class="panel-body">
                        <div class="media">
                            <!-- Vignette -->
                            <c:if test="${not empty vignetteURL}">
                                <div class="media-left">
                                    <img src="${vignetteURL}" alt="" class="media-object">
                                </div>
                            </c:if>
                            
                            <div class="media-body">
                                <!-- Title -->
                                <p class="media-heading"><ttc:title document="${document}" /></p>
                    
                                <!-- Description -->
                                <c:if test="${not empty description}">
                                    <p>${description}</p>
                                </c:if>
                            </div>
                        </div>
                        
                        <!-- Continuation button -->
                        <div class="text-right">
                            <a href="${url}" class="btn btn-default btn-sm">
                                <i class="halflings halflings-zoom-in"></i>
                                <span><is:getProperty key="CONTINUATION" /></span>
                            </a>
                        </div>
                    </div>
                </div>
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
