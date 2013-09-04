package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HTMLCommentsTreeBuilder {

	public static final int DEPTH_STEP = 12;
	public static final String NEW_LINE = "<br/>";

	public static String buildHtmlTree(StringBuffer htmlTree, JSONArray comments, int level, int authType, String user) {

		float depth = computeDepth(level, DEPTH_STEP);
		htmlTree.append("<ul style=\"padding-left:");
		htmlTree.append(depth);
		htmlTree.append(";\">");

		Iterator itComments = comments.iterator();
		while (itComments.hasNext()) {
			JSONObject comment = (JSONObject) itComments.next();

			htmlTree.append("<li>");

			htmlTree.append("<div class=\"authorDate\">");

			String creator = (String) comment.get("author");
			htmlTree.append("<div class=\"author\">");
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
			htmlTree.append("</div>");
			
			Boolean canDelete = (Boolean) comment.get("canDelete");
			if (canDelete) {
				htmlTree.append("<div class=\"delete_comment\">");
				htmlTree.append("<a class=\"fancybox_comment\" href=\"#div_delete_comment\" ");
				htmlTree.append("onclick=\"document.getElementById('currentCommentId').value='");
				htmlTree.append(comment.get("id"));
				htmlTree.append("'\">Supprimer</a>");
				htmlTree.append("</div>");
			}

			htmlTree.append("<div class=\"child_comment\">");
			htmlTree.append("<a class=\"fancybox_comment\" href=\"#div_add_child_comment\" ");
			htmlTree.append("onclick=\"document.getElementById('commentParentId').value='");
			htmlTree.append(comment.get("id"));
			htmlTree.append("'\">Répondre</a>");
			htmlTree.append("</div>");

			htmlTree.append("</li>");

			JSONArray children = (JSONArray) comment.get("children");
			if (children != null) {
				level++;
				htmlTree.append(buildHtmlTree(new StringBuffer(), children, level, authType, user));
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
