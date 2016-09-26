<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>

<%@ page isELIgnored="false" %>


<div>
    <p class="lead">
        <i class="glyphicons glyphicons-notes"></i>
        <span><op:translate key="DRAFTS_PORTLET_TITLE" /></span>
    </p>
    
    <div class="panel panel-default">
        <div class="panel-body">
            <ul class="list-unstyled">
			    <c:forEach var="document" items="${documents}">
			        <li><ttc:title document="${document}" /></li>
			    </c:forEach>
			</ul>
        </div>
    </div>
</div>