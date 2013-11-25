package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.Collections;
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
		
        Integer indexFgtToRemove = EditableWindowHelper.findIndexByRefURI(doc, PORTLETSCHEMA, refURI);
		
        propertiesToRemove.add(PORTLETSCHEMA.concat("/").concat(indexFgtToRemove.toString()));
		
        PropertyList props = doc.getProperties().getList(PORTLETPROPS);
        Integer index = 0;

        for (Object o : props.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap map = (PropertyMap) o;
                if (refURI.equals(map.get("refURI"))) {
                    propertiesToRemove.add(PORTLETPROPS.concat("/").concat(index.toString()));
                }
            }
            index++;
        }
        // Bug automation, supprimer la liste de propriétés par son dernier élément, puis l'avant dernier, etc.
        // sinon décalage des n° d'index dans les propriétés
        Collections.reverse(propertiesToRemove);
		return propertiesToRemove;
	}


}
