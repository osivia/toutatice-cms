<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled no-ajax-link">
    <c:forEach var="document" items="${documents}">
        <li><ttc:title document="${document}" /></li>
    </c:forEach>
</ul>
