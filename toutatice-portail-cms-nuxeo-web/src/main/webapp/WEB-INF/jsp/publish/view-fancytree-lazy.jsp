<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />

<portlet:actionURL name="drop" var="dropActionURL" />
<portlet:resourceURL var="lazyLoadingURL" />


<div class="menu" data-dropurl="${dropActionURL}" data-lazyloadingurl="${lazyLoadingURL}">
    <div class="portlet-filler hidden-scrollbar">
        <c:if test="${startLevel eq 1 and empty displayItem.children}">
            <p class="text-muted text-center"><op:translate key="NO_ITEMS" /></p>
        </c:if>
    
        <div class="fancytree fancytree-lazy">
            <c:set var="parent" value="${displayItem}" scope="request" />
            <c:set var="lazy" value="true" scope="request" />
        
            <c:choose>
                <c:when test="${startLevel eq 1}">
                    <jsp:include page="display-fancytree-items.jsp" />
                </c:when>
                
                <c:otherwise>
                    <ul>
                        <li data-retain="${displayItem.selected}" data-acceptedtypes="${fn:join(displayItem.acceptedTypes, ',')}" data-expanded="true" data-id="${displayItem.id}" data-current="${displayItem.current}"
                            <c:choose>
                                <c:when test="${displayItem.current or displayItem.lastSelected}">class="text-primary"</c:when>
                                <c:otherwise>class="text-muted"</c:otherwise>
                            </c:choose>
                            <c:if test="${displayItem.navItem.type.folderish}">data-folder="true"</c:if>
                            <c:if test="${not empty displayItem.navItem.type.icon and not fn:contains(displayItem.navItem.type.icon, 'folder')}">data-iconclass="${displayItem.navItem.type.icon}"</c:if>
                        >
                            <!-- Node -->
                            <a href="${displayItem.url}">${displayItem.title}</a>
                        
                            <jsp:include page="display-fancytree-items.jsp" />
                        </li>
                    </ul>
                </c:otherwise>
            </c:choose>    
        </div>
    </div>
</div>
