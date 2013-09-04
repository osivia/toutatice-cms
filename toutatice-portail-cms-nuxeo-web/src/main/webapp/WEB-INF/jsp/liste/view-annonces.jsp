<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>
<%@page import="java.text.Format"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="org.osivia.portal.api.urls.Link"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.model.PropertyMap"%>

<%@ page contentType="text/plain; charset=UTF-8"%>

<portlet:defineObjects />

<%
	NuxeoController ctx = (NuxeoController) renderRequest
			.getAttribute("ctx");

	Document doc = (Document) renderRequest.getAttribute("doc");
	int parite = (Integer) renderRequest.getAttribute("parite");

	//Link link = ctx.getLink(doc);


	String title = Formater.formatTitle(doc);
	String extrait = Formater.formatExtrait(doc);


%>

<hr />

<li class="item<%=parite%>"><h3><%=title%></h3>
	<p class="extrait"><%=extrait%></p>
</li>