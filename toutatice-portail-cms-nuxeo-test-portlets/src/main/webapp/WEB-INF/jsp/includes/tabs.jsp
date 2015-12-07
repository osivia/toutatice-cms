<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<!-- Tabs -->
<ul class="nav nav-tabs">
    <c:forEach items="${tabs}" var="tab">
        <portlet:renderURL var="url">
            <portlet:param name="currentTabId" value="${tab.id}" />
        </portlet:renderURL>
    
    
        <li
            <c:if test="${currentTabId eq tab.id}">class="active"</c:if>
        >
            <a href="${url}">
                <op:translate key="${tab.key}" />
            </a>
        </li>
    </c:forEach>
</ul>
