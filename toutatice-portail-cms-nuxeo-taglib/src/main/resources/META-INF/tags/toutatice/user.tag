<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" isELIgnored="false" %>
<%@ attribute name="name" description="User name." required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="linkable" description="Linkable indicator. Default = true." required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ttc" uri="http://www.toutatice.fr/jsp/taglib/toutatice" %>


<c:set var="displayName"><ttc:userDisplayName name="${name}" /></c:set>
<c:set var="avatarUrl"><ttc:userAvatarLink name="${name}" /></c:set>
<c:set var="profileUrl"><ttc:userProfileLink name="${name}"/></c:set>


<c:if test="${not empty name}">
    <span>
        <img src="${avatarUrl}" alt="" class="avatar">
        
        <c:choose>
            <c:when test="${(empty linkable or linkable) and not empty profileUrl}">
                <a href="${profileUrl}" class="no-ajax-link">${displayName}</a>
            </c:when>
            
            <c:otherwise>
                <span>${displayName}</span>
            </c:otherwise>
        </c:choose>
    </span>
</c:if>
