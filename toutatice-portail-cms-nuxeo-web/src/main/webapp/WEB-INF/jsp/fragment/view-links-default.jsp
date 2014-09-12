<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page isELIgnored="false" %>


<ul class="list-inline">
    <c:forEach var="link" items="${links}">
        <li>
            <a href="${link.href}">
                <c:if test="${not empty link.icon}">
                    <i class="glyphicons ${link.icon}"></i>
                </c:if>
                
                <span>${link.title}</span>
            </a>
        </li>
    </c:forEach>
</ul>
