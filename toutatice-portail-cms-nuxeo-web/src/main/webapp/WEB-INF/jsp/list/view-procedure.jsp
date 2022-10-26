<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="imageErrorMessage"><op:translate key="IMAGE_ERROR_MESSAGE" /></c:set>


<c:if test="${not empty exportVarList}">
	<portlet:resourceURL var="exportCSVUrl" id="exportCSV">
	   <portlet:param name="injectdocs" value="true"/>
	</portlet:resourceURL>
	
	<ttc:addMenubarItem id="EXPORT_IN_CSV" labelKey="EXPORT_IN_CSV" url="${exportCSVUrl}" glyphicon="glyphicons glyphicons-table" ajax="false" />
</c:if>


<div class="table-responsive">
    <table class="table table-hover">
        <thead>
            <c:forEach var="column" items="${dashboardColumns}">
            
                <c:if test="${column.map['sortable']}">
                
                    <c:set var="sortLinkValue" value="${column.map['variableName']}"/>
                    
                    <c:choose>
                        <c:when test="${column.map['variableName'] eq sortValue}">
                            <c:if test="${sortOrder eq 'ASC'}">
	                            <c:set var="sortLinkOrder" value="DESC"/>
	                            <c:set var="glyphClass" value="halflings halflings-sort-by-attributes"/>
	                        </c:if>
	                        <c:if test="${sortOrder eq 'DESC'}">
	                            <c:set var="sortLinkOrder" value="ASC"/>
	                            <c:set var="glyphClass" value="halflings halflings-sort-by-attributes-alt"/>
	                        </c:if>
                        </c:when>
                        <c:otherwise>
                            <c:set var="sortLinkOrder" value="ASC"/>
                        </c:otherwise>
                    </c:choose>
                    
                    <portlet:actionURL var="sortLinkUrl">
                        <portlet:param name="sortValue" value="${sortLinkValue}" />
                        <portlet:param name="sortOrder" value="${sortLinkOrder}" />
                    </portlet:actionURL>
                
                    <th>
	                    <a href="${sortLinkUrl}" class="ajax-link">
	                       ${column.map['label']}
	                    </a>
	                     <c:if test="${column.map['variableName'] eq sortValue}"> <i class="${glyphClass}"></i></c:if>
                    </th>
                </c:if>
                <c:if test="${not column.map['sortable']}">
                    <th>${column.map['label']}</th>
                </c:if>
            
            </c:forEach>
        </thead>
        <tbody>
		    <c:forEach var="document" items="${documents}">
				<c:set var="webid" value="${document.properties['ttc:webid']}" />
				<ttc:documentLink document="${document}" var="documentLink"/>
				
                <tr>
		            <c:forEach var="column" items="${dashboardColumns}" varStatus="status">
                        <c:set var="variableName" value="${column.map['variableName']}" />
                        <c:set var="variableValue" value="${document.properties[variableName]}"></c:set>
                        <c:set var="variableType" value="${variablesDefinitions[variableName]['type']}" />
                        <c:set var="enableLink" value="${column.map['enableLink'] && document.properties['pi:currentStep'] != 'endStep'}" />
                        
                        <td>
                            
                            <c:choose>
                                <c:when test="${empty variableValue}"></c:when>
                            
                                <c:when test="${variableType eq 'DATE'}">
                                    <fmt:parseDate value="${variableValue}" var="variableValue" pattern="dd/MM/yyyy" />
                                    <fmt:formatDate value="${variableValue}" var="variableValue" type="DATE" />
                                </c:when>
                                
                                <c:when test="${variableType eq 'DATETIME'}">
                                    <fmt:formatDate value="${variableValue}" var="variableValue" type="BOTH" />
                                </c:when>
                                
                                <c:when test="${variableType eq 'VOCABULARY'}">
                                    <c:set var="variableValue"><ttc:vocabularyLabel name="${variablesDefinitions[variableName]['vocabularyId']}" key="${variableValue}" /></c:set>
                                </c:when>
                                
                                <c:when test="${variableType eq 'PERSON'}">
                                    <c:set var="variableValue"><ttc:user name="${variableValue}" linkable="false" /></c:set>
                                </c:when>
                                
                                <c:when test="${variableType eq 'RECORD'}">
                                    <c:set var="variableValue"><ttc:title path="${variableValue}" linkable="false" /></c:set>
                                </c:when>
                                
                                <c:when test="${variableType eq 'FILE'}">
                                    <c:choose>
                                        <c:when test="${enableLink}">
                                            <c:set var="variableValue"><i class="${variableValue.icon}"></i> <span>${variableValue.name}</span></c:set>
                                        </c:when>
                                        
                                        <c:otherwise>
                                            <c:set var="variableValue">
                                                <i class="${variableValue.icon}"></i>
                                                <a href="${variableValue.url}" target="_blank" class="no-ajax-link">
                                                    <span>${variableValue.name}</span>
                                                </a>
                                            </c:set>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                
                                <c:when test="${variableType eq 'PICTURE'}">
                                    <c:set var="variableValue">
                                        <a href="${variableValue.url}" class="thumbnail no-margin-bottom no-ajax-link" data-fancybox="gallery" data-caption="${variableValue.name}" data-type="image">
                                            <img src="${variableValue.url}" alt="${variableValue.name}" class="vignette img-fluid" data-error-message="${imageErrorMessage}">
                                        </a>
                                    </c:set>
                                </c:when>
                            </c:choose>
                            
                            <c:choose>
                                <c:when test="${enableLink}">
                                    <a href="${documentLink.url}" class="no-ajax-link">${variableValue}</a>
                                </c:when>
                                
                                <c:otherwise>${variableValue}</c:otherwise>
                            </c:choose>
                        </td>
                    </c:forEach>
				</tr>
		    </c:forEach>
        </tbody>
    </table>
</div>