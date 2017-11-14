<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled">
    <c:forEach var="document" items="${documents}">
        <li>
            <span><ttc:title document="${document}" /></span>
        </li>
    </c:forEach>
    
    
    <c:if test="${empty documents}">
        <li class="text-center">
            <span class="text-muted"><op:translate key="LIST_NO_ITEMS" /></span>
        </li>
    </c:if>
</ul>
