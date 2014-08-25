<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@ page import="org.nuxeo.ecm.automation.client.model.Document"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@ page isELIgnored="false" %>


<%
// Nuxeo controller
NuxeoController nuxeoController = (NuxeoController) request.getAttribute("ctx");
// Nuxeo document
Document document = (Document) request.getAttribute("doc");

// Title
pageContext.setAttribute("title", document.getTitle());
// Link
pageContext.setAttribute("link", nuxeoController.getLink(document));
// Icon
pageContext.setAttribute("icon", Formater.formatNuxeoIcon(document));
// Type
pageContext.setAttribute("type", document.getType());
// Description
pageContext.setAttribute("description", document.getString("dc:description"));
// Username
String username = document.getString("dc:creator");
if (nuxeoController.getPerson(username) != null) {
 pageContext.setAttribute("username", nuxeoController.getPerson(username).getDisplayName());
} else {
 pageContext.setAttribute("username", username);
}
// Avatar
pageContext.setAttribute("avatar", nuxeoController.getUserAvatar(username));
// Date
if (document.getDate("dc:modified") == null) {
    pageContext.setAttribute("date", document.getDate("dc:created"));
} else {
    pageContext.setAttribute("date", document.getDate("dc:modified"));
}

%>


<c:if test="${link.external}">
    <c:set var="target" value="_blank" />
</c:if>


<li>
    <p>
        <img src="${pageContext.request.contextPath}${icon}" alt="${type}" class="icon" />
    
        <a href="${link.url}" target="${target}">
            <span>${title}</span>
            
            <!-- Downloadable -->
            <c:if test="${link.downloadable}">
                <i class="glyphicons download_alt"></i>
            </c:if>
            
            <!-- External -->
            <c:if test="${link.external}">
                <i class="glyphicons new_window_alt"></i>
            </c:if>
        </a>
    </p>

    <p>${description}</p>

    <p class="small">
        <img src="${avatar.url}" alt="" class="avatar avatar-small" />
        <span>${username}</span>
        <span> - </span>
        <span><fmt:formatDate value="${date}" type="both" dateStyle="medium" timeStyle="short" /></span>
    </p>
</li>
