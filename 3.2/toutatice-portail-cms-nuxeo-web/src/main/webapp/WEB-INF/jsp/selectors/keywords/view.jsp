
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<portlet:actionURL var="addAction">
    <portlet:param name="action" value="add"/>
</portlet:actionURL>


<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<c:if test="${not empty libelle}">
    <span class="selector-libelle">${libelle}</span>
</c:if>

<div class="nuxeo-keywords-selector">
    <form method="post" action="${addAction}">
        <div class="table">
            <c:choose>
                <c:when test='${keywordMonoValued == "1"}'>
                    <!-- Mono-valued -->
                    <c:set var="textValue" value="${fn:join(keywords, ' ')}" />
                    <c:set var="imageName" value="monoAdd" />
                    <c:set var="imageSource" value="${contextPath}/img/submit.gif" />
                    <c:set var="imageTitle" value="Valider" />
                </c:when>
                
                <c:otherwise>
                    <!-- Multi-valued -->
                    <c:set var="textValue" value="${keyword}" />
                    <c:set var="imageName" value="add" />
                    <c:set var="imageSource" value="${contextPath}/img/add.gif" />
                    <c:set var="imageTitle" value="Ajouter" />
                        
                    <c:forEach var="item" items="${keywords}" varStatus="status">
                        <div class="table-row">
                            <div class="table-cell">${item}</div>
                            <div class="table-cell">
                                <a href="<portlet:actionURL>
                                        <portlet:param name="action" value="delete"/>
                                        <portlet:param name="occ" value="${status.count}"/>
                                     </portlet:actionURL>" class="delete" title="Supprimer"></a>
                            </div>
                        </div>
                    </c:forEach>                
                </c:otherwise>
            </c:choose>        
            
            <div class="table-row">
                <div class="table-cell input-text">
                    <input type="text" name="keyword" value="${textValue}" />
                </div>
                <div class="table-cell">
                    <input type="image" name="${imageName}" src="${imageSource}" title="${imageTitle}" />
                </div>
                
                <c:if test='${keywordMonoValued == "1"}'>
                    <div class="table-cell">
                        <input type="image" name="delete" src="${contextPath}/img/delete.gif" onclick="clearText(this)" title="Effacer" />
                    </div>
                </c:if>
            </div>    
        </div>
    </form>
</div>
