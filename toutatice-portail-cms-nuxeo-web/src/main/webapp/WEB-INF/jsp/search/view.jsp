
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>



<%@page import="javax.portlet.ResourceURL"%>

<portlet:defineObjects />


<%

String searchBaseUrl = (String) request.getAttribute("searchUrl");

%>


<script type="text/javascript">
function onsubmitsearch( form)
{
   var searchUrl = "<%=searchBaseUrl%>";
   
   searchUrl = searchUrl.replace("__REPLACE_KEYWORDS__", form.keywords.value);
   form.action = searchUrl;

}
</script>


<div class="nuxeo-input-search-small">
		<form method="post" onsubmit="return onsubmitsearch( this);">
			<input type="text" name="keywords" value="" size="20" />	<input type="submit" value="OK" />
		</form>
</div>
