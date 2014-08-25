<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page import="org.nuxeo.ecm.automation.client.model.Document"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
// Detailed view link
pageContext.setAttribute("detailedViewLink", nuxeoController.getLink(document, "detailedView"));

%>


<c:if test="${link.external}">
    <c:set var="target" value="_blank" />
</c:if>


<li class="clearfix">
    <!-- Vignette -->
    <c:if test="${not empty vignette}">
        <img src="${vignette}" alt="" class="vignette pull-left" />
    </c:if>

    <div>
        <!-- Title -->
        <p class="lead">
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

        <!-- Description -->
        <p>${description}</p>

        <!-- Detailed view -->
        <p class="pull-right">
            <a href="${detailedViewLink.url}"><is:getProperty key="LIST_DETAILED_VIEW" /></a>
        </p>
    </div>
</li>
