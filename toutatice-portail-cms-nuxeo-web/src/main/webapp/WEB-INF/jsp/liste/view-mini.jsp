<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page import="org.nuxeo.ecm.automation.client.model.Document"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


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

%>


<c:if test="${link.external}">
    <c:set var="target" value="_blank" />
</c:if>


<li class="list-group-item">
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
</li>
