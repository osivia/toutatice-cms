package fr.toutatice.portail.core.nuxeo;

import java.net.URI;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

public class LinkHandlerCtx {
	PortletContext portletCtx;
	PortletRequest request;
	RenderResponse response;
	String scope;
	String displayLiveVersion;

	String pageId;
	URI nuxeoBaseURI;


	Document doc;
	
	public PortletRequest getRequest() {
		return request;
	}

	public void setRequest(PortletRequest request) {
		this.request = request;
	}

	public RenderResponse getResponse() {
		return response;
	}

	public void setResponse(RenderResponse response) {
		this.response = response;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getDisplayLiveVersion() {
		return displayLiveVersion;
	}

	public void setDisplayLiveVersion(String displayLiveVersion) {
		this.displayLiveVersion = displayLiveVersion;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}
	
	public PortletContext getPortletCtx() {
		return portletCtx;
	}
	
	public URI getNuxeoBaseUri() {
		return nuxeoBaseURI;
	}

	public LinkHandlerCtx(PortletContext portletCtx, PortletRequest request, RenderResponse response, String scope, String displayLiveVersion, String pageId, URI nuxeoBaseURI, Document doc) {
		super();
		this.portletCtx = portletCtx;
		this.request = request;
		this.response = response;
		this.scope = scope;
		this.displayLiveVersion = displayLiveVersion;
		this.pageId = pageId;
		this.nuxeoBaseURI = nuxeoBaseURI;
		this.doc = doc;
	}




}
