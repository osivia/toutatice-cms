
<%@page import="org.osivia.portal.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>


<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Documents"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>



<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>




<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments"%><portlet:defineObjects />

<%
PaginableDocuments docs = (PaginableDocuments) renderRequest.getAttribute("docs")	;
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String keywords = (String) request.getAttribute("keywords");
int currentPage = (Integer) request.getAttribute("currentPage");
String hideSearchSubForm = (String) request.getAttribute("hideSearchSubForm");

%>

<portlet:defineObjects />


<div class="nuxeo-results-search">

<% if(!"1".equals(hideSearchSubForm)){ %>

	<div class="nuxeo-input-search">
			<form method="post" action="<portlet:actionURL/>">
				<input type="text" name="keywords" value="<%= keywords %>" size="40">	<input type="submit" value="Rechercher" name="searchAction"/>
			</form>
	</div>
	
<% } %>

<div class="nuxeo-nb-results_search">

	<%= docs.getTotalSize() %> <%= (docs.getTotalSize() <= 1) ? "résultat" : "résultats" %>

</div>


<div class="nuxeo-list-search">
	<ul>

<%

Iterator it = docs.iterator();
while( it.hasNext())	{
	
	Document doc = (Document) it.next();
	
	Link link = ctx.getLink(doc);
	

	String	icon = "<img style=\"vertical-align:middle\" src=\""+renderRequest.getContextPath()+Formater.formatSpecificIcon(doc)+"\">";
	String target = Formater.formatTarget(link);	
	String url = link.getUrl();
	
%>
		<li><%=icon%>  <a <%=target%> href="<%=url%>"><%=doc.getTitle()%></a>  <br><p class="description"><%= Formater.formatDescription(doc)%></p><div class="separateur"></div></li>

<%
}
%>
			</ul>
</div>


<div class="pagination">


	
Page 
<%

int minPage = Math.max(0, currentPage - 5);
int maxPage = Math.min(currentPage + 5, docs.getPageCount());

for( int numPage = minPage; numPage < maxPage; numPage++)	{
	PortletURL pageURL = renderResponse.createRenderURL();

	pageURL.setParameter("keywords", keywords);
	pageURL.setParameter( "currentPage", Integer.toString(numPage));
	
	if( currentPage == numPage)	{
%>
			<b><span><%= (numPage + 1) %></span></b>
<%	} else { %>
			<a href="<%= pageURL.toString()%>"><span><%= (numPage + 1) %></span></a>
<%	}  
}
%>
</div>


</div>



<%= ctx.getDebugInfos() %>
