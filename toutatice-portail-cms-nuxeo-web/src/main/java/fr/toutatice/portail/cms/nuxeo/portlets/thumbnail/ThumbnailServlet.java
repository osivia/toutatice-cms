package fr.toutatice.portail.cms.nuxeo.portlets.thumbnail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.core.BinaryContent;
import fr.toutatice.portail.cms.nuxeo.core.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;

public class ThumbnailServlet extends HttpServlet

{

	private static final long serialVersionUID = -7270075202468538917L;
	private static PortletContext portletCtx;

	public static void setPortletContext(PortletContext documentPortletCtx) {
		portletCtx = documentPortletCtx;
	}

	private static long THUMBNAIL_TIMEOUT = 180;

	public String formatResourceLastModified() {

		SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		return inputFormater.format(new Date(System.currentTimeMillis()));
	}

	public boolean isResourceExpired(String sOriginalDate) {

		boolean isExpired = true;

		if (sOriginalDate != null) {

			SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
			try {
				Date originalDate = inputFormater.parse(sOriginalDate);
				if (System.currentTimeMillis() < originalDate.getTime() + THUMBNAIL_TIMEOUT * 1000)
					isExpired = false;
			} catch (Exception e) {

			}
		}

		return isExpired;
	}

	public boolean serveResourceByCache(HttpServletRequest resourceRequest, HttpServletResponse resourceResponse)
			throws PortletException, IOException {

		String sOriginalDate = resourceRequest.getHeader("if-modified-since");
		if (sOriginalDate == null)
			sOriginalDate = resourceRequest.getHeader("If-Modified-Since");

		if (!isResourceExpired(sOriginalDate)) { // validation
													// request

			// resourceResponse.setContentLength(0);
			resourceResponse.sendError(HttpServletResponse.SC_NOT_MODIFIED);

			resourceResponse.setHeader("Last-Modified", sOriginalDate);

			return true;
		}

		return false;
	}

	public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws IOException,
			ServletException {
		
		OutputStream output = theResponse.getOutputStream();
		try {

			if (serveResourceByCache(theRequest, theResponse))
				return;

			String docPath = theRequest.getParameter("path");
			docPath = URLDecoder.decode(docPath, "UTF-8");

			NuxeoController ctx = new NuxeoController(portletCtx);

			ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
			ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

			BinaryContent content = (BinaryContent) ResourceUtil.getFileContent(ctx, docPath, "ttc:vignette");

			// Les headers doivent être positionnées avant la réponse
			theResponse.setContentType(content.getMimeType());
			
			theResponse.setHeader("Cache-Control", "max-age=" + THUMBNAIL_TIMEOUT);

			theResponse.setHeader("Last-Modified", formatResourceLastModified());
			

			ResourceUtil.copy(new FileInputStream(content.getFile()), theResponse.getOutputStream(), 4096);

		} catch (Exception e) {
			throw new ServletException(e);
		} finally	{
			output.close();
		}

	}

}
