<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="vignetteURL"><ttc:getImageURL document="${document}" property="ttc:vignette" /></c:set>
<c:set var="author" value="${document.properties['dc:creator']}" />

<c:set var="date" value="${document.properties['dc:modified']}" />
<c:if test="${empty date}">
    <c:set var="date" value="${document.properties['dc:created']}" />
</c:if>


<div class="metadata">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">
                <i class="glyphicons tags"></i>
                <span><is:getProperty key="METADATA" /></span>
            </h3>
        </div>
    
        <div class="panel-body">
            <!-- Vignette -->
            <c:if test="${not empty vignetteURL}">
                <img src="${vignetteURL}" alt="" class="img-thumbnail pull-left">
            </c:if>
            
            <!-- Author -->
            <p>
                <strong><is:getProperty key="AUTHOR" /></strong>
                <span> : </span>
                <ttc:user name="${author}"/>
            </p>
            
            <!-- Publication date -->
            <p>
                <strong><is:getProperty key="DOCUMENT_PUBLICATION_DATE" /></strong>
                <span> : </span>
                <span><fmt:formatDate value="${date}" type="both" dateStyle="full" timeStyle="short" /></span>
            </p>
        </div>
    </div>
</div>

<hr>
