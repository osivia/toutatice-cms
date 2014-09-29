<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="imageURL"><ttc:getImageURL property="annonce:image" /></c:set>
<c:set var="author" value="${document.properties['dc:creator']}" />
<c:set var="date" value="${document.properties['dc:created']}" />
<c:set var="resume" value="${document.properties['annonce:resume']}" />
<c:set var="content"><ttc:transform property="note:note" /></c:set>

<c:set var="contentClass" value="col-xs-12" />


<article class="annonce">
    <div class="row">
        <!-- Title -->
        <h3 class="hidden">${document.title}</h3>
        
        <!-- Image -->
        <c:if test="${not empty imageURL}">
            <c:set var="contentClass" value="col-xs-12 col-md-8" />
            
            <div class="col-xs-12 col-md-4">
                <p>
                    <img src="${imageURL}" alt="" class="img-thumbnail">
                </p>
            </div>
        </c:if>
        
        <div class="${contentClass}">
            <!-- Resume -->
            <div class="lead">${resume}</div>
            
            <!-- Content -->
            <div>${content}</div>
        </div>
    </div>
    
    <!-- Edition informations -->
    <p class="small test-right">
<%--         <span><is:getProperty key="CREATED_BY" /></span> --%>
<%--         <ttc:user name="${author}"/> --%>
        <span><is:getProperty key="DATE_ARTICLE_PREFIX" /></span>
        <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
    </p>
</article>
