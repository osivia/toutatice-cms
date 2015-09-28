<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="permaLinkURL" value="${requestScope['permaLinkURL']}" />
<c:set var="rssLinkURL" value="${requestScope['rssLinkURL']}" />


<c:if test="${not empty permaLinkURL}">
    <ttc:addMenubarItem id="PERMALINK" labelKey="PERMALINK" order="0" url="${permaLinkURL}" glyphicon="halflings halflings-link" ajax="false" />
</c:if>

<c:if test="${not empty rssLinkURL}">
    <ttc:addMenubarItem id="RSS" labelKey="RSS" order="2" url="${rssLinkURL}" glyphicon="social social-rss" ajax="false" />
</c:if>
