<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<c:set var="url"><ttc:documentLink document="${document}" displayContext="download"/></c:set>
<c:set var="name" value="${document.properties['file:content']['name']}"/>
<c:set var="size" value="${document.properties['file:content']['length']}"/>


<div class="card mb-3">
    <div class="card-body">
        <!-- Title -->
        <h3 class="h5 card-title text-overflow">
            <span><ttc:icon document="${document}"/></span>
            <span>${name}</span>
        </h3>

        <!-- Size -->
        <h4 class="h6 card-subtitle mb-3 text-muted">
            <span><ttc:fileSize size="${size}"/></span>
        </h4>

        <!-- OnlyOffice -->
        <c:if test="${not empty onlyofficeEditCollabUrl}">
            <p class="card-text mb-2">
                <c:choose>
                    <c:when test="${isEditableByUser}">
                        <!-- Live edition -->
                        <a href="${onlyofficeEditCollabUrl}" class="btn btn-outline-primary btn-block no-ajax-link">
                            <i class="glyphicons glyphicons-basic-pencil"></i>
                            <span><op:translate key="ONLYOFFICE_EDIT"/></span>
                        </a>
                    </c:when>

                    <c:otherwise>
                        <!-- Read only -->
                        <a href="${onlyofficeEditCollabUrl}" class="btn btn-outline-primary btn-block no-ajax-link">
                            <span><op:translate key="ONLYOFFICE_VIEW"/></span>
                        </a>
                    </c:otherwise>
                </c:choose>
            </p>
        </c:if>

        <!-- Drive -->
        <c:if test="${driveEnabled}">
            <c:choose>
                <c:when test="${empty driveEditUrl}">
                    <p class="card-text">
                        <a href="${driveEditUrl}" class="btn btn-outline-secondary btn-block btn-sm no-ajax-link">
                            <span><op:translate key="DRIVE_EDIT"/></span>
                        </a>
                    </p>
                </c:when>

                <c:otherwise>
                    <div class="alert alert-warning">
                        <span><op:translate key="MESSAGE_DRIVE_CLIENT_NOT_STARTED"/></span>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <!-- Download -->
        <p class="card-text">
            <a href="${url}" target="_blank" class="btn btn-outline-secondary btn-block btn-sm no-ajax-link">
                <i class="glyphicons glyphicons-basic-square-download"></i>
                <span><op:translate key="DOWNLOAD"/></span>
            </a>
        </p>
    </div>
</div>


<ttc:include page="view-default-extra.jsp"/>
