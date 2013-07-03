package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.w3c.dom.Element;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;

public class RssGenerator {

	private static Log log = LogFactory.getLog(RssGenerator.class);

	public static org.w3c.dom.Document createDocument(NuxeoController ctx, PortalControllerContext portalCtx, String rssTitle,
			PaginableDocuments docs, String permLinkRef) throws Exception {

		/* Contexts and formaters initialization */

		HttpServletRequest request = ControllerContextAdapter.getControllerContext(portalCtx).getServerInvocation().getServerContext()
				.getClientRequest();

		SimpleDateFormat nuxeoFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		nuxeoFormater.setTimeZone(TimeZone.getTimeZone("GMT"));

		SimpleDateFormat rssDateFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		rssDateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));


		/* Generation XML */

		// Création d'une fabrique de documents
		DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

		// création d'un constructeur de documents
		DocumentBuilder constructeur = fabrique.newDocumentBuilder();

		org.w3c.dom.Document document = constructeur.newDocument();

		// Propriétés du DOM
		document.setXmlVersion("1.0");
		document.setXmlStandalone(true);

		Element root;

		root = document.createElement("rss");

		root.setAttribute("version", "2.0");

		Element channel = document.createElement("channel");
		
		if( rssTitle != null){
			Element title = document.createElement("title");
			title.setTextContent(rssTitle);
			channel.appendChild(title);
		}
		
		
		Element pubDate = document.createElement("pubDate");
		pubDate.setTextContent(rssDateFormater.format(new Date(System.currentTimeMillis())));
		channel.appendChild(pubDate);

		Element lastBuildDate = document.createElement("lastBuildDate");
		lastBuildDate.setTextContent(rssDateFormater.format(new Date(System.currentTimeMillis())));
		channel.appendChild(lastBuildDate);

		/* Loop on each item */

		Iterator<Document> it = docs.iterator();
		while (it.hasNext()) {
			Document doc = (Document) it.next();

			Element item;

			item = document.createElement("item");

			Element title = document.createElement("title");
			title.setTextContent(doc.getTitle());
			item.appendChild(title);
			

			Element link = document.createElement("link");
		
			
			String permaLinkURL = ctx.getPortalUrlFactory().getPermaLink(portalCtx, null, null, doc.getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);
			
			link.setTextContent(permaLinkURL);
			item.appendChild(link);

			String sDate = doc.getProperties().getString("dc:modified");
			if (sDate == null)
				sDate = doc.getProperties().getString("dc:created");

			if (sDate != null) {

				Element date = document.createElement("pubDate");

				Date docDate = nuxeoFormater.parse(sDate);

				date.setTextContent(rssDateFormater.format(docDate));
				item.appendChild(date);
			}

			String sDescription = doc.getProperties().getString("dc:description");

			if (sDescription != null) {
				Element description = document.createElement("description");

				description.setTextContent(Formater.formatDescription(doc, false));
				item.appendChild(description);
			}

			PropertyMap map = doc.getProperties().getMap("ttc:vignette");

			if (map != null && map.getString("data") != null) {
				
				Element enclosure = document.createElement("enclosure");

				/*
			
				Map<String, String> params = new HashMap<String, String>();
				params.put("thubmnailPath", doc.getPath());
				params.put("thubmnailField", "ttc:vignette");
				
				String permaLinkThumbnail = ctx.getPortalUrlFactory().getPermaLink(portalCtx, permLinkRef, params,  portalCtx.getRequest().getParameter("osivia.cms.path"), IPortalUrlFactory.PERM_LINK_TYPE_RSS_PICTURE);
				
				enclosure.setAttribute("url", permaLinkThumbnail);
				*/


				String baseUrl = "http://" + request.getServerName() + ":" + request.getServerPort();

				enclosure.setAttribute("url", baseUrl + ctx.createFileLink(doc, "ttc:vignette"));

			
				
				
				String mimeType =  map.getString("mime-type");
				if( mimeType != null)
					enclosure.setAttribute("type", mimeType);
				
				String length =  map.getString("length");
				if( length != null)
					enclosure.setAttribute("length", length);

				item.appendChild(enclosure);
			}

			channel.appendChild(item);

		}

		root.appendChild(channel);

		document.appendChild(root);

		return document;

	}

}
