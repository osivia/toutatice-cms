<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<div class="portlet-filler d-flex flex-column flex-grow-1">
    <div class="row flex-grow-1">
        <div class="col-md-8 col-lg-9 d-flex flex-column mh-100">
            <div class="d-flex flex-column flex-grow-1">
                <!-- Document view -->
                <ttc:include page="view-${dispatchJsp}.jsp"/>
            </div>
        </div>

        <div class="col-md-4 col-lg-3 d-flex flex-column mh-100">
            <div class="d-flex flex-column flex-grow-1">
                <!-- Document extra view -->
                <ttc:include page="view-${dispatchExtraJsp}-extra.jsp"/>

                <!-- Document attachments view -->
                <c:if test="${attachments}">
                    <ttc:include page="attachments.jsp"/>
                </c:if>

                <!-- Metadata -->
                <c:if test="${metadata}">
                    <ttc:include page="metadata.jsp"/>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Document comments view -->
    <ttc:comments document="${document}"/>
</div>
