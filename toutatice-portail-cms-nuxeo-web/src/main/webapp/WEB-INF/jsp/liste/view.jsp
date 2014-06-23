<%@page import="org.osivia.portal.api.Constants"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.osivia.portal.api.menubar.MenubarItem"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>

<%@ page contentType="text/plain; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Documents"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%>

<%@page import="org.nuxeo.ecm.automation.client.model.PaginableDocuments"%><portlet:defineObjects />

<jsp:include page="../files/add-content.jsp"></jsp:include>

<%
PaginableDocuments docs = (PaginableDocuments) renderRequest.getAttribute("docs")	;
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String style = (String) renderRequest.getAttribute("style")	;
int currentPage = (Integer) request.getAttribute("currentPage");
int nbPages = (Integer) renderRequest.getAttribute("nbPages")	;

String permaLinkURL = (String) renderRequest.getAttribute("permaLinkURL")	;
String rssLinkURL = (String) renderRequest.getAttribute("rssLinkURL")	;
String nuxeoRequest = (String) renderRequest.getAttribute("nuxeoRequest")	;
String selectors = (String) renderRequest.getAttribute("selectors")	;

%>


<div class="nuxeo-list-<%=style%>">


<%
// Menu items can be customized
// by jsp duplication

List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

if( permaLinkURL != null)	{
	MenubarItem item = new MenubarItem("PERMLINK", "Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC, permaLinkURL, null, "portlet-menuitem-permalink", null);
	item.setGlyphicon("link");
    item.setAjaxDisabled(true);
	menuBar.add(item);
}
%>

<%
if( rssLinkURL != null)	{

	MenubarItem item = new MenubarItem("RSS", "RSS", MenubarItem.ORDER_PORTLET_SPECIFIC + 2, rssLinkURL, null, "portlet-menuitem-rss", null);
	item.setAjaxDisabled(true);
	menuBar.add(item);	
}
%>

<% if( nuxeoRequest != null)	{ %>

		<div class="displayRequest" style="border: 1px; border-style: solid"> 
			<%= StringEscapeUtils.escapeHtml(nuxeoRequest) %>
		</div>

<%	} %>


<div class="no-ajax-link">
	<ul class="list-group">
<%
int indice = 0;
int parite = 0;
Iterator it = docs.iterator();
while( it.hasNext())	
{
	indice++;
	parite = indice % 2;
	Document doc = (Document) it.next();
	

		// Appel de la jsp
		
		String jspName = "view-"+ ViewListPortlet.getListTemplates().get(style).getKey().toLowerCase() + ".jsp";
		renderRequest.setAttribute("parite", parite);
		renderRequest.setAttribute("doc", doc);
		
%>
		<jsp:include page="<%= jspName %>"></jsp:include>
<%		


}
%>
	</ul>
	
</div>	
<% 
if ( nbPages > 1 )	
{ %>
<div class="text-center">	
	<ul class="pagination">
<%

int minPage = Math.max(0, currentPage - 5);
int maxPage = Math.min(currentPage + 5, nbPages);

for( int numPage = minPage; numPage < maxPage; numPage++)	{
	PortletURL pageURL = renderResponse.createRenderURL();

	pageURL.setParameter( "currentPage", Integer.toString(numPage));
	pageURL.setParameter( "currentState", renderRequest.getWindowState().toString());
	
	if( selectors != null)
		pageURL.setParameter( "lastSelectors", selectors);	
	
	if( currentPage == numPage)	{
%>
			<li class="active">
                <a href="#">
                    <span><%=(numPage + 1) %></span>
                    <span class="sr-only">(current)</span>
                </a>
            </li>
<%	} else { %>
			<!-- JMETER_URL_LIST="<%= pageURL.toString()%>" -->
            <li>
                <a href="<%=pageURL.toString() %>"><%=(numPage + 1) %></a>
            </li>
<%	}  
}
%>
	</ul>
</div>
<% } %>



	<jsp:include page="footer.jsp"></jsp:include>

</div>






