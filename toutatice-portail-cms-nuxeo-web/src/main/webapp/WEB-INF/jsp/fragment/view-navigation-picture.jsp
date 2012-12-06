
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.fragment.FragmentType"%>
<%@page import="fr.toutatice.portail.api.urls.IPortalUrlFactory"%>
<%@page import="fr.toutatice.portail.api.menubar.MenubarItem"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.document.ViewDocumentPortlet"%>
<%@page import="fr.toutatice.portail.api.urls.Link"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>



<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Blob"%>


<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.Document"%>
<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList"%>


<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.StringHelper"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater"%>

<%@page import="org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap"%>

<portlet:defineObjects />

<%

NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx")	;

String navigationPageTemplate = (String) renderRequest.getAttribute("navigationPageTemplate");

%>


<% if( navigationPageTemplate != null) { %>

<%= navigationPageTemplate %>

<%	}	%>




