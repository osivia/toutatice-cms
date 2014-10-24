<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<ul>
    <c:forEach var="document" items="${documents}">
        <!-- Document properties -->
        <ttc:documentLink document="${document}" var="link" />
        <c:remove var="target" />
        <c:if test="${link.external}">
            <c:set var="target" value="_blank" />
        </c:if>
    
    
        <li>
            <a href="${link.url}" target="${target}">
		        <span>${document.title}</span>
		    
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
    </c:forEach>
</ul>
