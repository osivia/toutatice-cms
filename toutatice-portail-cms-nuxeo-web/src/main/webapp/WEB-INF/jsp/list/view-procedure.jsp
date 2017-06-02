<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false"%>


<div class="table-responsive">
    <table class="table table-hover">
        <thead>
            <c:forEach var="column" items="${dashboardColumns}">
                <th>${column.map['label']}
                    <c:if test="${column.map['sortable']}">
		                <i class="halflings halflings-sort-by-attributes"></i>
                    </c:if>
                </th>
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
		                      <c:if test="${status.first}">
			                      <a href="${documentLink.url}">${columnValue}</a>
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