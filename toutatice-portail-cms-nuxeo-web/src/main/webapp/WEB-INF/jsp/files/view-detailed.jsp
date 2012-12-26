
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


<portlet:defineObjects />

<%
Documents docs = (Documents) renderRequest.getAttribute("docs")	;
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String basePath = (String) request.getAttribute("basePath");
String folderPath = (String) request.getAttribute("folderPath");
String displayMode = (String) request.getAttribute("displayMode");

%>

<div class="nuxeo-file-browser">

<%@ include file="header.jsp" %>
	

	<div class="nuxeo-file-list">
	
		<div class="no-ajax-link"> 


<%
int indice = 0;
int parite = 0;
Iterator it = docs.iterator();
while( it.hasNext())	{

	indice++;
	parite = indice % 2;	
	Document doc = (Document) it.next();

	String url = null;
	Link link = null;
	String bigIcon = Formater.formatNuxeoBigIcon(doc);
	String target = "";	
	
	 if( FileBrowserPortlet.isNavigable(doc))	{
		PortletURL folderURL = renderResponse.createRenderURL();
		folderURL.setParameter("folderPath", doc.getPath());
		if( displayMode != null)
			folderURL.setParameter("displayMode", displayMode);
		url = folderURL.toString();
		link = new Link(url, false);
	}	else {
		link = ctx.getLink(doc, "fileExplorer");
		url = link.getUrl();
		target = Formater.formatTarget(link);
	}
	 
	 bigIcon = "<img src=\""+renderRequest.getContextPath()+bigIcon+"\">";					


%>	
	<div class="separateur"></div>	
	
	<div class="file-detail ligne<%=parite%>">
	
<%				
		String srcVignette = "";
		PropertyMap map = doc.getProperties().getMap("ttc:vignette");
		if( map != null && map.getString("data") != null)	
			srcVignette = "<img src=\""+ ctx.createFileLink(doc, "ttc:vignette") + "\" />";
		else
			srcVignette = bigIcon;	
%>				
					<div class="vignette"> <%= srcVignette %> </div> 	
					
					<div class="main">
						<div class="title">
							<a  <%=target%> href="<%=url%>">  <%=doc.getTitle()%> </a>
						</div>
						<p class="description">
								<%= Formater.formatDescription(doc) %> &nbsp;
						</p> 
					</div>
						
					<div class="detail">
						
						<dl>
							<dt>Modifié le</dt>
							<dd><%= Formater.formatDateAndTime(doc) %><br/></dd>
							<dt>Type</dt>
							<dd><%= Formater.formatType(doc) %><br/></dd>
<%			
		String size =  Formater.formatSize(doc);
		if( size.length() > 0)	{	%>
							<dt>Taille</dt>
							<dd><%= size %><br/></dd>
							
<%		}	%>							
						</dl>		
					</div>				
	</div>



<% 	} %>

		</div>

	</div>

</div>



<!--
<p align="center">
		scope	<%=  ctx.getScope() %> <br/>
</p>
-->