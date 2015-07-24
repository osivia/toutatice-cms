<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="internationalization" prefix="is"%>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>

    <c:set var="publishedDocuments" value="${document.publishedDocuments}" />
        
    <c:choose>
        
        <c:when test="${not empty publishedDocuments}">
        
             <c:if test="${remoteSectionsPage}">
                 <h4><is:getProperty key="MANY_REMOTE_SECTIONS_FOR_DOCUMENT" args="${document.title}"/></h4>
             </c:if>
        
                <c:forEach items="${publishedDocuments}" var="publishedDocument">
                
                    <div class="media">
                        <div class="media-body">
                            <c:set var="publishedDocumentURL"><ttc:transformNxLink link="${publishedDocument.nxUrl}" params="${publishedDocument.linkContextualization}"/></c:set>
                            <a href="${publishedDocumentURL}"><span>${publishedDocument.sectionTitle}</span></a>
                        </div>
                        <div class="media-right">
                            <small>(${publishedDocument.path})</small>
                        </div>
                    </div>
                    
                </c:forEach>
            
        </c:when>
        <c:otherwise>
            <c:if test="${remoteSectionsPage}">
                <h4><is:getProperty key="NO_REMOTE_SECTIONS_FOR_DOCUMENT" args="${document.title}"/></h4>
            </c:if>
            
            <c:set var="documentURL"><ttc:documentLink document="${document}" /></c:set>
            <div class="media">
                <div class="media-body">
                    <a href="${documentURL}"><span>${document.title}</span></a>
                </div>
                <div class="media-right">
                    <small>(${document.path})</small>
                </div>
            </div>
        
        </c:otherwise>
        
    </c:choose>
