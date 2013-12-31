
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.FileBrowserPortlet"%>
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


Document folderDoc = (Document) request.getAttribute("doc");

%>

<div class="nuxeo-file-browser no-ajax-link">

<jsp:include page="add-content.jsp"></jsp:include>


	
<div class="separateur"></div>	

<p class="nuxeo-file-browser-description"><%=Formater.formatDescription(folderDoc)%></p>

<table class="nuxeo-file-browser-table"  cellspacing="0" width="100%">


<tr align="left">
	<th width="3%">&nbsp;</th>
	<th width="55%">Nom</th>
	<th width="15%">Date</th>			
	<th width="20%">Dernier contributeur</th>	
</tr>	

<%

Iterator it = docs.iterator();
while( it.hasNext())	{
	Document doc = (Document) it.next();
	
	String lastContributor = doc.getProperties().getString("dc:lastContributor");
	lastContributor = lastContributor != null ? lastContributor : "";
	String url = null;
	String downloadFileUrl = null;
	Link link = null;
	Link downloadFileLink = null;
	String icon = Formater.formatNuxeoIcon(doc);
	String target = "";	
	String downloadFileTarget = "";
	

	ResourceURL actionsURL = renderResponse.createResourceURL();
	actionsURL.setParameter("type", "fileActions");
	actionsURL.setResourceID(doc.getId());
	String actionsMenuURL = actionsURL.toString();

	String actionId = "actions"+ renderResponse.getNamespace() + doc.getId();

	
	link = ctx.getLink(doc,"fileExplorer");
	url = link.getUrl();
	target = Formater.formatTarget(link);
	
	if("File".equals(doc.getType())){
		downloadFileLink = ctx.getLink(doc,"download");
		downloadFileUrl = downloadFileLink.getUrl();
		downloadFileTarget = Formater.formatTarget(downloadFileLink);
	}

	 
	icon = "<img style=\"vertical-align:middle\" src=\""+renderRequest.getContextPath()+icon+"\">";					
%>


		<tr class="file-item" align="left"> 
			<td> 
				<%=icon%>
			</td> 
			
						
			<td>
	
				<a <%=target%> href="<%=url%>">  <%=doc.getTitle()%> </a>
<% if(!"".equalsIgnoreCase(Formater.formatSize(doc))){ %>
				&nbsp;(<%= Formater.formatSize(doc) %>)
<% } %>
<% if(downloadFileLink != null) {%>
				<a <%=downloadFileTarget%> href="<%=downloadFileUrl%>"><img src="<%=renderRequest.getContextPath()%>/img/download-vert-small.png" border="0"></a>
<% } %>

				<div class="file-actions" onclick="getFileActions('<%= actionsMenuURL %>', '<%= actionId %>');">   <div class="file-actions-menu" id="<%= actionId %>">  <div class="ajax-waiting" > </div> </div> </div>				
			</td>
			
			<td>
				<%= Formater.formatDateAndTime(doc) %>
			</td>
			
			<td>
				<div class="doc-lastContributor"><%= lastContributor %></div>
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


<div id="div_delete_file-item" style="display: none">
	<jsp:include page="confirm-delete-item.jsp"></jsp:include>
</div>


