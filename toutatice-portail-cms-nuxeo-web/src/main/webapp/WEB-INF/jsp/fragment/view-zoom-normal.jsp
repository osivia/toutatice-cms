
<%@page import="fr.toutatice.portail.cms.nuxeo.service.editablewindow.Link"%>
<%@page import="org.osivia.portal.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>

<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>


<%@page import="javax.portlet.PortletURL"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>


<portlet:defineObjects />

<%
	NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

Document doc = (Document) renderRequest.getAttribute("doc");
Link zoom = (Link) renderRequest.getAttribute("zoom");
%>
		<li> <div class="list-bloc"> <a href="<%=zoom.getHref()%>"><%=zoom.getTitle()%></a>
		</div></li>	