<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul class="list-group">
    <c:forEach var="document" items="${documents}">
        <!-- Document properties -->
        <ttc:documentLink document="${document}" var="link" />
        <c:remove var="target" />
        <c:if test="${link.external}">
            <c:set var="target" value="_blank" />
        </c:if>
        <c:set var="iconURL"><ttc:getDocumentIconURL document="${document}" /></c:set>
        <c:set var="typeName"><is:getProperty key="${fn:toUpperCase(document.type.name)}" /></c:set>
        <c:set var="author" value="${document.properties['dc:creator']}" />
        <c:set var="date" value="${document.properties['dc:modified']}" />
        <c:if test="${empty date}">
            <c:set var="date" value="${document.properties['dc:created']}" />
        </c:if>
        
    
        <li class="list-group-item list-group-item-linked">
            <a href="${link.url}" target="${target}" class="list-group-item">
                <!-- Title -->
		        <p class="list-group-item-heading">
		            <img src="${iconURL}" alt="${typeName}" />
		            
		            <span>${document.title}</span>
		        
		            <!-- Downloadable -->
		            <c:if test="${link.downloadable}">
		                <i class="glyphicons download_alt"></i>
		            </c:if>
		            
		            <!-- External -->
		            <c:if test="${link.external}">
		                <i class="glyphicons new_window_alt"></i>
		            </c:if>
		        </p>
		        
		        <!-- Last edition informations -->
		        <p class="list-group-item-text small">
                    <span><is:getProperty key="EDITED_BY" /></span>
                    <ttc:user name="${author}" linkable="false" />
                    <span><is:getProperty key="DATE_ARTICLE_PREFIX" /></span>
                    <span><fmt:formatDate value="${date}" type="date" dateStyle="long" /></span>
		        </p>
            </a>
        </li>
    </c:forEach>
</ul>
