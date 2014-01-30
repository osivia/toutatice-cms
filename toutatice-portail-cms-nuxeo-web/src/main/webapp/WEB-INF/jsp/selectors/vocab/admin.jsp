
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>


<portlet:defineObjects/>

<% 
	String othersLabelSpanContent = " "; 
	String othersLabel = (String) renderRequest.getAttribute("othersLabel");
	if(StringUtils.isNotEmpty(othersLabel))
			othersLabelSpanContent = "   Libellé:&nbsp;<input type=\"text\" name=\"othersLabel\" value=\"" + othersLabel +"\"/>"; 
%>

<script type="text/javascript">
	var othersLabelSpanContent = "   Libellé:&nbsp;<input type=\"text\" name=\"othersLabel\" value=\"${othersLabel}\"/>"; 
	function manageOthersOption(othersCheckbox){
		var othersLabelSpan = document.getElementById("othersLabelSpan");
		if(othersCheckbox.checked == true){
			othersLabelSpan.innerHTML = othersLabelSpanContent;
		} else {
			othersCheckbox.form.elements["othersLabel"] = "";
			othersLabelSpan.innerHTML = " ";
		}
	}
</script>

	<div>
		<form method="post" action="<portlet:actionURL/>">
		
				
			<label>Libellé</label><br/>
			<input type="text" name="libelle" value="${libelle}" size="40"/><br/>
		
			<label>Identifiant sélecteur</label><br/>
			<input type="text" name="selectorId" value="${selectorId}" size="40"/><br/>
			
			<% String monoValued = "";
			   if("1".equals(renderRequest.getAttribute("selectorMonoValued"))){
				   monoValued = "checked='checked'";	
			   }
			%>
			<input type="checkbox" name="selectorMonoValued" value="1" <%= monoValued %>/>&nbsp;Sélecteur monovalué<br/>
			<br/>
			
			<% String othersOption = "";
			   if("1".equals(renderRequest.getAttribute("othersOption"))){
				   othersOption = "checked='checked'";	
			   }
			%>
			<input type="checkbox" name="othersOption" value="1" <%= othersOption %> onclick="manageOthersOption(this);"/>&nbsp;Champ "Autres"&nbsp;&nbsp;
			<span id="othersLabelSpan"><%= othersLabelSpanContent %></span>
			<br/><br/>
			
			<label>Nom vocabulaire niveau 1</label><br/>
			<input type="text" name="vocabName1" value="${vocabName1}" size="40"/><br/>
			
			<label style="display: inline-block; padding-left: 20px;}">Préselection</label><input type="text" name="preselect1" value="${preselect1}" size="20"/><br/>
						
			<label>Nom vocabulaire niveau 2 (facultatif)</label><br/>
			<input id="vocab2" type="text" name="vocabName2" value="${vocabName2}" size="40"/><br/>

			<label>Nom vocabulaire niveau 3 (facultatif)</label><br/>
			<input id="vocab3" type="text" name="vocabName3" value="${vocabName3}" size="40"/><br/>

			<input type="submit" name="modifierPrefs"  value="Valider"/>
			<input type="submit" name="annuler"  value="Annuler"/>
		</form>
	</div>
	
	