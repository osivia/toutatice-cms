package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentAttachmentDTO;
import fr.toutatice.portail.cms.nuxeo.api.fragment.FragmentModule;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;

/**
 * Attachments fragment module.
 * 
 * @author Cédric Krommenhoek
 * @see FragmentModule
 */
public class AttachmentsFragmentModule extends FragmentModule {

    /** Fragment identifier. */
    public static final String ID = "attachments";


    /** Document path window property. */
    private static final String PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;

    /** Attachments Nuxeo document property. */
    private static final String ATTACHEMENTS_DOCUMENT_PROPERTY = "files:files";

    /** JSP name. */
    private static final String JSP_NAME = "attachments";


    /** Document DAO. */
    private final DocumentDAO documentDao;


    /**
     * Constructor.
     * 
     * @param portletContext portlet context
     */
    public AttachmentsFragmentModule(PortletContext portletContext) {
        super(portletContext);

        // Document DAO
        this.documentDao = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();
        // Window
        PortalWindow window = WindowFactory.getWindow(request);

        // Document path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);

        if (StringUtils.isBlank(path)) {
            request.setAttribute(MESSAGE_KEY_ATTRIBUTE, "MESSAGE_PATH_UNDEFINED");
        } else {
            // Computed path
            path = nuxeoController.getComputedPath(path);

            // Nuxeo document context
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
            // Nuxeo document
            Document document = documentContext.getDoc();
            nuxeoController.setCurrentDoc(document);

            // Nuxeo document properties
            PropertyMap properties = document.getProperties();
            // Attachments property list
            PropertyList list = properties.getList(ATTACHEMENTS_DOCUMENT_PROPERTY);

            if ((list == null) || list.isEmpty()) {
                // Empty response
                request.setAttribute("osivia.emptyResponse", "1");
            } else {
                List<DocumentAttachmentDTO> attachments = new ArrayList<>(list.size());

                for (int i = 0; i < list.size(); i++) {
                    // Attachment property map
                    PropertyMap map = list.getMap(i);

                    // Attachment
                    DocumentAttachmentDTO attachment = new DocumentAttachmentDTO();

                    // Attachment file property map
                    PropertyMap file = map.getMap("file");

                    // Attachment name
                    String name = file.getString("name");
                    if (StringUtils.isEmpty(name)) {
                        name = map.getString("filename");
                    }
                    attachment.setName(name);

                    // Attachment icon
                    String mimeType = file.getString("mime-type");
                    String icon = this.documentDao.getIcon(mimeType);
                    attachment.setIcon(icon);

                    // Attachment size
                    Long size = file.getLong("length");
                    attachment.setSize(size);

                    // Attachement URL
                    String url = nuxeoController.createAttachedFileLink(path, String.valueOf(i));
                    attachment.setUrl(url);

                    attachments.add(attachment);
                }

                request.setAttribute("attachments", attachments);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();
        // Window
        PortalWindow window = WindowFactory.getWindow(request);

        // Document path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);
        request.setAttribute("path", path);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();
        // Window
        PortalWindow window = WindowFactory.getWindow(request);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Action name
            String action = request.getParameter(ActionRequest.ACTION_NAME);

            if ("save".equals(action)) {
                // Document path
                String path = StringUtils.trimToNull(request.getParameter("path"));
                window.setProperty(PATH_WINDOW_PROPERTY, path);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewJSPName() {
        return JSP_NAME;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return JSP_NAME;
    }

}
