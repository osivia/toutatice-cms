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
import org.osivia.portal.api.cms.FileDocumentType;
import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
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
        // Properties
        Map<String, Object> properties = dto.getProperties();
        properties.putAll(this.toMap(document.getProperties()));

        // Original Nuxeo document
        dto.setDocument(document);

        return dto;
    }


    /**
     * Convert property list to list.
     *
     * @param propertyList property list
     * @return list
     */
    private List<Object> toList(PropertyList propertyList) {
        List<Object> list = new ArrayList<Object>(propertyList.size());

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
        Map<String, Object> map = new HashMap<String, Object>(propertyMap.size());

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
        // Mime type
        MimeType mimeTypeObject;
        try {
            mimeTypeObject = new MimeType(mimeType);
        } catch (Exception e) {
            mimeTypeObject = null;
        }

        return this.getIcon(mimeTypeObject);
    }


    /**
     * Get icon from mime type.
     * 
     * @param mimeType mime type
     * @return icon, may be null
     */
    public String getIcon(MimeType mimeType) {
        // CMS customizer
        INuxeoCustomizer cmsCustomizer = this.nuxeoService.getCMSCustomizer();

        // Icon
        String icon;

        if (mimeType == null) {
            icon = null;
        } else {
            // File document types
            List<FileDocumentType> types = cmsCustomizer.getFileDocumentTypes();

            icon = null;
            for (FileDocumentType type : types) {
                if (StringUtils.equals(mimeType.getPrimaryType(), type.getMimePrimaryType())) {
                    if (type.getMimeSubTypes().isEmpty()) {
                        icon = type.getIcon();
                    } else if (type.getMimeSubTypes().contains(mimeType.getSubType())) {
                        icon = type.getIcon();
                        break;
                    }
                }
            }
        }

        return icon;
    }

}
