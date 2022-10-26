<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page isELIgnored="false" %>


<div>
    <c:choose>
        <c:when test="${empty link}">
            <img src="${imageSource}" alt="" class="img-fluid">
        </c:when>
        
        <c:otherwise>
            <a href="${link.url}" class="thumbnail no-ajax-link">
                <img src="${imageSource}" alt="">
            </a>
        </c:otherwise>
    </c:choose>
</div>
