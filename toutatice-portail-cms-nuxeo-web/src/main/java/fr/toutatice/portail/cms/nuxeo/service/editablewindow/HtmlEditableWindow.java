package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * 
 * Service spécifique fragments HTML
 * 
 */
public class HtmlEditableWindow extends EditableWindow {


    public static final String HTMLSCHEMA = "htmlfgt:htmlFragment";
	
    public HtmlEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);

    }
	
	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {
		return super.fillGenericProps(doc, fragment, modeEditionPage);
	}


	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		
        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, HTMLSCHEMA, refURI);
		
		propertiesToRemove.add(HTMLSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		return propertiesToRemove;
	}
}
