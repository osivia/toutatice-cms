<%@ page contentType="text/plain; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Documents"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.TransformationContext"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.ViewContentLink"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments"%><portlet:defineObjects />

<%
PaginableDocuments docs = (PaginableDocuments) renderRequest.getAttribute("docs")	;
TransformationContext ctx = (TransformationContext) renderRequest.getAttribute("ctx")	;
String style = (String) renderRequest.getAttribute("style")	;
int currentPage = (Integer) request.getAttribute("currentPage");
int nbPages = (Integer) renderRequest.getAttribute("nbPages")	;

String permaLinkURL = (String) renderRequest.getAttribute("permaLinkURL")	;

%>


<div class="nuxeo-list-<%=style%>">

<%
if( permaLinkURL != null)	{
%>
	<div class="nuxeo-list-permalink"><a href="#" onclick="alert('<%= permaLinkURL %>');return false">Permalien</a></div>
<%
}
%>

	<ul>
<%
int indice = 0;
int parite = 0;
Iterator it = docs.iterator();
while( it.hasNext())	
{
	indice++;
	parite = indice % 2;
	Document doc = (Document) it.next();
	
	ViewContentLink link = ctx.createLink(doc);
	String url = link.getUrl();
	String target = Formater.formatTarget(link);
	String icon = Formater.formatNuxeoIcon(doc);
	
	icon = "<img class=\"icon\" src=\""+renderRequest.getContextPath()+icon+"\">";
		
	if( ViewListPortlet.STYLE_MINI.equals(style))	
	{%>
		<li class="item<%=parite%>">  <a <%=target%> href="<%=url%>"><%=doc.getTitle()%> </a>  </li><%			
	}
	if( ViewListPortlet.STYLE_NORMAL.equals(style))	
	{%>
		<li class="item<%=parite%>">  <%= icon%> <a <%=target%> href="<%=url%>"><span><%=doc.getTitle()%></span> </a> </li><%			
	} 
	else if( ViewListPortlet.STYLE_DETAILED.equals(style)) 
	{	
		String detail = "";
		detail = Formater.formatDate( doc);
		
		String size = Formater.formatSize( doc);
		if( size.length() > 0)
			detail += " - " + size;
%>
		<li class="item<%=parite%>">  <%= icon%> <a <%=target%> href="<%=url%>"><span><%=doc.getTitle()%> </span></a><p class="description"><%= Formater.formatDescription(doc)%></p> <p class="detail"><%=detail%></p><div class="separateur"></div></li>
<%			
	} 
	else if( ViewListPortlet.STYLE_NEWS.equals(style))	
	{%>
		<li> <div class="list-bloc">  <%= icon%> <a class="title" <%=target%> href="<%=url%>"><span><%=doc.getTitle()%></span> </a><p class="list-date"><%= Formater.formatDateAndTime(doc)%></p></div></li><%			
	} 
	else if( ViewListPortlet.STYLE_EDITORIAL.equals(style)) 
	{	
		String srcVignette = "";
		PropertyMap map = doc.getProperties().getMap("ttc:vignette");
		
		if( map != null && map.getString("data") != null)	
			srcVignette = "<div class=\"vignette\"><img class=\"vignette\" src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" /></div>";
		else
			srcVignette = "<div class=\"vignette-vide\"> </div>";
			
		
%>
		<li class="item<%=parite%>">
			<%=srcVignette%>
			<a class="title" <%=target%> href="<%=url%>"><%=doc.getTitle()%> </a>
			<p class="description"><%= Formater.formatDescription(doc)%></p>
			<div class="separateur"></div>
		</li>
<%			
	} 

}
%>
	</ul>
<% 
if ( nbPages > 1 )	
{ %>	
	<div class="pagination">
	Page
<%

int minPage = Math.max(0, currentPage - 5);
int maxPage = Math.min(currentPage + 5, nbPages);

for( int numPage = minPage; numPage < maxPage; numPage++)	{
	PortletURL pageURL = renderResponse.createRenderURL();

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

<% } %>



</div>



<!-- JSS
<p align="center">
		scope	<%=  ctx.getScope() %> <br/>
</p>
-->
