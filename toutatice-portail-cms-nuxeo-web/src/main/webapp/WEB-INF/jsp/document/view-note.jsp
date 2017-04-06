<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>

<c:set var="content"><ttc:transform document="${document}" property="note:note" /></c:set>


<article class="note">
    <!-- Title -->
    <h3 class="hidden">${document.title}</h3>
    
    <!-- Content -->
    <div class="clearfix">${content}</div>
</article>
