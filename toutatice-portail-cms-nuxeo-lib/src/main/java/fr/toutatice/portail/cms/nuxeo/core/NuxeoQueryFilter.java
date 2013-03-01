package fr.toutatice.portail.cms.nuxeo.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NuxeoQueryFilter {

	public static String addPublicationFilter(String nuxeoRequest, boolean displayLiveVersion) {

		/* Filtre pour sélectionner uniquement les version publiées */

		String requestFilter = "";

		if (displayLiveVersion) {
			// selection des versions lives : il faut exclure les proxys
			requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0  AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0 ";
		} else {
			// sélection des folders et des documents publiés

			//requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 1  AND ecm:currentLifeCycleState <> 'deleted' ";
			// la section n'a pas de proxy
			requestFilter = "(ecm:isProxy = 1 OR ecm:primaryType = 'Section') AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' ";

		}

		// Insertion du filtre avant le order

		String beforeOrderBy = "";
		String orderBy = "";

		try {
			Pattern ressourceExp = Pattern.compile("(.*)ORDER([ ]*)BY(.*)");

			Matcher m = ressourceExp.matcher(nuxeoRequest.toUpperCase());
			m.matches();

			if (m.groupCount() == 3) {
				beforeOrderBy = nuxeoRequest.substring(0, m.group(1).length());
				orderBy = nuxeoRequest.substring(m.group(1).length());
			}
		} catch (IllegalStateException e) {
			beforeOrderBy = nuxeoRequest;
		}

		String finalRequest = beforeOrderBy;

		if (finalRequest.length() > 0)
			finalRequest += " AND ";
		finalRequest += requestFilter;

		finalRequest += " " + orderBy;
		nuxeoRequest = finalRequest;

		return nuxeoRequest;

	}

}
