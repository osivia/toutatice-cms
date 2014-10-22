<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<div class="links">
    <ul class="list-unstyled">
    <c:forEach var="fgt" items="${fgts}">
    
        <li>
            <a href="#${fgt.key}">
                <span>${fgt.value}</span>
            </a>
        </li>
    </c:forEach>
</ul>
</div>
