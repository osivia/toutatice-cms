<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page isELIgnored="false"%>

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
				<c:set var="procedureType" value="${document.type.name}" />
				<c:if test="${procedureType eq 'Record'}">
				    <c:set var="globalVariablesValues" value="${document.properties['rcd:globalVariablesValues']}" />
				</c:if>
				<c:if test="${procedureType eq 'ProcedureInstance' or empty document.type}">
                    <c:set var="globalVariablesValues" value="${document.properties['pi:globalVariablesValues']}" />
                </c:if>
                
				<ttc:documentLink document="${document}" var="documentLink"/>
				<tr>
		            <c:forEach var="column" items="${dashboardColumns}" varStatus="status">
	                <c:set var="variableName" value="${column.map['variableName']}" />
		                  <td>
		                      <c:choose>
		                          <c:when test="${variableName eq 'dc:creator' or variableName eq 'dc:created' or variableName eq 'dc:lastContributor' 
		                                      or variableName eq 'dc:modified'}">
		                              <c:set var="columnValue" value="${document.properties[variableName]}"></c:set>
		                          </c:when>
		                          <c:otherwise>
		                              <c:set var="columnValue" value="${globalVariablesValues[variableName]}"></c:set>
		                          </c:otherwise>
		                      </c:choose>
		                      
		                      <c:if test="${variablesDefinitions[variableName]['type'] eq 'DATE'}">
		                          <fmt:parseDate value = "${columnValue}" var="columnValue" pattern = "dd/MM/yyyy" />
                                  <fmt:formatDate value="${columnValue}" var="columnValue" type="DATE"/>
                              </c:if>
	                          <c:if test="${variablesDefinitions[variableName]['type'] eq 'DATETIME'}">
	                              <fmt:formatDate value="${columnValue}" var="columnValue" type="BOTH"/>
	                          </c:if>
	                          
		                      <c:if test="${status.first}">
			                      <a href="${documentLink.url}" class="no-ajax-link">${columnValue}</a>
		                      </c:if>
		                      <c:if test="${not status.first}">
			                      ${columnValue}
		                      </c:if>
		                  </td>
		            </c:forEach>
				</tr>
		    </c:forEach>
        </tbody>
    </table>
</div>