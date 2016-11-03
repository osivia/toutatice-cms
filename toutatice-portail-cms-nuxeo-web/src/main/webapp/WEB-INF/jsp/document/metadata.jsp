<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="author" value="${document.properties['dc:creator']}" />

<c:set var="date" value="${document.properties['dc:modified']}" />
<c:if test="${empty date}">
    <c:set var="date" value="${document.properties['dc:created']}" />
</c:if>


<div class="metadata">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">
                <i class="halflings halflings-tags"></i>
                <span><op:translate key="METADATA" /></span>
            </h3>
        </div>
    
        <div class="panel-body">
            <dl>
                <!-- Author -->
                <dt><op:translate key="AUTHOR" /></dt>
                <dd>
                    <p><ttc:user name="${author}"/></p>
                </dd>
            
                <!-- Publication date -->
                <dt><op:translate key="DOCUMENT_PUBLICATION_DATE" /></dt>
                <dd>
                    <p><fmt:formatDate value="${date}" type="both" dateStyle="full" timeStyle="short" /></p>
                </dd>
            
                <!-- Remote publication spaces -->
                <jsp:include page="metadata-remote-sections.jsp" />
            </dl>
        </div>
    </div>
</div>
