<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="author" value="${document.properties['dc:creator']}" />
<c:set var="lastContributor" value="${document.properties['dc:lastContributor']}" />
<c:set var="created" value="${document.properties['dc:created']}" />
<c:set var="modified" value="${document.properties['dc:modified']}" />


<div class="metadata">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">
                <i class="halflings halflings-tags"></i>
                <span><op:translate key="METADATA" /></span>
            </h3>
        </div>
    
        <div class="panel-body">
            <dl>
                <!-- Creation -->
                <c:if test="${not empty created}">
                    <dt><op:translate key="DOCUMENT_METADATA_CREATION" /></dt>
                    <dd>
                        <p>
                            <span><op:translate key="DOCUMENT_METADATA_CREATED_ON" /></span>
                            <span><op:formatRelativeDate value="${created}" /></span>
                            
                            <c:if test="${not empty author}">
                                <br>
                                <span><op:translate key="DOCUMENT_METADATA_BY" /></span>
                                <span><ttc:user name="${author}"/></span>
                            </c:if>
                        </p>
                    </dd>
                </c:if>
                
                <!-- Modification -->
                <c:if test="${not empty modified}">
                    <dt><op:translate key="DOCUMENT_METADATA_MODIFICATION" /></dt>
                    <dd>
                        <p>
                            <span><op:translate key="DOCUMENT_METADATA_MODIFIED_ON" /></span>
                            <span><op:formatRelativeDate value="${modified}" /></span>
                            
                            <c:if test="${not empty lastContributor}">
                                <br>
                                <span><op:translate key="DOCUMENT_METADATA_BY" /></span>
                                <span><ttc:user name="${lastContributor}"/></span>
                            </c:if>
                        </p>
                    </dd>
                </c:if>
            
                <!-- Remote publication spaces -->
                <jsp:include page="metadata-remote-sections.jsp" />
            </dl>
        </div>
    </div>
</div>
