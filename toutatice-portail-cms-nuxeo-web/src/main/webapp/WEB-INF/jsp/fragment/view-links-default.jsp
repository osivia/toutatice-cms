<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page isELIgnored="false" %>


<ul class="list-inline">
    <c:forEach var="link" items="${links}">
        <c:remove var="target" />
        <c:if test="${link.external}">
            <c:set var="target" value="_blank" />
        </c:if>
    
    
        <li>
            <a href="${link.url}" target="${target}">
                <c:if test="${not empty link.glyphicon}">
                    <i class="glyphicons ${link.glyphicon}"></i>
                </c:if>
                
                <span>${link.title}</span>
            </a>
        </li>
    </c:forEach>
</ul>
