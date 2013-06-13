package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;

/**
 * Service spécifique fragments portlet métier
 * 
 */
public class PortletEWService extends EditableWindowService {

	public final static String PORTLETSCHEMA = "ptlfgt:portletFragment";
	
	private static PortletEWService instance;
	
	public static PortletEWService getInstance() {
		if(instance == null) {
			instance = new PortletEWService();
		}
		return instance;
	}
	
	private PortletEWService() {
	}

	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment,
			Boolean modeEditionPage) {

		PropertyMap mapPortlet = findSchemaByRefURI(doc, PORTLETSCHEMA, fragment.getString("uri"));
		type.setPorletInstance(mapPortlet.getString("portletInstance"));
		
		Map<String, String> props = super.fillGenericProps(doc, fragment, modeEditionPage);
		return props;
	}

	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		
		Integer indexToRemove = findIndexByRefURI(doc, PORTLETSCHEMA, refURI);
		
		propertiesToRemove.add(PORTLETSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		// TODO supprimer les propriétés de configuration de la portlet
		
		return propertiesToRemove;
	}

	
}
