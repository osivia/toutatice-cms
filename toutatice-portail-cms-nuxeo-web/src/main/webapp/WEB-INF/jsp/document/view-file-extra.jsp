<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="url"><ttc:documentLink document="${document}" displayContext="download" /></c:set>
<c:set var="name" value="${document.properties['file:content']['name']}" />
<c:set var="size" value="${document.properties['file:content']['length']}" />
<c:set var="vignetteUrl"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />


<div class="panel panel-default">
    <div class="panel-body">
        <p>
            <!-- Title -->
            <i class="${document.type.glyph}"></i>
            <a href="${url}" target="_blank" class="no-ajax-link">${name}</a>
            
            <!-- Size -->
            <span>(<ttc:fileSize size="${size}" />)</span>
         </p>
     </div>
 </div>


<ttc:include page="view-default-extra.jsp" />
