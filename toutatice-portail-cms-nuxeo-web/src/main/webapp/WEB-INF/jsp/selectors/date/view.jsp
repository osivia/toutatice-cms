<%@ page contentType="text/plain; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.selectors.DateSelectorPortlet"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>

<%@ page isELIgnored="false" %>

<portlet:defineObjects />

<%
List<String> dates = (List<String>) renderRequest.getAttribute("dates");
if( dates != null && dates.size() > 0) 	
{
	String dateFrom = (String) dates.get(0);
	String dateTo = (String) dates.get(1);		
	%>
	<table class="nuxeo-keywords-selector-table"  cellspacing="5" width="95%">
		<tr>
			<td width="90%">Du <%=dateFrom%> au <%=dateTo%> </td> 
			<td  width="10%">
			<a href="<portlet:actionURL>
	         		<portlet:param name="action" value="delete"/>
	         </portlet:actionURL>"><img src="<%= renderRequest.getContextPath() %>/img/delete.gif" border="0"/></a>
			</td>
		</tr>

	</table>
	<%			
}
%>

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

<div class="nuxeo-keywords-selector">
	<form method="post" action="<portlet:actionURL/>">
		<input type="text" id="datefrom" name="datefrom" value="${dateFrom}" size="10">	
		<input type="text" id="dateto" name="dateto" value="${dateTo}" size="10">	
		<input border=0 src="<%= renderRequest.getContextPath() %>/img/add.gif" name="add" type="image" value="submit" align="middle" > 
	</form>				
</div>
	