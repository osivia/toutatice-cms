/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.directory.entity.DirectoryPerson;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.ContextualizationHelper;

/**
 * Classe permettant de construire l'arbre HTML des commentaires.
 * @author david
 *
 */
public class HTMLCommentsTreeBuilder {
	
	/** Décalage des réponses aux commentaires */
	public static final int DEPTH_STEP = 12;
	public static final String NEW_LINE = "<br/>";
	/** Tag indiquant qu'une jsp devra être insérée dans view-comments.jsp */
	public static final String ADD_COM_CHILD_JSP_TAG = "§§§";
	/** Tag pour insérer les id des div de commenatires */
	public static final String DIV_COM_ID_TAG = "###";
	/**Tag to insert container identifiant */
    public static final String ADD_CONTAINER_ID = "*$%";


    /** Directory service locator. */
    private final IDirectoryServiceLocator directoryServiceLocator;

    private static HTMLCommentsTreeBuilder instance;

    private HTMLCommentsTreeBuilder() {
        this.directoryServiceLocator = Locator.findMBean(IDirectoryServiceLocator.class, IDirectoryServiceLocator.MBEAN_NAME);
    }

    public static HTMLCommentsTreeBuilder getInstance() {
        if (instance == null) {
            instance = new HTMLCommentsTreeBuilder();
        }
        return instance;
    }

    public String buildHtmlTree(CMSServiceCtx cmsCtx, StringBuffer htmlTree, JSONArray comments, int level, int authType, String user) throws CMSException {

		float depth = computeDepth(level, DEPTH_STEP);
		htmlTree.append("<ul style=\"padding-left:");
		htmlTree.append(depth);
		htmlTree.append(";\">");

		Iterator<JSONObject> itComments = comments.iterator();
		while (itComments.hasNext()) {
			JSONObject comment = (JSONObject) itComments.next();
			
			htmlTree.append("<li onmouseover=\"javascript:toggleactions(this, true);\" onmouseout=\"javascript:toggleactions(this, false);\">");
			

			htmlTree.append("<div class=\"authorDate\">");

			String creator = (String) comment.get("author");
			htmlTree.append("<div class=\"author\">");
            htmlTree.append("<img src=\"");
            htmlTree.append(NuxeoController.getCMSService().getUserAvatar(cmsCtx, comment.get("author").toString()).getUrl());
            htmlTree.append("\" alt=\"avatar\" class=\"avatarIcon\"> ");

            if (directoryServiceLocator.getDirectoryService() != null) {
                DirectoryPerson person = directoryServiceLocator.getDirectoryService().getPerson(creator);

                creator = person.getDisplayName();

                IPortalUrlFactory urlFactory = (IPortalUrlFactory) cmsCtx.getPortletCtx().getAttribute("UrlService");

                
                if(urlFactory != null) {
                    PortalControllerContext pcc = new PortalControllerContext(cmsCtx.getControllerContext());
                    Map<String, String> properties = new HashMap<String, String>();
                    properties.put("osivia.ajaxLink", "1");
                    properties.put("theme.dyna.partial_refresh_enabled", "true");
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("uidFichePersonne", person.getUid());

                    try {
                        String profileUrl = urlFactory.getStartPageUrl(pcc, "userprofile", "/default/templates/userprofile", properties,
                                parameters);

                        // String profileUrl = urlFactory.getStartPortletUrl(pcc, "toutatice-identite-fichepersonne-portailPortletInstance", properties,
                        // parameters, false);

                        creator = "<a href=\"" + profileUrl + "\" >" + creator + "</a>";

                    } catch (PortalException e) {
                        e.printStackTrace();
                    }
                }
            }

			htmlTree.append(creator);
			htmlTree.append("</div>");

			htmlTree.append("<div class=\"creationDate\">");
			String creationDate = convertDateToString((JSONObject) comment.get("creationDate"));
			htmlTree.append(creationDate);
			htmlTree.append("</div>");

			htmlTree.append("</div>");

			String content = (String) comment.get("content");
			content = restoreNewLines(content);
			htmlTree.append("<div class=\"content\" style=\"width:");
			htmlTree.append(100 - depth);
			htmlTree.append("%\">");
			htmlTree.append(content);
			htmlTree.append(ADD_COM_CHILD_JSP_TAG);
			htmlTree.append("</div>");
			
			if(ContextualizationHelper.isCurrentDocContextualized(cmsCtx)){
				Boolean canDelete = (Boolean) comment.get("canDelete");
				if (canDelete) {
					htmlTree.append("<div class=\"delete_comment\">");
					htmlTree.append("<a class=\"fancybox_inline\" href=\"#div_delete_comment" + ADD_CONTAINER_ID +"\" ");
					htmlTree.append("onclick=\"document.getElementById('currentCommentId" + ADD_CONTAINER_ID + "').value='");
					htmlTree.append(comment.get("id"));
					htmlTree.append("'\">Supprimer</a>");
					htmlTree.append("</div>");
				}
				
				htmlTree.append("<div class=\"child_comment\">");
				htmlTree.append("<span  class=\"add-child-comment-span\" ");
				htmlTree.append("onclick=\"showCommentField(\'" + DIV_COM_ID_TAG + ADD_CONTAINER_ID + "\');");
                htmlTree.append("document.getElementById('commentParentId" + DIV_COM_ID_TAG + ADD_CONTAINER_ID + "').value='");
				htmlTree.append(comment.get("id"));
				htmlTree.append("'\">Répondre");
				htmlTree.append("</span>");
				htmlTree.append("</div>");
			}

			htmlTree.append("</li>");

			JSONArray children = (JSONArray) comment.get("children");
			if (children != null) {
				level++;
				htmlTree.append(buildHtmlTree(cmsCtx, new StringBuffer(), children, level, authType, user));
				level = 0;
			}
		}
		htmlTree.append("</ul>");
		return htmlTree.toString();
	}

	public static String storeNewLines(final String content) {
		String commentContent = content;
		return commentContent.replaceAll("\r\n", NEW_LINE);
	}

	public static String restoreNewLines(final String content) {
		String commentContent = content;
		return commentContent.replaceAll(NEW_LINE, "\r\n");
	}

	private static float computeDepth(final int level, final int fixedStep) {
		float depth = 0;
		if (level != 0) {
			float fixedDepth = (float) fixedStep / 100;
			depth = (float) fixedDepth / (1 - level * fixedDepth);
			depth = (float) Math.round(depth * 100);
		}
		return depth;
	}

	private static String convertDateToString(JSONObject jsonDate) {
		String sDate = null;
		Long time = (Long) jsonDate.get("timeInMillis");
		if (time != null) {
			Date date = new Date(time);
			DateFormat outputFormater = new SimpleDateFormat("dd/MM/yy HH:mm");
			sDate = outputFormater.format(date);
		} else {
			sDate = "<div class=\"errorDateFormat\">Mauvais format de date</div>";
		}
		return sDate;
	}

}
