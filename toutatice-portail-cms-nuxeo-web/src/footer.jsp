

<%@ page contentType="text/plain; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="org.osivia.portal.api.selection.SelectionItem"%>
<%@page import="java.util.Set"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>

<portlet:defineObjects />

<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

Set<SelectionItem> selection = (Set<SelectionItem> ) renderRequest.getAttribute("selection")	;


%>

<div class="footer">

<% if( selection != null)	{	%>

<%= selection.size() %> item(s) sélectionnés

<% } %>

</div>





