
<%@page import="fr.toutatice.portail.core.nuxeo.ListTemplate"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page isELIgnored="false" %>


<%@page import="java.util.Map"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet"%>


<portlet:defineObjects/>

<script language="javascript"> 
function toggleSample() {
	var ele = document.getElementById("toggleText");
	var text = document.getElementById("displayText");
	if(ele.style.display == "block") {
    		ele.style.display = "none";
  	}
	else {
		ele.style.display = "block";
	}
} 
</script>



<%
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String beanShell = "";
if( "1".equals( request.getAttribute("beanShell")))
		beanShell = "checked";
String displayNuxeoRequest = "";
if( "1".equals( request.getAttribute("displayNuxeoRequest")))
	displayNuxeoRequest = "checked";
%>


	<div>
		<form method="post" action="<portlet:actionURL/>">
		<label>Requête Nuxeo</label><br/>
		<textarea rows="10" cols="75" name="nuxeoRequest" >${nuxeoRequest}</textarea><br/><br/>
		<input type="checkbox" name="beanShell" value="1" <%= beanShell%>/>Interprétation BeanShell de la requête (
		
		<a id="displayText" href="javascript:toggleSample();">Exemple</a>)<br/>
		
		<div id="toggleText" style="display: none; border: 1px; border-style: solid"> <pre>
String requete =  "ecm:path STARTSWITH '/default-domain/workspaces/toutatice'";

if (params.get("title") != null) {
requete += " AND " + NXQLFormater.formatTextSearch("dc:title",params.get("title")) ;
}

if (params.get("description") != null) {
requete += " AND " + NXQLFormater.formatTextSearch("dc:description",params.get("description")) ;
}

if (params.get("nature") != null) {
requete += " AND " +
NXQLFormater.formatVocabularySearch("dc:nature",params.get("nature")) ;

}

if (params.get("subject") != null) {
requete += " AND " +
NXQLFormater.formatVocabularySearch("dc:subjects",params.get("subject")) ;

}

requete += " ORDER BY dc:modified DESC";

return requete;
</pre>
</div>		
<br/>
		<input type="checkbox" name="displayNuxeoRequest" value="1" <%= displayNuxeoRequest%>/>Affichage de la requête (pour test) <br/>
<br/>
		<label>Limiter les résultats à <input type="text" name="maxItems" value="${maxItems}" size="2"> items <br/><br/>

		<label>Pagination :</label> <input type="text" name="pageSize" value="${pageSize}" size="2"> items par page<br/><br/>
		<label>Style d'affichage</label><br/>
		<select name="style">
<%
			Map<String, ListTemplate> templates = (Map<String, ListTemplate>) request.getAttribute("templates");
			String style = (String) request.getAttribute("style");

			for(Map.Entry<String,ListTemplate> template : templates.entrySet()){
				String possibleStyle = template.getValue().getKey();
					if( possibleStyle.equals(style)){
%>
										<option selected="selected" value="<%= possibleStyle %>"><%= template.getValue().getLabel() %></option>
<%
					}else{
%>
										<option value="<%= possibleStyle %>"><%= template.getValue().getLabel() %></option>
<%						
					}
				}

%>
									</select><br/><br/>		
	
		<label>Scope</label><br/>
<%= ctx.formatScopeList( (String) renderRequest.getAttribute("scope")) %><br/><br/>

		<label>Référence permalink :</label> <input type="text" name="permaLinkRef" value="${permaLinkRef}" size="10"> <br/><br/>
			
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	