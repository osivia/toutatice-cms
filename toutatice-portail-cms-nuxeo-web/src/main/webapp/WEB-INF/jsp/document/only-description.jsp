<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="documentURL"><ttc:getDocumentURL /></c:set>
<c:set var="vignetteURL"><ttc:getImageURL property="ttc:vignette" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />


<div class="only-description">
    <!-- Vignette -->
    <c:if test="${not empty vignetteURL}">
        <img src="${vignetteURL}" alt="" class="img-thumbnail pull-left">
    </c:if>
    
    <!-- Description -->
    <c:if test="${not empty description}">
        <p class="lead">${description}</p>
    </c:if>
    
    <!-- Continuation button -->
    <div class="btn-toolbar pull-right">
        <a href="${documentURL}" class="btn btn-default">
            <i class="glyphicons play_button"></i>
            <span><is:getProperty key="CONTINUATION" />...</span>
        </a>
    </div>
</div>
