
<%@page import="fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.selectors.VocabSelectorPortlet"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Collection"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.PageSelectors"%>
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

String preselect1 = (String) request.getAttribute("preselect1");

String othersLabel = (String) window.getProperty("osivia.othersLabel");

if( libelle != null)	{
%><span class="selector-libelle"><%= libelle %></span> <%	
}
%>


<%

List<String> vocabsId = (List<String>) renderRequest.getAttribute("vocabsId");

boolean selectorMonoValued = "1".equals(renderRequest.getAttribute("selectorMonoValued"));

if(!selectorMonoValued){

	if( vocabsId.size() > 0) 	{
	%>
	
	<table class="nuxeo-keywords-selector-table"  cellspacing="5" width="95%">
	
	<%
	int occ = 0;
	
	for (String vocabId : vocabsId){
		
			String sOcc = Integer.toString(occ++);
			
		%>
			<tr>
                <td width="90%"><%= VocabSelectorPortlet.getLabel(othersLabel, vocabId, vocab1, preselect1 )%> </td> <td>
				
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
if(vocab1 != null && StringUtils.isNotEmpty(vocab1.getId())) { 
	
	
	/* Pour faire apparaître le deuxième sélecteur (si il existe)
	 * en cas de choix sur le premier
	 * sélecteur.
	 */ 
	if(selectorMonoValued) {
		
		PortletURL actionUrl = renderResponse.createActionURL();
		actionUrl.setParameter("vocab1Id", "SELECTED_VALUE");
		actionUrl.setParameter("vocab12d", "");
		actionUrl.setParameter("vocab13d", "");
		actionUrl.setParameter("monovaluedSubmit", "monovaluedSubmit");
		
		onChangeEvent1 = "onchange=\"refreshOnVocabularyChange(this,'"+ actionUrl +"');\"";
		
	} else {
		
		// URL de rafraichissement de la liste
		PortletURL refreshURL = renderResponse.createRenderURL();
		refreshURL.setParameter("vocab1Id", "SELECTED_VALUE");
		
		onChangeEvent1 = "onchange=\"refreshOnVocabularyChange(this,'"+ refreshURL +"');\"";		
	}
		
	String other1Selected = "";
	if(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(vocab1Id))
		other1Selected = "selected='selected'";

%>
	
<div class="nuxeo-keywords-selector">
		<form id="<portlet:namespace />vocabsForm" method="post" action="<portlet:actionURL/>">
        
<% if( preselect1 == null){ %>				
						<select id="<portlet:namespace />Topic" name="vocab1Id" <%= onChangeEvent1 %> >
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
						
						Collection<VocabularyEntry> children = vocab1.getChildren().values();
						if(StringUtils.isNotEmpty(othersLabel) && children != null && children.size() > 0){
			%>
							<option <%= other1Selected %> value="<%= VocabSelectorPortlet.OTHER_ENTRIES_CHOICE %>"><%= othersLabel %></option>
			<%          } %>
						</select> 
<%  }   %>			
<% } 


VocabularyEntry vocab2 = null;

if(StringUtils.isNotEmpty(vocab1Id) && vocabName2 != null)	{
	vocab2  = vocab1.getChild(vocab1Id);
	if( vocab2 != null)	{	
		
		String onChangeEvent2 = "";
		
		if(selectorMonoValued) {
			
			PortletURL actionUrl2 = renderResponse.createActionURL();
			actionUrl2.setParameter("vocab1Id", vocab1Id);
			actionUrl2.setParameter("vocab2Id", "SELECTED_VALUE");
			actionUrl2.setParameter("vocab3Id", "");
			actionUrl2.setParameter("monovaluedSubmit", "monovaluedSubmit");
			
			onChangeEvent2 = "onchange=\"refreshOnVocabularyChange(this,'"+ actionUrl2 +"');\"";
			
		} else {
			
			PortletURL refreshURL2 = renderResponse.createRenderURL();
			refreshURL2.setParameter("vocab1Id", vocab1Id);
			refreshURL2.setParameter("vocab2Id", "SELECTED_VALUE");
			
			onChangeEvent2 = "onchange=\"refreshOnVocabularyChange(this,'"+ refreshURL2 +"');\"";
		
		}
				
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

						Collection<VocabularyEntry> children = vocab2.getChildren().values();
						if(StringUtils.isNotEmpty(othersLabel) && children != null && children.size() > 0){
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
		
		if(selectorMonoValued) {
			
			PortletURL actionUrl3 = renderResponse.createActionURL();
			actionUrl3.setParameter("vocab1Id", vocab1Id);
			actionUrl3.setParameter("vocab2Id", vocab2Id);
			actionUrl3.setParameter("vocab3Id", "SELECTED_VALUE");
			actionUrl3.setParameter("monovaluedSubmit", "monovaluedSubmit");
			
			onChangeEvent3 = "onchange=\"refreshOnVocabularyChange(this,'"+ actionUrl3 +"');\"";
			
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
			
						Collection<VocabularyEntry> children = vocab3.getChildren().values();
						if(StringUtils.isNotEmpty(othersLabel) && children != null && children.size() > 0){
			%>
							<option <%= other3Selected %> value="<%= VocabSelectorPortlet.OTHER_ENTRIES_CHOICE %>"><%= othersLabel %></option>
			<%          } %>
			
						</select>	 
						
<%
		}
	} 

if(!selectorMonoValued){	
%>  
						<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" /> 
							    					
<% } else { %>	
	<!--<input type="hidden" name="monovaluedSubmit" value="monovaluedSubmit"/>-->
<% } %>
			
	</form>		
		
</div>			
		
	