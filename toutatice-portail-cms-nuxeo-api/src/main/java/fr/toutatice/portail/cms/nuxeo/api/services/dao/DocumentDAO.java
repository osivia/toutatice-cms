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
import org.osivia.portal.api.locator.Locator;

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
                icon = StringUtils.defaultIfEmpty(this.getIcon(document), type.getGlyph());
            } else {
                icon = type.getGlyph();
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
        
        // Space type icon
        String spaceTypeStr = document.getProperties().getString("ttc:spaceType");
        if(spaceTypeStr != null ) {
	        DocumentType spaceType = this.getType(spaceTypeStr);
	        // Icon
	        if (spaceType != null) {
	            String icon = spaceType.getGlyph();
	            dto.setSpaceIcon(icon);
	        }
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
        Map<String, DocumentType> types = cmsCustomizer.getCMSItemTypes();
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
                	
                	if(document.getType().equals("DocxfFile") || document.getType().equals("OformFile")) {
                		return true;
                	}
                	else return OnlyofficeLiveEditHelper.isMimeTypeSupported(mimeType);
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
     * Get icon from document or attachment mime type.
     *
     * @param mimeType mime type
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

        // Icon
        String icon;
        boolean flaticon = false;
        if (mimeTypeObject == null) {
            icon = null;
        } else {
            String primaryType = mimeTypeObject.getPrimaryType();
            String subType = mimeTypeObject.getSubType();

            if ("application".equals(primaryType)) {
                // Application

                if ("pdf".equals(subType)) {
                    // PDF
                    icon = "pdf";
                    flaticon = true;
                } else if ("msword".equals(subType) || "vnd.openxmlformats-officedocument.wordprocessingml.document".equals(subType)) {
                    // MS Word
                    icon = "word";
                    flaticon = true;
                }
	            else if ("onlyoffice-docxf".equals(subType)) {
	                // MS Word
	                icon = "docxf";
	                flaticon = true;
	            } 
	            else if ("onlyoffice-oform".equals(subType)) {
	                // MS Word
	                icon = "oform";
	                flaticon = true;
	            } 
                else if ("vnd.ms-excel".equals(subType) || "vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(subType)
                        || "vnd.ms-excel.sheet.macroenabled.12".equals(subType)) {
                    // MS Excel
                    icon = "excel";
                    flaticon = true;
                } else if ("vnd.ms-powerpoint".equals(subType) || "vnd.openxmlformats-officedocument.presentationml.presentation".equals(subType)) {
                    // MS Powerpoint
                    icon = "powerpoint";
                    flaticon = true;
                } else if ("vnd.oasis.opendocument.text".equals(subType)) {
                    // OpenDocument - Text
                    icon = "odt";
                    flaticon = true;
                } else if ("vnd.oasis.opendocument.spreadsheet".equals(subType)) {
                    // OpenDocument - Spread sheet
                    icon = "ods";
                    flaticon = true;
                } else if ("vnd.oasis.opendocument.presentation".equals(subType)) {
                    // OpenDocument - Presentation
                    icon = "odp";
                    flaticon = true;
                } else if ("zip".equals(subType) || "gzip".equals(subType)) {
                    // Archive
                    icon = "archive";
                    flaticon = true;
                } else {
                    icon = null;
                }
            } else if ("text".equals(primaryType)) {
                // Text

                if ("html".equals(subType) || "xml".equals(subType)) {
                    // HTML or XML
                    icon = "xml";
                    flaticon = true;
                } else {
                    // Plain text
                    icon = "text";
                    flaticon = true;
                }
            } else if ("image".equals(primaryType)) {
                // Image
                icon = "picture";
            } else if ("video".equals(primaryType)) {
                // Video
                icon = "film";
            } else if ("audio".equals(primaryType)) {
                // Audio
                icon = "music";
            } else {
                icon = null;
            }
        }

        if (icon != null) {
            if (flaticon) {
                icon = "flaticon flaticon-" + icon;
            } else {
                icon = "glyphicons glyphicons-" + icon;
            }
        }

        return icon;
    }

}
