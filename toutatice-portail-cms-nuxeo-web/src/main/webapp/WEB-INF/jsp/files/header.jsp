
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="javax.portlet.WindowState"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.files.FileBrowserPortlet"%><div class="header">

<div class="path">
<%	
List breadcrumbs = (List) renderRequest.getAttribute("breadcrumbs")	;
String displayModeHeader = (String) request.getAttribute("displayMode");
String folderPathHeader = (String) request.getAttribute("folderPath");


Iterator itb = breadcrumbs.iterator();
while( itb.hasNext())	{
	Document doc = (Document) itb.next();

	String url = null;
	PortletURL folderURL = renderResponse.createRenderURL();
	folderURL.setParameter("folderPath", doc.getPath());
	if( displayModeHeader != null)
		folderURL.setParameter("displayMode", displayModeHeader);
	url = folderURL.toString();
%>
&gt; <a  href="<%=url%>"><%=doc.getTitle()%> </a>
<%	
	}
	
%>	
</div>


<script type="text/javascript">
function selectMode( form)
{

	parent.location.href = form.list.value;
}
</script>

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

</div>