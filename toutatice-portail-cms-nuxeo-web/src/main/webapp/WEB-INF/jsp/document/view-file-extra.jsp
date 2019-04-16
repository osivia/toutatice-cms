<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<c:set var="url">
    <ttc:documentLink document="${document}" displayContext="download" />
</c:set>
<c:set var="name" value="${document.properties['file:content']['name']}" />
<c:set var="size" value="${document.properties['file:content']['length']}" />


<div class="panel panel-default">
    <div class="panel-body">
        <!-- Title -->
        <h3 class="h4 text-overflow">
            <span><ttc:icon document="${document}" /></span>
            <span>${name}</span>
        </h3>

        <!-- Size -->
        <p>
            <span><ttc:fileSize size="${size}" /></span>
        </p>

        <!-- OnlyOffice -->
        <c:if test="${not empty onlyofficeEditCollabUrl}">
            <c:choose>
                <c:when test="${isEditableByUser}">
                    <!-- Live edition -->
                    <p>
                        <a href="${onlyofficeEditCollabUrl}" class="btn btn-primary btn-block no-ajax-link">
                            <span><op:translate key="ONLYOFFICE_EDIT" /></span>
                        </a>
                    </p>
                </c:when>
                
                <c:otherwise>
                    <!-- Read only -->
                    <p>
                        <a href="${onlyofficeEditCollabUrl}" class="btn btn-primary btn-block no-ajax-link">
                            <span><op:translate key="ONLYOFFICE_VIEW" /></span>
                        </a>
                    </p>
                </c:otherwise>
            </c:choose>
        </c:if>
        
        <!-- Drive -->
        <c:if test="${driveEnabled}">
            <c:choose>
                <c:when test="${empty driveEditUrl}">
                    <p>
                        <a href="${driveEditUrl}" class="btn btn-default btn-block no-ajax-link">
                            <span><op:translate key="DRIVE_EDIT" /></span>
                        </a>
                    </p>
                </c:when>
                
                <c:otherwise>
                    <div class="alert alert-warning">
                        <span><op:translate key="MESSAGE_DRIVE_CLIENT_NOT_STARTED" /></span>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <!-- Download -->
        <a href="${url}" target="_blank" class="btn btn-default btn-block no-ajax-link">
            <span><op:translate key="DOWNLOAD" /></span>
        </a>
    </div>
</div>


<ttc:include page="view-default-extra.jsp" />
