<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="document" description="Document DTO." required="true" rtexprvalue="true" type="fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO" %>
<%@ attribute name="linkable" description="Document linkable indicator. Default = true." required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="displayContext" description="Document link display context." required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="icon" description="Document type icon indicator. Default = false." required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ttc" uri="http://www.toutatice.fr/jsp/taglib/toutatice" %>


<span>
    <c:choose>
        <c:when test="${empty linkable or linkable}">
            <ttc:documentLink document="${document}" displayContext="${displayContext}" var="documentLink" />
            
            <a href="${documentLink.url}" class="no-ajax-link"
                <c:if test="${documentLink.external}">target="_blank"</c:if>    
            >
                <c:if test="${icon}">
                    <i class="${document.type.glyph}"></i>
                </c:if>
                <span>${document.title}</span>
            </a>
            
            <c:if test="${documentLink.external}">
                <small>
                    <i class="glyphicons glyphicons-new-window-alt"></i>
                </small>
            </c:if>
        </c:when>
        
        <c:otherwise>
            <span>
                <c:if test="${icon}">
                    <i class="${document.icon}"></i>
                </c:if>
                <span>${document.title}</span>
            </span>
        </c:otherwise>
    </c:choose>
</span>
