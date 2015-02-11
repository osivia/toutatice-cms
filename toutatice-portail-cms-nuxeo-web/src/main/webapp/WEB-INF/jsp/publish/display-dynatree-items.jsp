<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<ul class="dynatree-container">
    <c:forEach var="child" items="${parent.children}">
        <!-- Selected item ? -->
        <c:remove var="selected" />
        <c:if test="${child.selected}">
            <c:set var="selected" value="expanded" />
        </c:if>
        
        <!-- Current item ? -->
        <c:remove var="current" />
        <c:set var="color" value="text-muted" />
        <c:if test="${child.current}">
            <c:set var="current" value="active" />
            <c:set var="color" value="text-primary" />
        </c:if>
        
        <!-- Browsable item ? -->
        <c:remove var="browsable" />
        <c:if test="${child.navItem.type.browsable}">
            <c:set var="browsable" value="folder" />
        </c:if>
        
        <!-- Glyph -->
        <c:set var="glyph" value="${fn:substringAfter(child.navItem.type.glyph, 'glyphicons-')}" />
        <c:if test="${fn:contains(glyph, 'folder')}">
            <c:remove var="glyph" />
        </c:if>
        
        <!-- Data -->
        <c:set var="data" value="id: '${child.id}', path: '${child.navItem.path}', isLazy: ${child.navItem.type.browsable && not child.selected}, acceptedTypes: '${fn:join(child.acceptedTypes, ',')}', addClass: '${current} ${glyph}'" />
        
    
        <li class="dynatree-temporary-node text-muted ${selected} ${current} ${browsable}" data="${data}">
            <a href="${child.url}" class="dynatree-temporary-title ${color}">${child.title}</a>
            
            <c:if test="${not empty child.children}">
                <c:set var="parent" value="${child}" scope="request" />
                <jsp:include page="display-dynatree-items.jsp" />
            </c:if>
        </li>
    </c:forEach>
</ul>
