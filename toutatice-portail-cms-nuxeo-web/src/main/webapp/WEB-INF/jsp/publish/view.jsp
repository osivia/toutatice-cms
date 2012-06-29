
<%@page import="java.io.IOException"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>




<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList"%>





<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.publish.NavigationDisplayItem"%><portlet:defineObjects />


<%!

public void displayItem(javax.servlet.jsp.JspWriter out, NavigationDisplayItem itemToDisplay, int level) throws IOException {
	
	
	if( level > 0)	{
		String target = "";
		if( itemToDisplay.isExternal())
			target = "target=\"_blank\"";
		
		String li = "<li";
		if( itemToDisplay.isSelected())
			li+=  " class=\"selected\"";
		li += ">";
		
		out.println(li);
		
		out.println("<a "+ target + "  href=\""+ itemToDisplay.getUrl() + "\">"+ itemToDisplay.getTitle()+ "</a>");
		
		out.println("</li>");
	}
	
	if( itemToDisplay.getChildrens().size() > 0)	{
		out.println("<ul>");
		
		for (NavigationDisplayItem child : itemToDisplay.getChildrens())
			displayItem( out, child, level +1);
		out.println("</ul>");
	}
	
}

%>



<%
NavigationDisplayItem itemToDisplay = (NavigationDisplayItem)  renderRequest.getAttribute("itemToDisplay")	;
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
%>


<div class="nuxeo-publish-navigation">
	<%	displayItem( out,  itemToDisplay, 0); %>
</div>	
		
					
<%= ctx.getDebugInfos() %>


