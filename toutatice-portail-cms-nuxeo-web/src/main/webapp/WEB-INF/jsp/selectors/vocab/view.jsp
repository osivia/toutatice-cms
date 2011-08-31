
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
VocabularyEntry vocab1  = ((VocabularyEntry) renderRequest.getAttribute("vocab1"));
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
		<td width="90%"><%= vocabId%> </td> <td>
		
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

<div class="nuxeo-keywords-selector">
		<form method="post" action="<portlet:actionURL/>">
			
						<select id="<portlet:namespace />Topic" name="vocab1Id" style="width: 80%;">
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
					
			<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" > 
		</form>			
			

			
		
	</div>
	