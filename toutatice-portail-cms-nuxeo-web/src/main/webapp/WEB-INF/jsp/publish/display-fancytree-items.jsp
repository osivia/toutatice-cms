<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<ul
    <c:if test="${not parent.selected}">class="hidden-script"</c:if>
>
    <c:forEach var="child" items="${parent.children}">
        <li data-retain="${child.selected}" data-acceptedtypes="${fn:join(child.acceptedTypes, ',')}"
            <c:choose>
                <c:when test="${child.current}">class="text-primary"</c:when>
                <c:otherwise>class="text-muted"</c:otherwise>
            </c:choose>
            
            <c:if test="${child.selected}">data-expanded="true"</c:if>
            <c:if test="${child.navItem.type.browsable}">data-folder="true"</c:if>
            <c:if test="${not empty child.navItem.type.glyph and not fn:contains(child.navItem.type.glyph, 'folder')}">data-iconclass="${child.navItem.type.glyph}"</c:if>
            
            <c:if test="${lazy}">data-lazy="${child.navItem.type.browsable && not child.selected}" data-id="${child.id}" data-path="${child.navItem.path}" data-current="${child.current}"</c:if>
        >
            <!-- Node -->
            <a href="${child.url}">${child.title}</a>
            
            <!-- Children -->
            <c:if test="${not empty child.children}">
                <c:set var="parent" value="${child}" scope="request" />
                <jsp:include page="display-fancytree-items.jsp" />
            </c:if>
        </li>
    </c:forEach>    
</ul>
