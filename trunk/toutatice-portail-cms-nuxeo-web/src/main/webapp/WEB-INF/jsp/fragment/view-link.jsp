<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page isELIgnored="false" %>


<c:if test="${link.external}">
    <c:set var="target" value="_blank" />
</c:if>


<div>
    <a href="${link.url}" target="${target}" class="no-ajax-link">
        <span class="${cssClasses}">${name}</span>
    </a>
</div>
