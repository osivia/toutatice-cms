<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="nuxeo-file-browser no-ajax-link">
    <c:choose>
        <c:when test="${not empty path}">
            <!-- Description -->
            <p>${description}</p>
            
            <!-- Table -->
            <div class="table-responsive">
                <table class="table">
                    <!-- Table header -->
                    <thead>
                        <tr>
                            <th></th>
                            <th><is:getProperty key="FILE_BROWSER_NAME" /></th>
                            <th><is:getProperty key="FILE_BROWSER_DATE" /></th>
                            <th><is:getProperty key="FILE_BROWSER_LAST_CONTRIBUTOR" /></th>
                        </tr>
                    </thead>
                    
                    <!-- Table body -->
                    <tbody>
                        <c:forEach var="document" items="${documents}">
                            <!-- External link ? -->
                            <c:remove var="target" />
                            <c:if test="${document.link.external}">
                                <c:set var="target" value="_blank" />
                            </c:if>
                        
                            <tr>
                                <!-- Icon -->
                                <td>
                                    <img src="${pageContext.request.contextPath}${document.iconSource}" alt="${document.iconAlt}">
                                </td>
                                
                                <!-- Display name -->
                                <td>
                                    <a href="${document.link.url}" target="${target}">${document.title}</a>
                                    
                                    <c:if test="${not empty document.size}">
                                        <span>(${document.size})</span>
                                    </c:if>
                                    
                                    <c:if test="${not empty document.downloadLink.url}">
                                        <!-- External link ? -->
                                        <c:remove var="downloadTarget" />
                                        <c:if test="${document.downloadLink.external}">
                                            <c:set var="downloadTarget" value="_blank" />
                                        </c:if>
                                    
                                        <a href="${document.downloadLink.url}" target="${downloadTarget}">
                                            <i class="glyphicons download_alt"></i>
                                            <span class="sr-only"><is:getProperty key="DOWNLOAD" /></span>
                                        </a>
                                    </c:if>
                                </td>
                                
                                <!-- Date -->
                                <td>${document.date}</td>
                                
                                <!-- Last contributor -->
                                <td>
                                    <i class="glyphicons halflings user"></i>
                                    <span>${document.lastContributor}</span>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>        
                </table>
            </div>
        </c:when>
    
        <c:otherwise>
            <p class="text-danger">
                <i class="glyphicons halflings exclamation-sign"></i>
                <span><is:getProperty key="MESSAGE_PATH_UNDEFINED" /></span>
            </p>
        </c:otherwise>
    </c:choose>
</div>
