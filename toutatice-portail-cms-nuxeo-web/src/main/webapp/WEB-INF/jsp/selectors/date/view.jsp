<%@ page contentType="text/plain; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.selectors.DateSelectorPortlet"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>

<%@ page isELIgnored="false" %>

<portlet:defineObjects />

<script>
	var $JQry = jQuery.noConflict();

	$JQry(function() {
		var dates = $JQry( "#datefrom, #dateto" ).datepicker({
			defaultDate: "+1w",
			changeMonth: true,
			numberOfMonths: 1,
			dateFormat: 'dd/mm/yy',
			onSelect: function( selectedDate ) {
				var option = this.id == "datefrom" ? "minDate" : "maxDate",
					instance = $JQry( this ).data( "datepicker" ),
					date = $JQry.datepicker.parseDate(
						instance.settings.dateFormat ||
						$JQry.datepicker._defaults.dateFormat,
						selectedDate, instance.settings );
				dates.not( this ).datepicker( "option", option, date );
			}
		});
	});
</script>

<%

String libelle = (String) request.getAttribute("libelle");

if( libelle != null)	{
%><span class="selector-libelle"><%= libelle %></span> <%	
}

List<String> dates = (List<String>) renderRequest.getAttribute("dates");

if(! "1".equals(renderRequest.getAttribute("datesMonoValued"))){
	
	if( dates != null && dates.size() > 0) 	
	{ %>
		<table class="nuxeo-keywords-selector-table"  cellspacing="5" width="95%">
	<%
		int occ = 0;
		for(String interval : dates){
			String sOcc = Integer.toString(occ++);
			
			String[] decomposedInterval = interval.split(DateSelectorPortlet.DATES_SEPARATOR);
			
			String dateFrom = decomposedInterval[0];
			String dateTo = decomposedInterval[1];		
			%>
				<tr>
					<td width="90%">Du <%=dateFrom%> au <%=dateTo%> </td> 
					<td  width="10%">
					<a href="<portlet:actionURL>
			         		<portlet:param name="action" value="delete"/>
			         		<portlet:param name="occ" value="<%= sOcc %>"/>
			         </portlet:actionURL>"><img src="<%= renderRequest.getContextPath() %>/img/delete.gif" border="0"/></a>
					</td>
				</tr>
		
			<%	
		}%>
		</table>
	<%
	} 
	%>
	
	<div class="nuxeo-keywords-selector">
		<form method="post" action="<portlet:actionURL/>">
			<input type="text" id="datefrom" name="datefrom" value="${dateFrom}" size="10">	
			<input type="text" id="dateto" name="dateto" value="${dateTo}" size="10">	
			<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" > 
		</form>				
	</div>

<% } else { 


	String dateFrom = "";
	String dateTo = "";	
	if(dates != null && dates.size() == 1){
		String[] interval = dates.get(0).split(DateSelectorPortlet.DATES_SEPARATOR);
		dateFrom = interval[0];
		dateTo = interval[1];	
	}
	
%>

	<div class="nuxeo-keywords-selector">
		<form method="post" action="<portlet:actionURL/>">
			<input type="text" id="datefrom" name="datefrom" value="<%= dateFrom %>" size="10">	
			<input type="text" id="dateto" name="dateto" value="<%= dateTo %>" size="10">	
			<input border=0 width="16px" height="16px" src="<%= renderRequest.getContextPath() %>/img/submit.jpg" name="monoAdd" type="image" value="submit" align="middle" > 
			<a style="vertical-align:bottom;" href="<portlet:actionURL>
			 		<portlet:param name="action" value="delete"/>
			 		<portlet:param name="occ" value="0"/>
			     </portlet:actionURL>"><img src="<%= renderRequest.getContextPath() %>/img/delete.gif" border="0"/>
			</a>	
		</form>			
	</div>

<% } %>
	