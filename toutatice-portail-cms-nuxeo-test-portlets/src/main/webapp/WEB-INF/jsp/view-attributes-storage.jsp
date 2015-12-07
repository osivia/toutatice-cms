<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects/>

<c:set var="namespace"><portlet:namespace/></c:set>


<!-- Tabs -->
<c:set var="currentTab" value="tags" scope="request" />
<jsp:include page="includes/tabs.jsp" />


<section>
    <!-- Title -->
    <h3>
        <op:translate key="ATTRIBUTES_STORAGE" />
    </h3>
    
    
    <c:choose>
        <c:when test="${not empty configuration.selectionId}">
            <portlet:actionURL name="addToSelection" var="url" />
            <c:set var="placeholder"><op:translate key="ADD_TO_SELECTION_PLACEHOLDER" /></c:set>
            
            <form action="${url}" method="post" class="form-horizontal">
                <fieldset>
                    <legend><op:translate key="ADD_TO_SELECTION"/></legend>
                    
                    <div class="form-group">
                        <label for="${namespace}-selection-content" class="col-sm-3 control-label"><op:translate key="SELECTION_CONTENT"/></label>
                        <div class="col-sm-9">
                            <div class="input-group">
                                <input id="${namespace}-selection-content" type="text" name="content" class="form-control" placeholder="${placeholder}">
                                <span class="input-group-btn">
                                    <button type="submit" class="btn btn-default">
                                        <i class="glyphicons glyphicons-plus"></i>
                                        <span class="sr-only"><op:translate key="ADD" /></span>
                                    </button>
                                </span>
                            </div>
                        </div>
                    </div>
                </fieldset>
            </form>
        </c:when>
        
        <c:otherwise>
            <p class="text-muted text-center"><op:translate key="MESSAGE_EMPTY_SELECTION_ID" /></p>
        </c:otherwise>
    </c:choose>
    
    
    
    <portlet:actionURL name="editStorage" var="url" />
    <c:set var="namePlaceholder"><op:translate key="STORAGE_ATTRIBUTE_NAME" /></c:set>
    <c:set var="valuePlaceholder"><op:translate key="STORAGE_ATTRIBUTE_VALUE" /></c:set>

    <form action="${url}" method="post" class="form-horizontal">
        <fieldset>
            <legend><op:translate key="PORTLET_SEQUENCING" /></legend>
            
            <!-- Priority -->
            <div class="form-group">
                <label class="control-label col-sm-3"><op:translate key="PRIORITY" /></label>
                <div class="col-sm-9">
                    <p class="form-control-static">
                        <c:choose>
                            <c:when test="${not empty priority}">${priority}</c:when>
                            <c:otherwise><span class="text-muted"><op:translate key="MESSAGE_EMPTY_PRIORITY" /></span></c:otherwise>
                        </c:choose>
                    
                    </p>
                </div>
            </div>
            
            <!-- Last refresh -->
            <div class="form-group">
                <label class="control-label col-sm-3"><op:translate key="LAST_REFRESH" /></label>
                <div class="col-sm-9">
                    <p class="form-control-static"><fmt:formatDate value="${lastRefresh}" type="both" dateStyle="long" timeStyle="medium" /></p>
                </div>
            </div>
            
            <!-- Storage attributes -->
            <div class="form-group">
                <label class="control-label col-sm-3"><op:translate key="STORAGE_ATTRIBUTES" /></label>
                
                <div class="col-sm-9">
                    <c:forEach var="item" items="${storageAttributes}">
                        <div class="form-group">
                            <div class="col-xs-4">
                                <p class="form-control-static">${item.key}</p>
                            </div>
                            <div class="col-xs-6">
                                <p class="form-control-static">${item.value}</p>
                            </div>
                            <div class="col-xs-2">
                                <button type="submit" name="remove" value="${item.key}" class="btn btn-default">
                                    <i class="glyphicons glyphicons-remove-2"></i>
                                    <span class="sr-only"><op:translate key="REMOVE" /></span>
                                </button>
                            </div>
                        </div>
                    </c:forEach>
                    
                    <div class="row">
                        <div class="col-xs-4">
                            <input type="text" name="attributeName" class="form-control" placeholder="${namePlaceholder}">
                        </div>
                        <div class="col-xs-6">
                            <input type="text" name="attributeValue" class="form-control" placeholder="${valuePlaceholder}">
                        </div>
                        <div class="col-xs-2">
                            <button type="submit" name="add" class="btn btn-default">
                                <i class="glyphicons glyphicons-plus"></i>
                                <span class="sr-only"><op:translate key="ADD" /></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </fieldset>
    </form>
</section>
