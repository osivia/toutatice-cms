<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page import="org.nuxeo.ecm.automation.client.model.Document"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>


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
// Description
pageContext.setAttribute("description", document.getString("dc:description"));
// Vignette
if ((document.getProperties().getMap("ttc:vignette") != null) && (document.getProperties().getMap("ttc:vignette").getString("data") != null)) {
    pageContext.setAttribute("vignette", nuxeoController.createFileLink(document, "ttc:vignette"));
}
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


<li class="list-group-item clearfix">
    <!-- Vignette -->
    <c:if test="${not empty vignette}">
        <img src="${vignette}" alt="" class="vignette pull-left" />
    </c:if>

    <div>
        <!-- Title -->
        <h4>
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
        </h4>

        <!-- Description -->
        <p>${description}</p>
        
        <!-- Last edition informations -->
        <p class="small">
            <span><is:getProperty key="EDITED_BY" /></span>
            <img src="${avatar.url}" alt="" class="avatar avatar-small" />
            <span>${username}</span>
            <span><is:getProperty key="DATE_ARTICLE_PREFIX" /></span>
            <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
        </p>
    </div>
</li>
