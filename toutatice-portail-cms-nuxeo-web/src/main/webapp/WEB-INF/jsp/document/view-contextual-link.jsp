<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<!-- Link -->
<ttc:documentLink document="${document}" var="link" />

<!-- Target -->
<c:remove var="target" />
<c:if test="${link.external}">
    <c:set var="target" value="_blank" />
</c:if>

<!-- Contextual link value -->
<c:set var="clink" value="${document.properties['clink:link']}" />

<!-- Description -->
<c:set var="description" value="${document.properties['dc:description']}" />


<ttc:addMenubarItem id="LINK" labelKey="OPEN_LINK" url="${link.url}" target="${target}" glyphicon="halflings halflings-share-alt" />


<div class="contextual-link">
    <p>${description}</p>
    
    <p>
        <a href="${clink}" target="${target}">
            <span>${clink}</span>
        </a>
        
        <c:if test="${link.external}">
            <small>
                <i class="glyphicons glyphicons-new-window-alt"></i>
            </small>
        </c:if>
    </p>
</div>
