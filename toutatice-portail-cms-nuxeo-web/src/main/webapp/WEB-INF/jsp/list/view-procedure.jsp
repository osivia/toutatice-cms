<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page isELIgnored="false"%>


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
		                  <td>
                              <c:set var="columnValue" value="${document.properties[variableName]}"></c:set>
		                      
		                      <c:if test="${variablesDefinitions[variableName]['type'] eq 'DATE'}">
		                          <fmt:parseDate value = "${columnValue}" var="columnValue" pattern = "dd/MM/yyyy" />
                                  <fmt:formatDate value="${columnValue}" var="columnValue" type="DATE"/>
                              </c:if>
	                          <c:if test="${variablesDefinitions[variableName]['type'] eq 'DATETIME'}">
	                              <fmt:formatDate value="${columnValue}" var="columnValue" type="BOTH"/>
	                          </c:if>
	                          
	                          <c:choose>
	                               <c:when test="${column.map['enableLink']}">
				                      <a href="${documentLink.url}" class="no-ajax-link">${columnValue}</a>
	                               </c:when>
	                               <c:otherwise>
				                      ${columnValue}
	                               </c:otherwise>
	                          </c:choose>
		                  </td>
		            </c:forEach>
				</tr>
		    </c:forEach>
        </tbody>
    </table>
</div>