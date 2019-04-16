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
 */
package fr.toutatice.portail.cms.nuxeo.api.services.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimeType;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.FileMimeType;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentAttachmentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.liveedit.OnlyofficeLiveEditHelper;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;

/**
 * Document data access object service implementation.
 *
 * @author Cédric Krommenhoek
 * @see IDAO
 * @see Document
 * @see DocumentDTO
 */
public final class DocumentDAO implements IDAO<Document, DocumentDTO> {

    /** Nuxeo date regex. */
    private static final String NUXEO_DATE_REGEX = "[\\-0-9]+T[:\\.0-9]+Z";

    /** Singleton instance. */
    private static DocumentDAO instance;


    /** Nuxeo service. */
    private final INuxeoService nuxeoService;

    /** Nuxeo date regex pattern. */
    private final Pattern datePattern;


    /**
     * Private constructor.
     */
    private DocumentDAO() {
        super();

        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);

        // Nuxeo date regex pattern
        this.datePattern = Pattern.compile(NUXEO_DATE_REGEX);
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static DocumentDAO getInstance() {
        if (instance == null) {
            instance = new DocumentDAO();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentDTO toDTO(Document document) {
        return this.toDTO(null, document);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentDTO toDTO(PortalControllerContext portalControllerContext, Document document) {
        DocumentDTO dto = new DocumentDTO();

        // Identifier
        dto.setId(document.getId());
        // Title
        dto.setTitle(document.getTitle());
        // Path
        dto.setPath(document.getPath());
        // Type
        DocumentType type = this.getType(document.getType());
        dto.setType(type);
        // Icon
        if (type != null) {
            String icon;
            if (type.isFile()) {
                icon = StringUtils.defaultIfEmpty(this.getIcon(document), type.getIcon());
            } else {
                icon = type.getIcon();
            }
            dto.setIcon(icon);
        }
        // liveEdit
        dto.setLiveEditable(isLiveEditable(document));
        // Properties
        Map<String, Object> properties = dto.getProperties();
        properties.putAll(this.toMap(document.getProperties()));

        // Original Nuxeo document
        dto.setDocument(document);

        // Attachments
        if (portalControllerContext != null) {
            this.generateAttachments(portalControllerContext, document, dto);
        }

        return dto;
    }


    /**
     * Convert property list to list.
     *
     * @param propertyList property list
     * @return list
     */
    private List<Object> toList(PropertyList propertyList) {
        List<Object> list = new ArrayList<>(propertyList.size());

        int index = 0;
        for (Object object : propertyList.list()) {
            if (object instanceof PropertyMap) {
                PropertyMap propertyMapValue = (PropertyMap) object;
                list.add(this.toMap(propertyMapValue));
            } else if (object instanceof PropertyList) {
                PropertyList propertyListValue = (PropertyList) object;
                list.add(this.toList(propertyListValue));
            } else if (object instanceof String) {
                String stringValue = (String) object;

                Matcher dateMatcher = this.datePattern.matcher(stringValue);
                if (dateMatcher.matches()) {
                    Date date = propertyList.getDate(index);
                    list.add(date);
                } else {
                    list.add(stringValue);
                }
            } else {
                list.add(object);
            }

            index++;
        }

        return list;
    }


    /**
     * Convert property map to map.
     *
     * @param propertyMap property map
     * @return map
     */
    private Map<String, Object> toMap(PropertyMap propertyMap) {
        Map<String, Object> map = new HashMap<>(propertyMap.size());

        for (Entry<String, Object> entry : propertyMap.map().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof PropertyMap) {
                PropertyMap propertyMapValue = (PropertyMap) value;
                map.put(key, this.toMap(propertyMapValue));
            } else if (value instanceof PropertyList) {
                PropertyList propertyListValue = (PropertyList) value;
                map.put(key, this.toList(propertyListValue));
            } else if (value instanceof String) {
                String stringValue = (String) value;

                Matcher dateMatcher = this.datePattern.matcher(stringValue);
                if (dateMatcher.matches()) {
                    Date date = propertyMap.getDate(key);
                    map.put(key, date);
                } else {
                    map.put(key, stringValue);
                }
            } else {
                map.put(key, value);
            }
        }

        return map;
    }


    /**
     * Get CMS item type.
     *
     * @param type document type name
     * @return CMS item type
     */
    private DocumentType getType(String type) {
        // CMS customizer
        INuxeoCustomizer cmsCustomizer = this.nuxeoService.getCMSCustomizer();

        // CMS item types
        Map<String, DocumentType> types = cmsCustomizer.getDocumentTypes();
        return types.get(type);
    }

    private boolean isLiveEditable(Document document) {
        // Document properties
        PropertyMap properties = document.getProperties();
        // File content
        PropertyMap fileContent = properties.getMap("file:content");

        if (fileContent != null) {
            String mimeType = fileContent.getString("mime-type");
            if (StringUtils.isNotBlank(mimeType)) {
                boolean isPluginRegistered = this.nuxeoService.getCMSCustomizer().getCustomizationService()
                        .isPluginRegistered(OnlyofficeLiveEditHelper.ONLYOFFICE_PLUGIN_NAME);
                if (isPluginRegistered) {
                    return OnlyofficeLiveEditHelper.isMimeTypeSupported(mimeType);
                }
            }
        }
        return false;
    }


    /**
     * Get document icon.
     *
     * @param document document
     * @return icon, may be null
     */
    private String getIcon(Document document) {
        // Document properties
        PropertyMap properties = document.getProperties();
        // File content
        PropertyMap fileContent = properties.getMap("file:content");

        // Icon
        String icon;
        if (fileContent == null) {
            icon = null;
        } else {
            // Mime type
            String mimeType = fileContent.getString("mime-type");

            icon = this.getIcon(mimeType);
        }

        return icon;
    }


    /**
     * Get icon from mime type representation.
     *
     * @param mimeType mime type representation
     * @return icon, may be null
     */
    public String getIcon(String mimeType) {
        // CMS customizer
        INuxeoCustomizer customizer = this.nuxeoService.getCMSCustomizer();

        // File MIME type
        FileMimeType fileMimeType;
        try {
            fileMimeType = customizer.getFileMimeType(mimeType);
        } catch (IOException e) {
            fileMimeType = null;
        }

        // Icon
        String icon;
        if (fileMimeType == null) {
            icon = null;
        } else {
            icon = fileMimeType.getIcon();
        }

        return icon;
    }


    /**
     * Get icon from mime type.
     * 
     * @param mimeType mime type
     * @return icon, may be null
     */
    public String getIcon(MimeType mimeType) {
        // Icon
        String icon;
        if (mimeType == null) {
            icon = null;
        } else {
            icon = this.getIcon(mimeType.getBaseType());
        }

        return icon;
    }


    /**
     * Generate document attachments.
     *
     * @param document Nuxeo document
     * @param documentDto document DTO
     */
    private void generateAttachments(PortalControllerContext portalControllerContext, Document document, DocumentDTO documentDto) {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        nuxeoController.setCurrentDoc(document);

        // Document path
        String path = document.getPath();

        // Attachments
        List<DocumentAttachmentDTO> attachments = documentDto.getAttachments();

        // Attachments property list
        PropertyList list = document.getProperties().getList("files:files");

        if ((list != null) && !list.isEmpty()) {
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
                String icon = this.getIcon(mimeType);
                attachment.setIcon(icon);

                // Attachment size
                Long size = file.getLong("length");
                attachment.setSize(size);

                // Attachment digest
                String digest = file.getString("digest");
                attachment.setDigest(digest);

                // Attachement URL
                String url = nuxeoController.createAttachedFileLink(path, String.valueOf(i));
                attachment.setUrl(url);

                attachments.add(attachment);
            }
        }
    }

}
