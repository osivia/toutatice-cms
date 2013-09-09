<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>

<%@ page import="fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.SpaceMenuBarFragmentModule"%>


<portlet:defineObjects />

<%
	Map<String,String> itemsChecked = (Map<String,String>) renderRequest.getAttribute(SpaceMenuBarFragmentModule.CHECKBOXES);
	String editChecked = itemsChecked.get("EDIT");
	String editCheckedStatus = editChecked != null ? editChecked : "";
	String permLinkChecked = itemsChecked.get("PERMLINK");
	String permLinkCheckedStatus = permLinkChecked != null ? permLinkChecked : "";
%>

	<div>
			<b></b><label>Items de la MenuBar:</label><br/></b>
			<br/>
			<br/>
			
			<input type="checkbox" name="items" value="EDIT" <%= editCheckedStatus %>>&nbsp;<b>Edition dans Nuxeo</b><br/>
			<br/>
			
			<label>Nom de l'item</label><br/>
			<input type="text" name="itemNameEDIT" value="${itemNameEDIT}" size="40"><br/>
			<br/>
			
			<label>Position de l'item</label><br/>
			<i>(nombre: <b>la numérotation commence à 1</b>)</i><br/>
			<input type="text" name="itemPosEDIT" value="${itemPosEDIT}" size="10"><br/>
			<br/>
			
			<label>Action sur l'évènement 'onClick' de l'item</label><br/>
			<input type="text" name="itemClickEDIT" value="${itemClickEDIT}" size="40"><br/>
			<br/>
			
			<label>Classe(s) CSS</label><br/>
			<i>Si plusieurs classes sont à renseigner, les séparer par un espace</i><br/>
			<input type="text" name="cssItemClassEDIT" value="${cssItemClassEDIT}" size="40"><br/>
			<br/>
			
			<br/>
			
			<input type="checkbox" name="items" value="PERMLINK" <%= permLinkCheckedStatus %>>&nbsp;<b>Permalien de l'espace courant</b><br/>
			<br/>
			
			<label>Nom de l'item</label><br/>
			<input type="text" name="itemNamePERMLINK" value="${itemNamePERMLINK}" size="40"><br/>
			<br/>
			
			<label>Position de l'item</label><br/>
			<i>(nombre <b>la numérotation commence à 1</b>)</i><br/>
			<input type="text" name="itemPosPERMLINK" value="${itemPosPERMLINK}" size="10"><br/>
			<br/>
			
			<label>Action sur l'évènement 'onClick' de l'item</label><br/>
			<input type="text" name="itemClickPERMLINK" value="${itemClickPERMLINK}" size="40"><br/>
			<br/>
			
			<label>Classe(s) CSS</label><br/>
			<i>Si plusieurs classes sont à renseigner, les séparer par un espace</i><br/>
			<input type="text" name="cssItemClassPERMLINK" value="${cssItemClassPERMLINK}" size="40"><br/>
			<br/>
			
			<br/>

	</div>