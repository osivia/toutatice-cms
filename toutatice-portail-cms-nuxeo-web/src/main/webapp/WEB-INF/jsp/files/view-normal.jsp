
<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.osivia.portal.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>


<%@page import="org.nuxeo.ecm.automation.client.model.Documents"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>




<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%>
<portlet:defineObjects />

<%
List<Document> docs = (List<Document>) renderRequest.getAttribute("docs")	;
NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;
String basePath = (String) request.getAttribute("basePath");
String folderPath = (String) request.getAttribute("folderPath");
String displayMode = (String) request.getAttribute("displayMode");
//v2.0-SP1 : lien contextualisés
String cmsLink = (String) request.getAttribute("cmsLink");

%>

<div class="nuxeo-file-browser">


<%@ include file="header.jsp" %>

	
<div class="separateur"></div>	

<table class="nuxeo-file-browser-table"  cellspacing="0" width="100%">


<tr align="left">
	<th width="3%">&nbsp;</th>
	<th width="25%">Nom</th>
	<th width="15%">Date</th>	
	<th width="5%">Actions</th>		
	<th width="45%">&nbsp;</th>	
</tr>	


<%

Iterator it = docs.iterator();
while( it.hasNext())	{
	Document doc = (Document) it.next();

	String url = null;
	String downloadFileUrl = null;
	Link link = null;
	Link downloadFileLink = null;
	String icon = Formater.formatNuxeoIcon(doc);
	String target = "";	
	String downloadFileTarget = "";
	boolean noAjax = true;
	

	ResourceURL actionsURL = renderResponse.createResourceURL();
	actionsURL.setParameter("type", "fileActions");
	actionsURL.setResourceID(doc.getId());
	String actionsMenuURL = actionsURL.toString();

	String actionId = "actions"+ renderResponse.getNamespace() + doc.getId();

	
	
	 if(  ! "1".equals(cmsLink) && 	 FileBrowserPortlet.isNavigable( doc) )	{
		PortletURL folderURL = renderResponse.createRenderURL();
		folderURL.setParameter("folderPath", doc.getPath());
		if( displayMode != null)
			folderURL.setParameter("displayMode", displayMode);
	
		url = folderURL.toString();
		link = new Link(url, false);
		
		// le mode ajax n'est autorise que pour les folders en mode NORMAL
		if( WindowState.NORMAL.equals(renderRequest.getWindowState()))
			noAjax = false;
			

	}	else {
		link = ctx.getLink(doc,"fileExplorer");
		url = link.getUrl();
		target = Formater.formatTarget(link);
		
		if("File".equals(doc.getType())){
			downloadFileLink = ctx.getLink(doc,"downloableFile");
			downloadFileUrl = downloadFileLink.getUrl();
			downloadFileTarget = Formater.formatTarget(downloadFileLink);
		}
	}
	 
	icon = "<img style=\"vertical-align:middle\" src=\""+renderRequest.getContextPath()+icon+"\">";					
%>


		<tr class="file-item" align="left"> 
			<td> 
				<%=icon%>
			</td> 
			
						
			<td>
<% if (noAjax)		{ %> 
	 <div class="no-ajax-link file-name"> 
<% }	%>			
				<a <%=target%> href="<%=url%>">  <%=doc.getTitle()%> </a>
<% if(!"".equalsIgnoreCase(Formater.formatSize(doc))){ %>
				&nbsp;(<%= Formater.formatSize(doc) %>)
<% } %>
<% if(downloadFileLink != null) {%>
				<a <%=downloadFileTarget%> href="<%=downloadFileUrl%>"><img src="<%=renderRequest.getContextPath()%>/img/download-vert-small.png" border="0"></a>
<% } %>


<%
   if (noAjax){ %> 
	 </div> 
<% }	%>					
			</td>
			
			<td>
				<%= Formater.formatDateAndTime(doc) %>
			</td>

			<td>
				 <div class="file-actions" onclick="getFileActions('<%= actionsMenuURL %>', '<%= actionId %>');">   <div class="file-actions-menu" id="<%= actionId %>">  <div class="ajax-waiting" > </div> </div> </div>
			</td>
			
			<td>
			</td>

		
		</tr>
		

<%
	}

%>
</table>

<script>
	var $JQry = jQuery.noConflict();

	$JQry(document).ready(function() {
		$JQry(".file-item").hover(function () {
   			 $JQry(this).addClass("file-item-selected");

             $JQry(".file-actions-menu").css("visibility", "hidden");

   			 var file = Element.down(this, "div.file-actions");             
     		 $JQry(file).css("visibility","visible"); 			 
   		}, function() {
  			 $JQry(this).removeClass("file-item-selected");
             
  			 var file = Element.down(this, "div.file-actions");
     		 $JQry(file).css("visibility", "hidden"); 		
 			 }
 		);
 		
        $JQry(".file-actions-menu").mouseleave(function() {
            $JQry(this).css("visibility","hidden");
        });
	});
</script>
	

</div>

<!-- Modif-FILEBROWSER-begin -->
<div id="div_delete_file-item" style="display: none">
	<jsp:include page="confirm-delete-item.jsp"></jsp:include>
</div>
<!-- Modif-FILEBROWSER-end -->

<!--
<p align="center">
		scope	<%=  ctx.getScope()  %> <br/>
</p>
-->