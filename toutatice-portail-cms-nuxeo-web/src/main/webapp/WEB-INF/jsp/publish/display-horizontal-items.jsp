<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<c:set var="childLevel" value="${level + 1}" />


<c:if test="${parent.selected || (level <= openLevels)}">
    <ul class="list-unstyled">
        <c:forEach var="child" items="${parent.children}">
            <li>
                <a href="${child.url}"
                    <c:if test="${child.selected}">class="active"</c:if>
                >            
                    <span>${child.title}</span>
                </a>
            
                <c:if test="${not empty child.children}">
                    <c:set var="parent" value="${child}" scope="request" />
                    <c:set var="level" value="${childLevel}" scope="request" />
                    <jsp:include page="display-horizontal-items.jsp" />
                </c:if>
            </li>
        </c:forEach>
    </ul>
</c:if>
