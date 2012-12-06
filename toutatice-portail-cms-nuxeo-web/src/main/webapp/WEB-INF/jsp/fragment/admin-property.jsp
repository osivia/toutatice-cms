
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.fragment.FragmentType"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>


<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

String displayLiveVersion = "";
if( "1".equals( request.getAttribute("displayLiveVersion")))
	displayLiveVersion = "checked";
%>



	<div>
	
			<label>Path</label><br/>
			<input type="text" name="nuxeoPath" value="${nuxeoPath}" size="50"><br/>
			<input type="text" name="propertyName" value="${propertyName}" size="20"><br/>
			<label>Scope</label><br/>
			<%= ctx.formatScopeList( (String) renderRequest.getAttribute("scope")) %><br/>
			

			
		<input type="checkbox" name="displayLiveVersion" value="1" <%= displayLiveVersion%>/>Affichage des versions non publiées <br/>

		</form>
	</div>
	
	