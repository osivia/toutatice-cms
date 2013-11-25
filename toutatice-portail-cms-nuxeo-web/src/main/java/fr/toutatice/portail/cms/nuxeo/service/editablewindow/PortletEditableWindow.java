package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * 
 * Service spécifique fragments portlet
 * 
 */
public class PortletEditableWindow extends EditableWindow {


    private static final String PORTLETSCHEMA = "ptlfgt:portletFragment";

    private static final String PORTLETPROPS = "ptlprops:properties";

	
    public PortletEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);

    }


	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment,
			Boolean modeEditionPage) {
		Map<String, String> props = super.fillGenericProps(doc, fragment, modeEditionPage);
		
        PropertyMap mapPortletSch = EditableWindowHelper.findSchemaByRefURI(doc, PORTLETSCHEMA, fragment.getString("uri"));
        setInstancePortlet(mapPortletSch.getString("portletInstance"));
		
		
        PropertyList list = doc.getProperties().getList(PORTLETPROPS);

        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap map = (PropertyMap) o;
                if (fragment.getString("uri").equals(map.get("refURI"))) {
                    props.put(map.get("key").toString(), map.get("value").toString());
                }
            }
        }

		return props;
		
	}



	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		
        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, PORTLETSCHEMA, refURI);
		
        propertiesToRemove.add(PORTLETSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		// TODO supprimer les feed
		
		return propertiesToRemove;
	}


}
