/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.commands;

import java.util.List;

import javax.naming.ldap.LdapName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.services.InjectionData;
import fr.toutatice.portail.cms.nuxeo.services.InjectionService;

/**
 * Create Nuxeo documents command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class CreateDocumentsCommand implements INuxeoCommand {

    /** Everyone Nuxeo user. */
    private static final String USER_NUXEO_EVERYONE = "Everyone";
    /** Default Nuxeo user. */
    private static final String USER_NUXEO_DEFAULT = "nuxeoDefault";
    /** Nuxeo read permission. */
    private static final String PERMISSION_READ = "Read";
    /** Nuxeo everything permission. */
    private static final String PERMISSION_EVERYTHING = "Everything";
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


    /**
     * Constructor.
     *
     * @param nuxeoController Nuxeo controller
     * @param ldapNames LDAP names for permission setting
     * @param data injection data
     * @throws CMSException
     */
    public CreateDocumentsCommand(NuxeoController nuxeoController, List<LdapName> ldapNames, InjectionData data) throws CMSException {
        super();

        ICMSService cmsService = NuxeoController.getCMSService();

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
        this.deletePreviousDocuments(service, this.parent);
        this.createPortalSites(service, this.parent);
        return null;
    }


    /**
     * Utility method used to create portal sites.
     *
     * @param service document service
     * @param parent Nuxeo parent document
     * @throws CMSException
     */
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
                service.setPermission(document, USER_NUXEO_DEFAULT, PERMISSION_READ);
                service.setPermission(document, USER_NUXEO_EVERYONE, PERMISSION_EVERYTHING, false);

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


    /**
     * Utility method used to create portal pages.
     *
     * @param service document service
     * @param parent Nuxeo parent document
     * @param level current level of depth
     * @param parentNumber parent number
     * @throws CMSException
     */
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


    /**
     * Utility method used to create notes.
     *
     * @param service document service
     * @param parent Nuxeo parent document
     * @param parentNumber parent number
     * @throws CMSException
     */
    private void createNotes(DocumentService service, Document parent, String parentNumber) throws CMSException {
        try {
            for (int i = 1; i <= this.data.getNotesCount(); i++) {
                List<LdapName> randomLdapNames = InjectionService.randomPicking(this.ldapNames, this.data.getProbabilities());

                // Note name
                StringBuffer buffer = new StringBuffer();
                buffer.append(TYPE_NOTE);
                buffer.append("-(");
                buffer.append(parentNumber);
                buffer.append(i);
                buffer.append(")");
                for (LdapName randomLdapName : randomLdapNames) {
                    String groupName = (String) randomLdapName.getRdn(randomLdapName.size() - 1).getValue();
                    buffer.append("-");
                    buffer.append(StringUtils.removeStart(groupName, InjectionService.GROUP_NAME_PREFIX));
                }
                String name = buffer.toString();

                PropertyMap properties = new PropertyMap(1);
                properties.set(PROPERTY_TITLE, name);

                // Create note in workspace
                Document document = service.createDocument(this.workspace, TYPE_NOTE, name, properties);
                // Publish note in portal site or portal page
                Document publishedDocument = service.publish(document, parent);
                // Delete note in workspace
                service.remove(document);

                // ACL
                this.addPermissions(service, publishedDocument, randomLdapNames);
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * Utility method used to add permissions.
     *
     * @param service document service
     * @param document current Nuxeo document
     * @param ldapNames LDAP names
     */
    private void addPermissions(DocumentService service, Document document, List<LdapName> ldapNames) {
        if (CollectionUtils.isNotEmpty(ldapNames)) {
            // Local permissions
            for (LdapName ldapName : ldapNames) {
                String groupName = (String) ldapName.getRdn(ldapName.size() - 1).getValue();
                try {
                    service.setPermission(document, groupName, PERMISSION_READ);
                } catch (Exception e) {
                    // Do nothing
                }
            }

            // Block inheritance
            try {
                service.setPermission(document, USER_NUXEO_EVERYONE, PERMISSION_EVERYTHING, false);
            } catch (Exception e) {
                // Do nothing
            }
        }
    }


    /**
     * Utility method used to delete previous documents.
     *
     * @param service document service
     * @param parent parent Nuxeo document
     * @throws CMSException
     */
    private void deletePreviousDocuments(DocumentService service, Document parent) throws CMSException {
        try {
            Documents children = service.getChildren(parent);
            for (Document child : children.list()) {
                if (StringUtils.startsWith(child.getTitle(), TYPE_PORTAL_SITE)) {
                    service.remove(child);
                }
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getId() {
        return "CreateDocumentsCommand";
    }

}
