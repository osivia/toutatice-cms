<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<div class="picturebook">
    <div class="row">
        <c:forEach var="document" items="${documents}" varStatus="status">
            <!-- Document properties -->
            
            <!-- URL -->
            <c:set var="url"><ttc:documentLink document="${document}" /></c:set>
            
            <!-- Picture -->
            <c:set var="pictureURL"><ttc:documentLink document="${document}" picture="true" /></c:set>
            
            <!-- Thumbnail -->
            <c:set var="thumbnailURL"><ttc:documentLink document="${document}" picture="true" displayContext="Medium" /></c:set>
            
            <!-- Description -->
            <c:set var="description" value="${document.properties['dc:description']}" />
            
            <!-- File size -->
            <c:set var="fileSize" value="${document.properties['common:size']}" />
            
            
        
            <div class="col-xs-6 col-sm-4 col-md-3 col-lg-2">
                <div class="picture">
                    <a href="${pictureURL}" class="thumbnail fancybox" rel="gallery" data-title="${document.title}">
                        <img src="${thumbnailURL}" alt="${description}">
                    </a>
                </div>

                <p class="text-center">
                    <a href="${url}">${document.title}</a>
                    
                    <c:if test="${not empty fileSize}">
                        <span>(<ttc:formatFileSize size="${fileSize}" />)</span>
                    </c:if>
                </p>
            </div>
            
            
            <!-- Responsive column reset -->
            <c:choose>
                <c:when test="${status.count % 6 == 0}">
                    <div class="clearfix visible-xs visible-sm visible-lg"></div>
                </c:when>
                
                <c:when test="${status.count % 4 == 0}">
                    <div class="clearfix visible-xs visible-md"></div>
                </c:when>
                
                <c:when test="${status.count % 3 == 0}">
                    <div class="clearfix visible-sm"></div>
                </c:when>
                
                <c:when test="${status.count % 2 == 0}">
                    <div class="clearfix visible-xs"></div>
                </c:when>
            </c:choose>
        </c:forEach>
    </div>
</div>
