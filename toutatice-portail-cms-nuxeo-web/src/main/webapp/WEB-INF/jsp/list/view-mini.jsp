<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false" %>


<ul class="list-unstyled">
    <c:forEach var="document" items="${documents}">
        <li><ttc:title document="${document}" /></li>
    </c:forEach>
</ul>
