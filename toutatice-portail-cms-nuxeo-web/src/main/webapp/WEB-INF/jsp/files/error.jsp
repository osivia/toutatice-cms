<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>

<%@ page contentType="text/html" isELIgnored="false"%>


<portlet:defineObjects />


<p class="lead text-danger">
    <i class="glyphicons halflings exclamation-sign"></i>
    <span><is:getProperty key="MESSAGE_PATH_UNDEFINED" /></span>
</p>
