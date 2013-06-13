package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;

/**
 * 
 * Service spécifique fragments HTML
 * 
 */
public class HtmlEWService extends EditableWindowService {

	public static final String HTMLSCHEMA = "htmlfgt:htmlFragment";
	
	private static HtmlEWService instance;
	
	public static HtmlEWService getInstance() {
		if(instance == null) {
			instance = new HtmlEWService();
		}
		return instance;
	}
	
	private HtmlEWService() {
	}
	
	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {
		return super.fillGenericProps(doc, fragment, modeEditionPage);
	}


	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		
		Integer indexToRemove = findIndexByRefURI(doc, HTMLSCHEMA, refURI);
		
		propertiesToRemove.add(HTMLSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		return propertiesToRemove;
	}
}
