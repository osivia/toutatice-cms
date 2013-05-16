

<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.customizer.ListTemplate"%>
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

	var ele = document.getElementById("toggleSampleText");

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
		
		<div id="toggleSampleText" style="display: none; border: 1px; border-style: solid"> <pre>
		
/*
implicits variables :
   - navigationPath :  current navigation folder path
   - navigationPubInfos : current navigation publication infos   
           navigationPubInfos.getLiveId() to get folder's live ID
   - basePath :  page folder path
   - contentPath : current item path
   - request : portlet request 
   - params : public selectors (shared parameters)
   - spaceId : space's (workspace or published space) live id
*/

import java.util.List;
import java.util.Arrays;		

String requete =  "ecm:path STARTSWITH '"+navigationPath+"'";

// format search by title
if (params.get("title") != null) {
requete += " AND " + NXQLFormater.formatTextSearch("dc:title",params.get("title")) ;
}

//format for dates search
if (params.get("datesId") != null) {
requete += " AND " + NXQLFormater.formatDateSearch("dc:created",params.get("datesId")) ;
}

// format search by discipline with OTHERs values
if (params.get("discipline") != null) {
List vocabsNames = Arrays.asList(new String[]{"disciplines_parent","disciplines"});
requete += " AND " +
NXQLFormater.formatOthersVocabularyEntriesSearch(request,vocabsNames,"acrp:disciplines",params.get("discipline")) ;

}

// get childrens
requete =  "AND ecm:parentId =  '"+navigationPubInfos.getLiveId()+"'";

}

requete += " ORDER BY dc:modified DESC";

return requete;
</pre>
</div>	
<br/>
	
		<label>Version</label><br/>
<%= ctx.formatDisplayLiveVersionList( (String) renderRequest.getAttribute("displayLiveVersion")) %><br/><br/>
<br/>	

		<label>Filtre sur les contenus</label><br/>
<%= ctx.formatRequestFilteringPolicyList( (String) renderRequest.getAttribute("requestFilteringPolicy")) %><br/><br/>
<br/>	

<%			
			String checkShowMetadatas = "checked";
			String showMetadatas = (String) request.getAttribute("showMetadatas");
			if( ! "1".equals( showMetadatas))
				checkShowMetadatas = "";
%>			
			
		<input type="checkbox" name="showMetadatas" value="1" <%=checkShowMetadatas%>/>Affichage des méta-données (sur le détail des documents) <br/>	
	
<br/>
		<input type="checkbox" name="displayNuxeoRequest" value="1" <%= displayNuxeoRequest%>/>Affichage de la requête (pour test) <br/>
<br/>
		<label>Limiter les résultats à </label> <input type="text" name="maxItems" value="${maxItems}" size="2"> items <br/><br/>

		<label>Pagination :</label> <br/>
			&nbsp;&nbsp;Mode normal&nbsp;&nbsp;&nbsp;&nbsp;: <input type="text" name="pageSize" value="${pageSize}" size="2"> items par page<br/>
			&nbsp;&nbsp;Mode maximized&nbsp: <input type="text" name="pageSizeMax" value="${pageSizeMax}" size="2"> items par page<br/><br/>
			
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


		<label>Référence rss :</label> <input type="text" name="rssLinkRef" value="${rssLinkRef}" size="10"> <br/><br/>


		<label>Titre RSS :</label> <input type="text" name="rssTitle" value="${rssTitle}" size="40"> <br/><br/>
			
			<input type="submit" name="modifierPrefs"  value="Valider">
			<input type="submit" name="annuler"  value="Annuler">
		</form>
	</div>
	
	