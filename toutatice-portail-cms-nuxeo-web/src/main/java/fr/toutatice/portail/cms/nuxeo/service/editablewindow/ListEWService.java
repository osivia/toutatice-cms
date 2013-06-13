package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;

/**
 * 
 * Service spécifique fragments Liste
 * 
 */
public class ListEWService extends EditableWindowService {

	private static final String LISTSCHEMA = "listfgt:listFragment";
	
	private static final String NEWSSCHEMA = "nwslk:links";
	
	private static ListEWService instance;
	
	public static ListEWService getInstance() {
		if(instance == null) {
			instance = new ListEWService();
		}
		return instance;
	}
	
	private ListEWService() {
	}
	
	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment,
			Boolean modeEditionPage) {
		Map<String, String> props = super.fillGenericProps(doc, fragment, modeEditionPage);
		
		PropertyMap mapListe = findSchemaByRefURI(doc, LISTSCHEMA, fragment.getString("uri"));
		
		props.put("osivia.nuxeoRequest", mapListe.getString("request"));
		props.put("osivia.requestInterpretor", "beanShell");
		
		props.put("osivia.displayNuxeoRequest", null);
		props.put("osivia.cms.displayLiveVersion", null);
		props.put("osivia.cms.requestFilteringPolicy", null);
		props.put("osivia.cms.hideMetaDatas", null);
		props.put("osivia.cms.scope", null);
		
		props.put("osivia.cms.style", mapListe.getString("view"));
		props.put("osivia.cms.pageSize", mapListe.getString("pageSize"));
		props.put("osivia.cms.pageSizeMax", mapListe.getString("pageSizeMax"));
		props.put("osivia.cms.maxItems", mapListe.getString("maxItems"));
		
		props.put("osivia.permaLinkRef", null);
		props.put("osivia.rssLinkRef", null);
		props.put("osivia.rssTitle", null);
		
		props.put("osivia.cms.feed", mapListe.getString("feed"));
		
		// Cas particulier des feed
		if("true".equals(mapListe.getString("feed"))) {
			fillFeedProps(doc, fragment, props, mapListe);
		}
		
		return props;
		
	}

	private void fillFeedProps(Document doc, PropertyMap fragment,
			Map<String, String> props, PropertyMap mapListe) {
		
		// On récupère la liste des documents
		PropertyList newslinks = doc.getProperties().getList(NEWSSCHEMA);

		int nbElements = 0;
		Integer i = 0;
		for (Object news : newslinks.list()) {

			if (news instanceof PropertyMap) {
				PropertyMap mapNews = (PropertyMap) news;
				if (mapNews.getString("refURI").equals(fragment.get("uri"))) {
					props.put("osivia.cms.news." + i.toString() + ".docURI",
							mapNews.getString("documentURI"));
					props.put("osivia.cms.news." + i.toString() + ".order",
							mapNews.getString("order"));
					i++;
				}
			}
			
		}
		props.put("osivia.cms.news.size", Integer.toString(i));

	}

	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		
		Integer indexToRemove = findIndexByRefURI(doc, LISTSCHEMA, refURI);
		
		propertiesToRemove.add(LISTSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		// TODO supprimer les feed
		
		return propertiesToRemove;
	}


}
