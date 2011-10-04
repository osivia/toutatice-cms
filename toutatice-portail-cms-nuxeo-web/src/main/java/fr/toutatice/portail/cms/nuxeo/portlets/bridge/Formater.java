package fr.toutatice.portail.cms.nuxeo.portlets.bridge;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;

import fr.toutatice.portail.api.urls.Link;

public class Formater {

	public static String formatDate(Document doc) throws ParseException {

		String sDate = doc.getProperties().getString("dc:modified");
		if (sDate == null)
			sDate = doc.getProperties().getString("dc:created");

		if (sDate == null)
			return ("");

		sDate = sDate.substring(0, 10);

		DateFormat inputFormater = new SimpleDateFormat("yyyy-MM-dd");
		Date date = inputFormater.parse(sDate);

		DateFormat outputFormater = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRENCH);

		return outputFormater.format(date);
	}

	public static String formatDateAndTime(Document doc) throws ParseException {

		String sDate = doc.getProperties().getString("dc:modified");
		if (sDate == null)
			sDate = doc.getProperties().getString("dc:created");

		if (sDate == null)
			return ("");

		SimpleDateFormat inputFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = inputFormater.parse(sDate);

		DateFormat outputFormater = new SimpleDateFormat("dd/MM/yy HH:mm");

		return outputFormater.format(date);
	}

	public static String formatSize(Document doc) throws ParseException {

		String sSize = doc.getProperties().getString("common:size");

		if (sSize == null || "0".equals(sSize))
			return "";

		Long lSize = Long.decode(sSize);

		// Conversion en Ko
		lSize = (lSize / 1000) + 1;

		return Long.toString(lSize) + " Ko";

	}
	
	public static String formatText(String text, boolean printLineFeeds) throws ParseException {

		if (text == null)
			return "";

		
		String output = text;

		if (!printLineFeeds) {
			output = output.replaceAll("\n", " ");
			output = output.replaceAll("\r", " ");
		}

		return StringHelper.escapeHTML(output);
	}

	public static String formatDescription(Document doc, boolean printLineFeeds) throws ParseException {

		String description = doc.getProperties().getString("dc:description");

		return formatText(description, printLineFeeds);
	}

	public static String formatDescription(Document doc) throws ParseException {

		return formatDescription(doc, true);
	}

	private static Map<String, String> nuxeoIcons;

	static {
		nuxeoIcons = new HashMap<String, String>();
		nuxeoIcons.put("note.gif", "note.gif");
		nuxeoIcons.put("contextuallink.png", "link.png");
		nuxeoIcons.put("word.png", "word.png");
		nuxeoIcons.put("doc.png", "word.png");
		nuxeoIcons.put("docx.png", "word.png");
		nuxeoIcons.put("xls.png", "xls.png");
		nuxeoIcons.put("xlsx.png", "xls.png");
		nuxeoIcons.put("ppt.png", "ppt.png");
		nuxeoIcons.put("pdf.png", "pdf.png");
		nuxeoIcons.put("ordered_folder.png", "folder.gif");		
		nuxeoIcons.put("folder.gif", "folder.gif");
		
	}

	private static String extractNuxeoIconName(Document doc)	{
		String iconName = null;
		String iconURI = doc.getProperties().getString("common:icon");
		if( iconURI != null){
			int namePos = iconURI.lastIndexOf('/');
			if( namePos != -1)
				iconName = iconURI.substring(namePos + 1);
			else
				iconName = iconURI;
		}
		return iconName;

	}

	/**
	 * Renvoie l'icone Nuxeo standard (eventuellement une note, si aucun
	 * icone défini
	 * 
	 * @param doc
	 * @return
	 */
	public static String formatNuxeoIcon(Document doc) {

		String portalIcon = "note.gif";

		String iconName = extractNuxeoIconName( doc);

		if (iconName != null) {
			if (nuxeoIcons.get(iconName) != null) {
				portalIcon = nuxeoIcons.get(iconName);
			} else {
				
				// Pas de correspondance : icone par défaut
				
				// tous les folders
				if ("AnnonceFolder".equals(doc.getType()) || "FaqFolder".equals(doc.getType()) || "DocumentUrlContainer".equals(doc.getType()))
					portalIcon = "folder.gif";

				
				// Pas de correspondance : icone par défaut
				if ("File".equals(doc.getType()) )
					portalIcon = "file.gif";
			}
		}

		return "/img/icons/" + portalIcon;
	}
	
	
	
	private static Map<String, String> nuxeoBigIcons;

	static {
		nuxeoBigIcons = new HashMap<String, String>();
		nuxeoBigIcons.put("note.gif", "note_100.png");
		nuxeoBigIcons.put("contextuallink.png", "link_100.png");	
		nuxeoBigIcons.put("folder.gif", "folder_100.png");
		nuxeoBigIcons.put("ordered_folder.png", "folder_100.png");
		nuxeoBigIcons.put("note.gif", "note_100.png");
		nuxeoBigIcons.put("contextuallink.png", "link_100.png");	
		nuxeoBigIcons.put("folder.gif", "folder_100.png");
		nuxeoBigIcons.put("ordered_folder.png", "folder_100.png");		
		
	}

	/**
	 * Renvoie l'icone Nuxeo standard (eventuellement une note, si aucun
	 * icone défini
	 * 
	 * @param doc
	 * @return
	 */
	public static String formatNuxeoBigIcon(Document doc) {

		String portalIcon = "note_100.png";

		String iconName = extractNuxeoIconName( doc);

		if (iconName != null) {
			if (nuxeoBigIcons.get(iconName) != null) {
				portalIcon = nuxeoBigIcons.get(iconName);
			} else {
				// Pas de correspondance : icone par défaut
				
				// tous les folders
				if ("AnnonceFolder".equals(doc.getType()) || "FaqFolder".equals(doc.getType()) || "DocumentUrlContainer".equals(doc.getType()))
					portalIcon = "folder_100.png";

				if ("File".equals(doc.getType()) )
					portalIcon = "file_100.png";
			}
		}

		return "/img/icons/" + portalIcon;
	}

	public static String formatEmptyIcon() {
		String portalIcon = "empty.gif";
		return "/img/icons/" + portalIcon;
	}

	public static String formatExternalIcon() {
		return "/img/icons/link.png";
	}

	/**
	 * Permet d'illuster les liens qui donnent lieu à un comportement spécifique
	 * (téléchargement, liens externes)
	 * 
	 * @param doc
	 * @return
	 */
	public static String formatSpecificIcon(Document doc) {

		String icon = formatEmptyIcon();

		// Icones pour les fichiers
		if ("File".equals(doc.getType()) )
			icon = Formater.formatNuxeoIcon(doc);

		if ("ContextualLink".equals(doc.getType()))
			icon = Formater.formatExternalIcon();

		return icon;
	}
	
	
	private static Map<String, String> nuxeoTypes;

	static {
		nuxeoTypes = new HashMap<String, String>();
		nuxeoTypes.put("File", "Fichier");
		nuxeoTypes.put("Folder", "Dossier");
		nuxeoTypes.put("OrderedFolder", "Dossier");		
		nuxeoTypes.put("Note", "Note HTML");
		nuxeoTypes.put("Annonce", "Annonce");
		nuxeoTypes.put("AnnonceFolder", "Dossier d'annonces");
		nuxeoTypes.put("Forum", "Forum");
		nuxeoTypes.put("PictureBook", "Livre d'image");
		nuxeoTypes.put("Picture", "Image");	
		nuxeoTypes.put("ContextualLink", "Lien externe");		

	}

	
	public static String formatType(Document doc) {
		
		if (nuxeoTypes.get(doc.getType()) != null) {
			return nuxeoTypes.get(doc.getType());
		} else
			return "";
	
	}
	
	public static String formatTarget( Link link)	{
		if( link.isExternal())	
			return "target=\"_blank\"";	
		else
			return "";
	}

	public static String formatVignette(Document doc) {
		String portalIcon = "file.gif";
		String nuxeoURI = doc.getProperties().getString("common:icon");

		if (nuxeoURI != null && nuxeoIcons.get(nuxeoURI) != null) {
			portalIcon = nuxeoIcons.get(nuxeoURI);
		}

		return "/img/icons/" + portalIcon;
	}

}
