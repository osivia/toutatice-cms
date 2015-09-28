<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="documentURL"><ttc:documentLink document="${document}" /></c:set>
<c:set var="vignetteURL"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />


<div class="only-description">
    <div class="media">
        <!-- Vignette -->
        <c:if test="${not empty vignetteURL}">
            <div class="media-left">
                <img src="${vignetteURL}" alt="" class="media-object">
            </div>
        </c:if>
        
        <!-- Description -->
        <c:if test="${not empty description}">
            <div class="media-body">
                <p class="lead">${description}</p>
            </div>
        </c:if>
    </div>
    
    <!-- Continuation button -->
    <div class="text-right">
        <a href="${documentURL}" class="btn btn-default btn-sm">
            <i class="halflings halflings-zoom-in"></i>
            <span><is:getProperty key="CONTINUATION" />...</span>
        </a>
    </div>
</div>
