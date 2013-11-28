
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.SubType"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="org.osivia.portal.api.path.PortletPathItem"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page import="javax.portlet.WindowState"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.FileBrowserPortlet"%><div class="header">

<% if( WindowState.NORMAL.equals(renderRequest.getWindowState()))	{	

// Affichage du breadcrum en mode normal

%>

<div class="path"> 
<%	
List<PortletPathItem> portletPath = (List) renderRequest.getAttribute("portletPath")	;


Iterator itb = portletPath.iterator();
while( itb.hasNext())	{
	PortletPathItem pathItem = (PortletPathItem) itb.next();
	PortletURL folderURL = renderResponse.createRenderURL();
	
	Set<Map.Entry<String,String>> rps = pathItem.getRenderParams().entrySet();
	
	for(Map.Entry<String,String> rp:rps)	{
		folderURL.setParameter(rp.getKey(), rp.getValue());
	}
	String url = folderURL.toString();
	

%>

/ <a  href="<%=url%>"><%=pathItem.getLabel()%> </a>
<%	
	}
	
%>	
</div>
<% } %>	

<%	
String displayModeHeader = (String) request.getAttribute("displayMode");
String folderPathHeader = (String) request.getAttribute("folderPath");
%>


<script type="text/javascript">
function selectMode( form)
{

	parent.location.href = form.list.value;
}
</script>

<div class="path">

</div>

<% if( "1".equals(request.getAttribute("changeDisplayMode")))	{ %>

<div class="switch-display-mode">

<FORM NAME="switchMode">
	<SELECT NAME="list" onChange="selectMode(this.form)">
	
<%	

String selectedDetailed = "";
String selectedNormal = "selected=\"selected\"";
if( "detailed".equals( displayModeHeader))	{
	selectedNormal = "";
	selectedDetailed = "selected=\"selected\"";
}

PortletURL normalModeURL = renderResponse.createRenderURL();
if( folderPathHeader != null)
	normalModeURL.setParameter("folderPath", folderPathHeader);

normalModeURL.setParameter("displayMode", FileBrowserPortlet.DISPLAY_MODE_NORMAL);	
String normalURL = normalModeURL.toString();
%>		

		<OPTION VALUE="<%= normalURL%>" <%= selectedNormal%> >Affichage liste</OPTION>

	
	
<%	
PortletURL detailedModeURL = renderResponse.createRenderURL();
if( folderPathHeader != null)
	detailedModeURL.setParameter("folderPath", folderPathHeader);	
detailedModeURL.setParameter("displayMode", FileBrowserPortlet.DISPLAY_MODE_DETAILED);
detailedModeURL.setWindowState(WindowState.MAXIMIZED);
String detailedURL = detailedModeURL.toString();
%>
		<OPTION VALUE="<%= detailedURL%>" <%= selectedDetailed%>>Affichage détails</OPTION>
		
		
	</SELECT>
</FORM>
	
</div>

<%	} %>


</div>


<jsp:include page="add-content.jsp"></jsp:include>




