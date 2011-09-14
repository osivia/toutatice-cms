
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.selectors.VocabSelectorPortlet"%>
<%@page import="java.util.Map"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.PageSelectors"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyEntry"%>

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
String vocab1Id = (String) request.getAttribute("vocab1Id");
String vocab2Id = (String) request.getAttribute("vocab2Id");
String vocab3Id = (String) request.getAttribute("vocab3Id");
VocabularyEntry vocab1  = ((VocabularyEntry) renderRequest.getAttribute("vocab1"));
String vocabName2 = (String) request.getAttribute("vocabName2");
String vocabName3 = (String) request.getAttribute("vocabName3");
%>

<%

List<String> vocabsId = (List<String>) renderRequest.getAttribute("vocabsId");
if( vocabsId.size() > 0) 	{
%>

<table class="nuxeo-keywords-selector-table"  cellspacing="5" width="95%">

<%
int occ = 0;

for (String vocabId : vocabsId){
	
	String sOcc = Integer.toString(occ++);
	
%>
	<tr>
		<td width="90%"><%= VocabSelectorPortlet.getLabel("", vocabId, vocab1 )%> </td> <td>
		
		<a href="<portlet:actionURL>
         		<portlet:param name="action" value="delete"/>
         		<portlet:param name="occ" value="<%= sOcc %>"/>
         </portlet:actionURL>"><img src="<%= renderRequest.getContextPath() %>/img/delete.gif" border="0"/></a>
		
		</td width="10%">
	</tr>
<%			
	}
%>


</table>

<%			
}
%>


<%


String onChangeEvent1 = "";
if( vocabName2 != null) {
	// URL de rafrichissement de la liste
	PortletURL refreshURL = renderResponse.createRenderURL();
	refreshURL.setParameter("vocab1Id", "SELECTED_VALUE");
	
	onChangeEvent1 = "onchange=\"refreshOnVocabularyChange(this,'"+ refreshURL +"');\"";
 }
%>

<div class="nuxeo-keywords-selector">
		<form method="post" action="<portlet:actionURL/>">
			
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
			%>
			
						</select> 
						
<%
VocabularyEntry vocab2 = null;

if( vocab1Id != null && vocabName2 != null)	{
	vocab2  = vocab1.getChild(vocab1Id);
	if( vocab2 != null)	{	
		
		PortletURL refreshURL2 = renderResponse.createRenderURL();
		refreshURL2.setParameter("vocab1Id", vocab1Id);
		refreshURL2.setParameter("vocab2Id", "SELECTED_VALUE");
		
		String onChangeEvent2 = "";
		if( vocabName2 != null) {
			onChangeEvent2 = "onchange=\"refreshOnVocabularyChange(this,'"+ refreshURL2 +"');\"";
		 }		
		
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
			%>
			
						</select> 
<%
		}
	}
%>




<%
if( vocab1Id != null && vocab2Id != null && vocabName3 != null)	{
	VocabularyEntry vocab3  = vocab2.getChild(vocab2Id);
	if( vocab3 != null)	{		
		
%>

						<select id="<portlet:namespace />Topic" name="vocab3Id" style="width: 80%" >
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
			%>
			
						</select> 
<%
		}
	}
%>


						
        
					
			<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" /> 
			
			
			
			
		</form>			
			

			
		
	</div>
	