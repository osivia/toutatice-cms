<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<!-- Link -->
<ttc:documentLink document="${document}" var="link" displayContext="contextualLink"/>

<!-- Target -->
<c:set var="target" value="${link.external ? '_blank' : ''}" />

<!-- Contextual link value -->
<c:set var="clink" value="${document.properties['clink:link']}" />


<ttc:addMenubarItem id="LINK" labelKey="OPEN_LINK" url="${link.url}" target="${target}" glyphicon="halflings halflings-new-window" />


<div class="contextual-link">
    <p>
        <a href="${link.url}" target="${target}">
            <span>${clink}</span>
        </a>
        
        <c:if test="${link.external}">
            <small>
                <i class="glyphicons glyphicons-new-window-alt"></i>
            </small>
        </c:if>
    </p>
</div>
