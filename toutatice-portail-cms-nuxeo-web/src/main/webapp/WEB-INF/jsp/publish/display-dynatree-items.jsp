<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<ul>
    <c:forEach var="child" items="${parent.children}">
        <!-- Selected item ? -->
        <c:remove var="selected" />
        <c:if test="${child.selected}">
            <c:set var="selected" value="expanded" />
        </c:if>
        
        <!-- Current item ? -->
        <c:remove var="current" />
        <c:if test="${child.current}">
            <c:set var="current" value="active" />
        </c:if>
        
        <!-- Navigable item ? -->
        <c:remove var="navigable" />
        <c:if test="${child.navigable}">
            <c:set var="navigable" value="folder" />
        </c:if>
        
    
        <li class="${selected} ${current} ${navigable}" data="id: '${child.id}'">
            <a href="${child.url}">${child.title}</a>
            
            <c:if test="${not empty child.children}">
                <c:set var="parent" value="${child}" scope="request" />
                <jsp:include page="display-dynatree-items.jsp" />
            </c:if>
        </li>
    </c:forEach>
</ul>
