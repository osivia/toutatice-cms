<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<c:set var="vignetteUrl"><ttc:pictureLink document="${document}" property="ttc:vignette" /></c:set>
<c:set var="description" value="${document.properties['dc:description']}" />
<c:set var="validationState" value="${document.properties['validationState']}" />

<c:if test="${metadata}">
	<c:if test="${not empty vignetteUrl or not empty description or not empty validationState}">
	    <div class="panel panel-default">
	        <div class="panel-body">
	
		        	
	            <!-- Vignette -->
	            <c:if test="${not empty vignetteUrl}">
	                <p><img src="${vignetteUrl}" alt="" class="img-responsive center-block"></p>
	            </c:if>
	            
	            <!-- Description -->
	            <c:if test="${not empty description}">
	                <p class="text-center">${description}</p>
	            </c:if>
	            
	            <!-- Validation state -->
	            <c:if test="${not empty validationState}">
	                <p class="text-center">
	                    <span class="label label-${validationState.color}">
	                        <c:if test="${not empty validationState.icon}">
	                            <i class="${validationState.icon}"></i>
	                        </c:if>
	                        
	                        <span><op:translate key="${validationState.key}" /></span>
	                    </span>
	                </p>
	            </c:if>
	
	        </div>
	    </div>
	</c:if>
</c:if>