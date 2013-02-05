
<%@page import="java.util.ArrayList"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.selectors.VocabSelectorPortlet"%>
<%@page import="java.util.Map"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.PageSelectors"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyEntry"%>
<%@page import="org.osivia.portal.api.windows.PortalWindow"%>

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
PortalWindow window = (PortalWindow) renderRequest.getAttribute("window");

String libelle = (String) request.getAttribute("libelle");
String vocab1Id = (String) request.getAttribute("vocab1Id");
String vocab2Id = (String) request.getAttribute("vocab2Id");
String vocab3Id = (String) request.getAttribute("vocab3Id");
VocabularyEntry vocab1  = ((VocabularyEntry) renderRequest.getAttribute("vocab1"));
String vocabName2 = (String) request.getAttribute("vocabName2");
String vocabName3 = (String) request.getAttribute("vocabName3");

String othersLabel = (String) window.getProperty("osivia.othersLabel");
%>

<script type="text/javascript">
	function submitVocabsForm(selectIndex, formId){
		var form = document.forms[formId]; 
		// RAZ des champs des sélecteurs suivants si ils ont été renseignés 
		for(var index = selectIndex + 1; index < 4; index++){
			var indexString = index.toString();
			var input = form.elements["vocab" + indexString + "Id"];
			if(typeof(input) != 'undefined'){
				input.value = '';
			}
		}
		form.submit();
		return false;
	}
</script>

<%
if( libelle != null)	{
%><span class="selector-libelle"><%= libelle %></span> <%	
}
%>


<%

List<String> vocabsId = (List<String>) renderRequest.getAttribute("vocabsId");

boolean selectorMultiValued = "1".equals(renderRequest.getAttribute("selectorMultiValued"));

if(selectorMultiValued){

	if( vocabsId.size() > 0) 	{
	%>
	
	<table class="nuxeo-keywords-selector-table"  cellspacing="5" width="95%">
	
	<%
	int occ = 0;
	
	for (String vocabId : vocabsId){
		
			String sOcc = Integer.toString(occ++);
			
		%>
			<tr>
				<td width="90%"><%= VocabSelectorPortlet.getLabel(othersLabel, vocabId, vocab1 )%> </td> <td>
				
				<a href="<portlet:actionURL>
		         		<portlet:param name="action" value="delete"/>
		         		<portlet:param name="occ" value="<%= sOcc %>"/>
		         		<portlet:param name="vocab1Id" value="<%= (vocab1Id != null) ? vocab1Id : "" %>"/>
		         		<portlet:param name="vocab2Id" value="<%= (vocab2Id != null) ? vocab2Id : ""  %>"/>
		         		<portlet:param name="vocab3Id" value="<%= (vocab3Id != null) ? vocab3Id : ""  %>"/>
		         </portlet:actionURL>"><img src="<%= renderRequest.getContextPath() %>/img/delete.gif" border="0"/></a>
				
				</td width="10%">
			</tr>
		<%	

		}
	%>
	
	
	</table>
	
	<%			
	}	
} 

String onChangeEvent1 = "";
if( vocabName2 != null) { 
	// URL de rafraichissement de la liste
	PortletURL refreshURL = renderResponse.createRenderURL();
	refreshURL.setParameter("vocab1Id", "SELECTED_VALUE");
	
	/* Pour faire apparaître le deuxième sélecteur (si il existe)
	 * en cas de choix sur le premier
	 * sélecteur.
	 */
	onChangeEvent1 = "onchange=\"refreshOnVocabularyChange(this,'"+ refreshURL +"');\""; 
	if(!selectorMultiValued) 
		onChangeEvent1 = "onchange=\"submitVocabsForm(1, this.form.id);\"";
		
	String other1Selected = "";
	if(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(vocab1Id))
		other1Selected = "selected='selected'";

%>
	
<div class="nuxeo-keywords-selector">
		<form id="<portlet:namespace />vocabsForm" method="post" action="<portlet:actionURL/>">
				
						<select id="<portlet:namespace />Topic" name="vocab1Id" style="width: 80%" <%= onChangeEvent1 %> >
							<option value="">Tous</option>					
			<% 								
						for(VocabularyEntry possible : vocab1.getChildren().values()){
								
							if( possible.getId().equals(vocab1Id)){
			%>
								<option selected="selected" value="<%= possible.getId() %>"><%= possible.getLabel() %></option>
			<%
							}else{
			%>
								<option value="<%= possible.getId() %>"><%= possible.getLabel() %></option>
			<%						
							}
						}
						if(StringUtils.isNotEmpty(othersLabel)){
			%>
							<option <%= other1Selected %> value="<%= VocabSelectorPortlet.OTHER_ENTRIES_CHOICE %>"><%= othersLabel %></option>
			<%          } %>
						</select> 
			
<% } 


VocabularyEntry vocab2 = null;

if(StringUtils.isNotEmpty(vocab1Id) && vocabName2 != null)	{
	vocab2  = vocab1.getChild(vocab1Id);
	if( vocab2 != null)	{	
		
		PortletURL refreshURL2 = renderResponse.createRenderURL();
		refreshURL2.setParameter("vocab1Id", vocab1Id);
		refreshURL2.setParameter("vocab2Id", "SELECTED_VALUE");
		
		String onChangeEvent2 = "";
		if( vocabName2 != null) {
			onChangeEvent2 = "onchange=\"refreshOnVocabularyChange(this,'"+ refreshURL2 +"');\"";
		 }	
		
		if( vocabName2 != null && !selectorMultiValued) 
				onChangeEvent2 = "onchange=\"submitVocabsForm(2, this.form.id);\"";
				
		String other2Selected = "";
		if(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(vocab2Id))
			other2Selected = "selected='selected'";
		
%>

						<select id="<portlet:namespace />Topic" name="vocab2Id" style="width: 80%" <%= onChangeEvent2 %> >
							<option value="">Tous</option>
			<%	
						
						for(VocabularyEntry possible : vocab2.getChildren().values()){
							
							if( possible.getId().equals(vocab2Id)){
			%>
								<option selected="selected" value="<%= possible.getId() %>"><%= possible.getLabel() %></option>
			<%
							}else{
			%>
								<option value="<%= possible.getId() %>"><%= possible.getLabel() %></option>
			<%						
								}
						}
						if(StringUtils.isNotEmpty(othersLabel)){
			%>
							<option <%= other2Selected %> value="<%= VocabSelectorPortlet.OTHER_ENTRIES_CHOICE %>"><%= othersLabel %></option>
			<%          } %>
			
						</select> 
<%
		}
	}
%>




<%

if(StringUtils.isNotEmpty(vocab1Id) && StringUtils.isNotEmpty(vocab2Id) && vocabName3 != null)	{
	VocabularyEntry vocab3  = vocab2.getChild(vocab2Id);
	if( vocab3 != null)	{	
		
		String onChangeEvent3 = "";
		if(!selectorMultiValued){
			onChangeEvent3 = "onchange=\"submitVocabsForm(3, this.form.id);\"";
		}
		
		String other3Selected = "";
		if(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(vocab3Id))
			other3Selected = "selected='selected'";
		
%>

						<select id="<portlet:namespace />Topic" name="vocab3Id" style="width: 80%" <%= onChangeEvent3 %>>
							<option value="">Tous</option>
			<%
						
						for(VocabularyEntry possible : vocab3.getChildren().values()){
							
							if( possible.getId().equals(vocab3Id)){
			%>
								<option selected="selected" value="<%= possible.getId() %>"><%= possible.getLabel() %></option>
			<%
							}else{
			%>
								<option value="<%= possible.getId() %>"><%= possible.getLabel() %></option>
			<%						
							}
						}
			
						if(StringUtils.isNotEmpty(othersLabel)){
			%>
							<option <%= other3Selected %> value="<%= VocabSelectorPortlet.OTHER_ENTRIES_CHOICE %>"><%= othersLabel %></option>
			<%          } %>
			
						</select>	 
						
<%
		}
	} 

if(selectorMultiValued){	
%>  
						<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" /> 
							    					
<% } else { %>	
	<input type="hidden" name="monovaluedSubmit" value="monovaluedSubmit"/>
<% } %>
			
	</form>		
		
</div>			
		
	