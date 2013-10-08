package fr.toutatice.portail.cms.nuxeo.commands;

import java.util.List;
import java.util.Random;

import javax.naming.ldap.LdapName;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.services.InjectionData;

/**
 * Create Nuxeo documents command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class CreateDocumentsCommand implements INuxeoCommand {

    /** Nuxeo permission. */
    private static final String PERMISSION = "Read";
    /** Note Nuxeo type. */
    private static final String TYPE_PORTAL_SITE = "PortalSite";
    /** Note Nuxeo type. */
    private static final String TYPE_PORTAL_PAGE = "PortalPage";
    /** Note Nuxeo type. */
    private static final String TYPE_NOTE = "Note";
    /** Title Nuxeo property. */
    private static final String PROPERTY_TITLE = "dc:title";
    /** Internal contents contextualization Nuxeo property. */
    private static final String PROPERTY_CONTEXTUALIZE_INTERNAL_CONTENTS = "ttc:contextualizeInternalContents";
    /** Show in menu Nuxeo property. */
    private static final String PROPERTY_SHOW_IN_MENU = "ttc:showInMenu";


    /** Random number generator. */
    private final Random random;
    /** CMS context. */
    private final CMSServiceCtx cmsContext;
    /** Names of LDAP groups. */
    private final List<LdapName> ldapNames;
    /** Nuxeo parent document. */
    private final Document parent;
    /** Nuxeo workspace document. */
    private final Document workspace;
    /** Injection data. */
    private final InjectionData data;


    public CreateDocumentsCommand(NuxeoController nuxeoController, List<LdapName> ldapNames, InjectionData data) throws CMSException {
        super();

        ICMSService cmsService = NuxeoController.getCMSService();

        // Random number generator
        this.random = new Random();

        // CMS context
        this.cmsContext = nuxeoController.getCMSCtx();
        this.cmsContext.setDisplayLiveVersion("1");
        this.cmsContext.setForcePublicationInfosScope("superuser_context");

        // LDAP groups
        this.ldapNames = ldapNames;

        // Parent document
        this.parent = (Document) cmsService.getContent(this.cmsContext, data.getParentPath()).getNativeItem();
        // Workspace document
        this.workspace = (Document) cmsService.getContent(this.cmsContext, data.getWorkspacePath()).getNativeItem();

        // Injection data
        this.data = data;
    }


    /**
     * {@inheritDoc}
     */
    public Object execute(Session nuxeoSession) throws CMSException {
        DocumentService service = nuxeoSession.getAdapter(DocumentService.class);
        this.createPortalSites(service, this.parent);
        return null;
    }


    private void createPortalSites(DocumentService service, Document parent) throws CMSException {
        int level = 1;

        try {
            for (int i = 1; i <= this.data.getCount(); i++) {
                String number = String.valueOf(i);
                String name = TYPE_PORTAL_SITE + "-" + number;

                PropertyMap properties = new PropertyMap(1);
                properties.set(PROPERTY_TITLE, name);
                properties.set(PROPERTY_CONTEXTUALIZE_INTERNAL_CONTENTS, true);

                // Create portal site in parent
                Document document = service.createDocument(this.parent, TYPE_PORTAL_SITE, name, properties);

                // Create notes
                this.createNotes(service, document, number);

                // Recursivity
                if (level < this.data.getDepth()) {
                    this.createPortalPages(service, document, level + 1, number);
                }
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    private void createPortalPages(DocumentService service, Document parent, int level, String parentNumber) throws CMSException {
        try {
            for (int i = 1; i <= this.data.getCount(); i++) {
                String number = parentNumber + i;
                String name = TYPE_PORTAL_PAGE + "-" + number;

                PropertyMap properties = new PropertyMap(1);
                properties.set(PROPERTY_TITLE, name);
                properties.set(PROPERTY_SHOW_IN_MENU, true);

                // Create portal site in parent
                Document document = service.createDocument(parent, TYPE_PORTAL_PAGE, name, properties);

                // Create notes
                this.createNotes(service, document, number);

                // Recursivity
                if (level < this.data.getDepth()) {
                    this.createPortalPages(service, document, level + 1, number);
                }
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    private void createNotes(DocumentService service, Document parent, String parentNumber) throws CMSException {
        try {
            for (int i = 1; i <= this.data.getNotesCount(); i++) {
                String number = parentNumber + i;
                String name = TYPE_NOTE + "-" + number;

                PropertyMap properties = new PropertyMap(1);
                properties.set(PROPERTY_TITLE, name);

                // Create note in workspace
                Document document = service.createDocument(this.workspace, TYPE_NOTE, name, properties);
                // Publish note in portal site or portal page
                Document publishedDocument = service.publish(document, parent);
                // Delete note in workspace
                service.remove(document);

                // ACL
                // this.addPermissions(service, publishedDocument);
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    private void addPermissions(DocumentService service, Document document) {
        for (float probability : this.data.getProbabilities()) {
            if (this.random.nextFloat() < probability) {
                // Add permission
                int index = this.random.nextInt(this.ldapNames.size());
                LdapName ldapName = this.ldapNames.get(index);
                String groupName = (String) ldapName.getRdn(this.ldapNames.size() - 1).getValue();

                try {
                    service.setPermission(document, groupName, PERMISSION);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getId() {
        return "CreateDocumentsCommand";
    }

}
