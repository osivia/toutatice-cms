

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>


<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

%>



	<div>
	
			<label>Path picture</label><br/>
			<input type="text" name="nuxeoPath" value="${nuxeoPath}" size="50"><br/>
			<label>Path lien(facultatif)</label><br/>
			<input type="text" name="targetPath" value="${targetPath}" size="50"><br/>
	</div>
	
	