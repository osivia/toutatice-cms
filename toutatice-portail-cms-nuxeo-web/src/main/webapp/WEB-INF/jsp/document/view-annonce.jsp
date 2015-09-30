<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="imageURL"><ttc:pictureLink document="${document}" property="annonce:image" /></c:set>
<c:set var="author" value="${document.properties['dc:creator']}" />
<c:set var="date" value="${document.properties['dc:created']}" />
<c:set var="resume" value="${document.properties['annonce:resume']}" />
<c:set var="content"><ttc:transform document="${document}" property="note:note" /></c:set>

<c:set var="contentClass" value="col-xs-12" />


<article class="annonce">
    <div class="row">
        <!-- Title -->
        <h3 class="hidden">${document.title}</h3>
        
        <!-- Image -->
        <c:if test="${not empty imageURL}">
            <c:set var="contentClass" value="col-xs-12 col-md-8" />
            
            <div class="col-xs-12 col-md-4">
                <img src="${imageURL}" alt="" class="img-responsive">
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
<%--         <span><op:translate key="CREATED_BY" /></span> --%>
<%--         <ttc:user name="${author}"/> --%>
        <span><op:translate key="DATE_ARTICLE_PREFIX" /></span>
        <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
    </p>
</article>
