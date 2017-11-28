package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindowHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.ViewFragmentPortlet;

/**
 * Picture editable window.
 *
 * @author Cédric Krommenhoek
 * @see EditableWindow
 */
public class PictureEditableWindow extends EditableWindow {

    /** Picture fragment schema. */
    public static final String PICTURE_FRAGMENT_SCHEMA = "pictfgt:pictureFragment";
    /** Image source schema. */
    public static final String IMAGE_SOURCE_SCHEMA = "picturePath";
    /** Target schema. */
    public static final String TARGET_SCHEMA = "targetPath";


    /**
     * Constructor.
     *
     * @param instance portlet instance
     * @param prefix window prefix
     */
    public PictureEditableWindow(String instance, String prefix) {
        super(instance, prefix);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> fillProps(Document document, PropertyMap fragment, Boolean modeEditionPage) {
        // Properties
        Map<String, String> properties = super.fillGenericProps(document, fragment, modeEditionPage);
        properties.put(ViewFragmentPortlet.FRAGMENT_TYPE_ID_WINDOW_PROPERTY, DocumentPictureFragmentModule.ID);
        properties.put(DocumentPictureFragmentModule.PROPERTY_NAME_WINDOW_PROPERTY, "file:content");

        // Picture schema property map
        PropertyMap propertyMap = EditableWindowHelper.findSchemaByRefURI(document, PICTURE_FRAGMENT_SCHEMA, fragment.getString("uri"));
        properties.put(DocumentPictureFragmentModule.NUXEO_PATH_WINDOW_PROPERTY, propertyMap.getString(IMAGE_SOURCE_SCHEMA));
        properties.put(DocumentPictureFragmentModule.TARGET_PATH_WINDOW_PROPERTY, propertyMap.getString(TARGET_SCHEMA));

        return properties;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> prepareDelete(Document document, String refURI) {
        // Properties
        List<String> properties = new ArrayList<String>();

        this.prepareDeleteGeneric(properties, document, refURI);

        // Index
        Integer index = EditableWindowHelper.findIndexByRefURI(document, PICTURE_FRAGMENT_SCHEMA, refURI);
        properties.add(PICTURE_FRAGMENT_SCHEMA.concat("/").concat(index.toString()));

        return properties;
    }

}
