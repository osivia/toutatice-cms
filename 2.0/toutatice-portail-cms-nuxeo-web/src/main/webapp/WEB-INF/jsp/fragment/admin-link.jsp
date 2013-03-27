<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects />

	<div>
			<label>Nom du lien</label><br/>
			<i>Il peur s'agir d'une propriété du document Nuxeo; ex: cd:title</i><br/>
			<input type="text" name="linkName" value="${linkName}" size="40"><br/>
			<br/>
	
			<label>Chemin Nuxeo du document</label><br/>
			<i>Il peut s'agir d'une propriété calculée; ex: ${basePath}</i><br/>
			<input type="text" name="docPathForLink" value="${docPathForLink}" size="40"><br/>
			<br/>
			
			<label>Classe(s) CSS</label><br/>
			<i>Si plusieurs classes sont à renseigner, les séparer par un espace</i><br/>
			<input type="text" name="cssLinkClass" value="${cssLinkClass}" size="40"><br/>
			<br/>
			
			<% String isNuxeoLink = "";
			   if("1".equals(renderRequest.getAttribute("isNuxeoLink"))){
				   isNuxeoLink = "checked='checked'";	
			   }
			%>
			<input type="checkbox" name="isNuxeoLink" value="1" <%= isNuxeoLink %> />&nbsp;Lien vers Nuxeo<br/>
			<br/>

	</div>
	
	