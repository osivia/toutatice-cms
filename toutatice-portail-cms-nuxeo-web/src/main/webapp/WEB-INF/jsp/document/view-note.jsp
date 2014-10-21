<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="author" value="${document.properties['dc:creator']}" />
<c:set var="date" value="${document.properties['dc:modified']}" />
<c:set var="content"><ttc:transform document="${document}" property="note:note" /></c:set>


<article class="note">
    <!-- Title -->
    <h3 class="hidden">${document.title}</h3>
    
    <!-- Content -->
    <div>${content}</div>
    
    <!-- Edition informations -->
    <p class="small test-right">
        <span><is:getProperty key="EDITED_BY" /></span>
        <ttc:user name="${author}"/>
        <span><is:getProperty key="DATE_ARTICLE_PREFIX" /></span>
        <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
    </p>
</article>
   