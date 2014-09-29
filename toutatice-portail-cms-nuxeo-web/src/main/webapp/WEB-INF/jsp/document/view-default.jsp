<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page isELIgnored="false"%>


<c:set var="description" value="${document.properties['dc:description']}" />


<div class="default">
    <p>${description}</p>
</div>
