<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ taglib uri="toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects />

<c:set var="description" value="${document.properties['dc:description']}" />


<div>
    <a href="<portlet:actionURL />" class="btn btn-warning">
        <i class="glyphicons bug"></i>
        <span>Action de test</span>
    </a>
</div>

<div class="file-browser table-responsive no-ajax-link">
    <table class="table table-hover">
        <!-- Description -->
        <c:if test="${not empty description}">
            <caption>${description}</caption>
        </c:if>
    
        <!-- Table head -->
        <thead>
            <tr>
                <th></th>
                <th><is:getProperty key="FILE_BROWSER_NAME" /></th>
                <th><is:getProperty key="FILE_BROWSER_DATE" /></th>
                <th><is:getProperty key="FILE_BROWSER_LAST_CONTRIBUTOR" /></th>
            </tr>
        </thead>
        
        <!-- Table body -->
        <tbody class="no-ajax-link">
            <c:forEach var="document" items="${documents}">
                <!-- Document properties -->
                <ttc:documentLink document="${document}" var="link" />
                <c:remove var="target" />
                <c:if test="${link.external}">
                    <c:set var="target" value="_blank" />
                </c:if>
                <c:set var="iconURL"><ttc:getDocumentIconURL document="${document}"/></c:set>
                <c:set var="typeName"><is:getProperty key="${fn:toUpperCase(document.type)}" /></c:set>
                <c:set var="size" value="${document.properties['common:size']}" />
                <c:set var="lastContributor" value="${document.properties['dc:lastContributor']}" />
                <c:set var="date" value="${document.properties['dc:modified']}" />
                <c:if test="${empty date}">
                    <c:set var="date" value="${document.properties['dc:created']}" />
                </c:if>
            
            
                <tr class="draggable" data-id="${document.id}">
                    <!-- Icon -->
                    <td>
                        <img src="${iconURL}" alt="${typeName}" />
                    </td>
                    
                    <!-- Display name -->
                    <td>
                        <a href="${link.url}" target="${target}">${document.title}</a>
                        
                        <!-- Size -->
                        <c:if test="${not empty size}">
                            <span>(<ttc:formatFileSize size="${size}" />)</span>
                        </c:if>
                        
                        <!-- External -->
                        <c:if test="${link.external}">
                            <i class="glyphicons halflings new_window"></i>
                        </c:if>
                    </td>
                    
                    <!-- Last modification -->
                    <td>
                        <fmt:formatDate value="${date}" type="date" dateStyle="long" />
                    </td>
                    
                    <!-- Last contributor -->
                    <td>
                        <ttc:user name="${lastContributor}" />
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
