
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.selectors.KeywordsSelectorPortlet"%>
<%@page import="org.apache.jasper.tagplugins.jstl.core.ForEach"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />


<%

List<String> keywords = (List<String>) renderRequest.getAttribute("keywords");
if( keywords.size() > 0) 	{
%>

<table class="nuxeo-keywords-selector-table"  cellspacing="5" width="95%">

<%
int occ = 0;

for (String keywordOcc : keywords){
	
	String sOcc = Integer.toString(occ++);
	
%>
	<tr>
		<td width="90%"><%= keywordOcc%> </td> <td>
		
		<a href="<portlet:actionURL>
         		<portlet:param name="action" value="delete"/>
         		<portlet:param name="occ" value="<%= sOcc %>"/>
         </portlet:actionURL>"><img src="<%= renderRequest.getContextPath() %>/img/delete.gif" border="0"/></a>
		
		</td width="10%">
	</tr>
<%			
	}
%>


</table>

<%			
}
%>

<div class="nuxeo-keywords-selector">
		<form method="post" action="<portlet:actionURL/>">
			<input type="text" name="keyword" value="${keyword}" size="15">	
			<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" > 
		</form>			
			
			
		
	</div>
	