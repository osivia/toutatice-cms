<%@ page isELIgnored="false" %>


<!-- Picture -->
<c:if test="${not empty imageSource}">
    <p>
        <img src="${imageSource}" alt="" class="img-responsive center-block" />
    </p>
</c:if>

<!-- Content -->
<c:if test="${not empty content}">
    <div>${content}</div>
</c:if>
