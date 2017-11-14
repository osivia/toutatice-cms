<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="/WEB-INF/tld/vocabulary-selector.tld" prefix="vs" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<portlet:actionURL var="actionURL" />


<c:set var="title"><op:translate key="SELECTOR_MULTI_ADD" /></c:set>


<c:choose>
    <c:when test="${selectorMonoValued eq '1'}">
        <c:set var="name" value="monoAdd" />
        <c:set var="glyphicon" value="halflings halflings-ok" />
        <c:set var="title"><op:translate key="SELECTOR_MONO_ADD" /></c:set>
    </c:when>
    
    <c:otherwise>
        <c:set var="name" value="add" />
        <c:set var="glyphicon" value="halflings halflings-plus" />
        
    </c:otherwise>
</c:choose>



<div class="vocabulary-selector">
    <form action="${actionURL}" method="post" role="form">
        <!-- Label -->
        <c:if test="${not empty libelle}">
            <label>${libelle}</label>
        </c:if>

        <c:if test="${empty preselect1}">
            <c:choose>
                <c:when test="${selectorMonoValued eq '1'}">
                    <portlet:actionURL var="url">
                        <portlet:param name="vocab1Id" value="SELECTED_VALUE" />
                        <portlet:param name="monovaluedSubmit" value="monovaluedSubmit" />
                    </portlet:actionURL>
                </c:when>
                
                <c:otherwise>
                    <portlet:renderURL var="url">
                        <portlet:param name="vocab1Id" value="SELECTED_VALUE" />
                    </portlet:renderURL>
                </c:otherwise>
            </c:choose>

            <div class="form-group">
                <div class="media">
                    <div
                        <c:if test="${not empty vocab1Id and empty vocab2Id and (selectorMonoValued ne '1')}">class="media-body"</c:if>
                    >
                        <select name="vocab1Id" class="form-control" onchange="refreshOnVocabularyChange(this, '${url}')">
                            <option value="">Tous</option>
                            
                            <c:forEach var="optGroup" items="${vocab1.children}">
                            
                            	<optgroup label="${optGroup.value.label}">
                            
                                <c:forEach var="child" items="${optGroup.value.children}">
                                	<c:set var="curSelection" value="${optGroup.value.id}/${child.value.id}" />
                                	
	                                <option value="${optGroup.value.id}/${child.value.id}"
	                                    <c:if test="${curSelection eq vocab1Id}">selected="selected"</c:if>
	                                >${child.value.label}
	                                </option>
                                
                                
                                </c:forEach>
                                
                                </optgroup>
                            
                            </c:forEach>
                            
                            <c:if test="${not empty othersLabel and not empty vocab1.children}">
                                <option value="othersVocabEntries"
                                    <c:if test="${'othersVocabEntries' eq vocab1Id}">selected="selected"</c:if>
                                >${othersLabel}</option>
                            </c:if>
                        </select>
                    </div>
                    
                    <c:if test="${not empty vocab1Id and empty vocab2Id and (selectorMonoValued ne '1')}">
                        <div class="media-right">
                            <button type="submit" name="add" class="btn btn-default">
                                <i class="halflings halflings-plus"></i>
                                <span class="sr-only">${title}</span>
                            </button>
                        </div>
                    </c:if>
                </div>
            </div>
		</c:if>
    </form>
</div>
