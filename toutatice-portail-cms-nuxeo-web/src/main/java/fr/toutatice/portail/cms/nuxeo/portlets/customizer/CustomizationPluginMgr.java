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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.ICustomizationModule;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.customization.ICMSCustomizationObserver;
import org.osivia.portal.core.customization.ICustomizationService;

import fr.toutatice.portail.cms.nuxeo.api.Customizable;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.ICmsItemAdapterModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.IMenubarModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.player.INuxeoPlayerModule;


/**
 * Customization plugin manager
 *
 * @see ICMSCustomizationObserver
 */
public class CustomizationPluginMgr implements ICMSCustomizationObserver {

    /** Custom JSP extension. */
    private static final String CUSTOM_JSP_EXTENTION = "-custom-";
    /** JSP directory. */
    private static final String WEB_INF_JSP = "/WEB-INF/jsp";


    /** CMS customizer. */
    private final DefaultCMSCustomizer customizer;
    /** Customization service. */
    private final ICustomizationService customizationService;

    /** Customization attributes cache. */
    private final Map<Locale, Map<String, Object>> customizationAttributesCache;
    /** Dest dispatcher for customized JSP */
    private final Map<String, String> destDispatcher;
    /** Fragments cache. */
    private final Map<Locale, Map<String, FragmentType>> fragmentsCache;
    /** Editable window cache. */
    private final Map<Locale, Map<String, EditableWindow>> ewCache;
    /** List templates cache. */
    private final Map<Locale, List<ListTemplate>> templatesCache;
    /** Menubar contributions cache. */
    private final Map<Locale, List<IMenubarModule>> menubarCache;
    /** Menu templates cache. */
    private final Map<Locale, SortedMap<String, String>> menuTemplatesCache;


    /** Dynamic modules that defines players . */
    private List<INuxeoPlayerModule> dynamicModules;
    /** Types cache. */
    private Map<String, DocumentType> typesCache;
    /** CMS item adapter modules cache. */
    private List<ICmsItemAdapterModule> cmsItemAdapterModulesCache;

    /** Customization deployement ts. */
    private long customizationDeployementTS;



    /**
     * Constructor.
     *
     * @param customizer CMS customizer
     */
    public CustomizationPluginMgr(DefaultCMSCustomizer customizer) {
        super();
        this.customizer = customizer;

        // Customization service
        this.customizationService = Locator.findMBean(ICustomizationService.class, ICustomizationService.MBEAN_NAME);
        this.customizationService.setCMSObserver(this);

        this.customizationAttributesCache = new ConcurrentHashMap<Locale, Map<String, Object>>();
        this.destDispatcher = new ConcurrentHashMap<String, String>();
        this.fragmentsCache = new ConcurrentHashMap<Locale, Map<String, FragmentType>>();
        this.ewCache = new ConcurrentHashMap<Locale, Map<String, EditableWindow>>();
        this.templatesCache = new ConcurrentHashMap<Locale, List<ListTemplate>>();
        this.menubarCache = new ConcurrentHashMap<Locale, List<IMenubarModule>>();
        this.menuTemplatesCache = new ConcurrentHashMap<Locale, SortedMap<String, String>>();

        this.customizationDeployementTS = System.currentTimeMillis();
    }


    /**
     * Gets the customization attributes.
     *
     * @param locale the locale
     * @return the customization attributes
     */
    private Map<String, Object> getCustomizationAttributes(Locale locale) {
        Map<String, Object> attributes = this.customizationAttributesCache.get(locale);
        if (attributes == null) {
            Map<String, Object> customizationAttributes = new Hashtable<String, Object>();
            CustomizationContext customizationContext = new CustomizationContext(customizationAttributes, locale);

            this.customizationService.customize(ICustomizationModule.PLUGIN_ID, customizationContext);

            this.customizationAttributesCache.put(locale, customizationAttributes);
        }

        return this.customizationAttributesCache.get(locale);
    }


    /**
     * Customize JSP.
     *
     * @param name the name
     * @param portletContext the portlet context
     * @param request the request
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public String customizeJSP(String name, PortletContext portletContext, PortletRequest request) throws IOException {

        String customJSPName = this.destDispatcher.get(name);
        if (customJSPName == null) {
            // Locale
            Locale locale = request.getLocale();
            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            // Default initialization
            this.destDispatcher.put(name, name);

            Map<String, String> jsp = (Map<String, String>) customizationAttributes.get(Customizable.JSP.toString());
            if ((name != null) && (jsp != null)) {
                String jspKey = StringUtils.removeStart(name, WEB_INF_JSP);
                String originalPath = jsp.get(jspKey);
                if (originalPath != null) {

                    int extension = name.lastIndexOf('.');
                    if (extension != -1) {
                        // Append custom name (the jsp musn't be overwritten)
                        String destinationName = WEB_INF_JSP + "/" + name.substring(0, extension) + CUSTOM_JSP_EXTENTION + this.customizationDeployementTS
                                + "." + name.substring(extension + 1);

                        this.destDispatcher.put(name, destinationName);

                        // Copy the original JSP
                        String dirPath = portletContext.getRealPath("/");
                        File destPath = new File(new File(dirPath), this.destDispatcher.get(name));

                        destPath.delete();

                        FileUtils.copyFile(new File(originalPath), destPath);
                    }
                }
            }
        }

        return this.destDispatcher.get(name);
    }


    /**
     * Customize list templates.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<ListTemplate> customizeListTemplates(Locale locale) {
        List<ListTemplate> templates = this.templatesCache.get(locale);

        if (templates == null) {
            templates = this.customizer.initListTemplates(locale);

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            Map<String, ListTemplate> templatesMap = ((Map<String, ListTemplate>) customizationAttributes.get(Customizable.LIST_TEMPLATE.toString() + locale));

            if (templatesMap != null) {
                Set<Entry<String, ListTemplate>> customTemplates = templatesMap.entrySet();
                for (Entry<String, ListTemplate> customTemplate : customTemplates) {
                    templates.add(customTemplate.getValue());
                }
            }

            this.templatesCache.put(locale, templates);
        }

        return templates;
    }


    /**
     * Customize fragments.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, FragmentType> getFragments(Locale locale) {
        Map<String, FragmentType> fragments = this.fragmentsCache.get(locale);

        if (fragments == null) {
            List<FragmentType> initList = this.customizer.initListFragments(locale);
            fragments = new Hashtable<String, FragmentType>();
            for (FragmentType fragment : initList) {
                fragments.put(fragment.getKey(), fragment);
            }

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            List<FragmentType> fragmentsList = (List<FragmentType>) customizationAttributes.get(Customizable.FRAGMENT.toString() + locale);

            if (fragmentsList != null) {
                for (FragmentType customFragment : fragmentsList) {
                    fragments.put(customFragment.getKey(), customFragment);
                }
            }

            this.fragmentsCache.put(locale, fragments);
        }

        return fragments;
    }


    /**
     * Customize editable window.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, EditableWindow> customizeEditableWindows(Locale locale) {
        Map<String, EditableWindow> ew = this.ewCache.get(locale);

        if (ew == null) {
            Map<String, EditableWindow> initList = this.customizer.initEditableWindows(locale);
            ew = new Hashtable<String, EditableWindow>();
            for (Map.Entry<String, EditableWindow> customEw : initList.entrySet()) {
                ew.put(customEw.getKey(), customEw.getValue());
            }

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            Map<String, EditableWindow> ewList = (Map<String, EditableWindow>) customizationAttributes.get(Customizable.EDITABLE_WINDOW.toString() + locale);

            if (ewList != null) {
                for (Map.Entry<String, EditableWindow> customEw : ewList.entrySet()) {
                    ew.put(customEw.getKey(), customEw.getValue());
                }
            }

            this.ewCache.put(locale, ew);
        }

        return ew;
    }


    /**
     * Customize cms item types.
     *
     * @param customizer the customizer
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, DocumentType> customizeCMSItemTypes() {
        if (this.typesCache == null) {
            List<DocumentType> defaultTypes = this.customizer.getDefaultCMSItemTypes();
            this.typesCache = new LinkedHashMap<String, DocumentType>(defaultTypes.size());

            for (DocumentType defaultType : defaultTypes) {
                this.typesCache.put(defaultType.getName(), defaultType);
            }

            List<DocumentType> customizedTypes = this.customizer.getCustomizedCMSItemTypes();
            for (DocumentType customizedType : customizedTypes) {
                this.typesCache.put(customizedType.getName(), customizedType);
            }

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(Locale.getDefault());

            Map<String, DocumentType> customDocTypes = (Map<String, DocumentType>) customizationAttributes.get(Customizable.DOC_TYPE.toString());

            if (customDocTypes != null) {
                Set<Entry<String, DocumentType>> customTypes = customDocTypes.entrySet();
                for (Entry<String, DocumentType> customType : customTypes) {
                    this.typesCache.put(customType.getKey(), customType.getValue());
                }
            }
        }

        return this.typesCache;
    }


    /**
     * Customize list templates.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<IMenubarModule> customizeMenubars(Locale locale) {
        List<IMenubarModule> menubars = this.menubarCache.get(locale);

        if (menubars == null) {
            menubars = new ArrayList<IMenubarModule>();

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            List<IMenubarModule> menubarList = (List<IMenubarModule>) customizationAttributes.get(Customizable.MENUBAR.toString());

            if (menubarList != null) {
                for (IMenubarModule customMenubarModule : menubarList) {
                    menubars.add(customMenubarModule);
                }
            }

            this.menubarCache.put(locale, menubars);
        }

        return menubars;
    }


    /**
     * Customize modules.
     *
     * @param ctx the ctx
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<INuxeoPlayerModule> customizeModules() {
        if (this.dynamicModules == null) {
            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(Locale.getDefault());
            List<INuxeoPlayerModule> players = (List<INuxeoPlayerModule>) customizationAttributes.get(Customizable.PLAYER.toString());

            this.dynamicModules = new ArrayList<INuxeoPlayerModule>();
            if (players != null) {
                this.dynamicModules.addAll(players);
            }

        }

        return this.dynamicModules;

    }


    /**
     * Customize menu templates.
     *
     * @param locale current user locale
     * @return menu templates
     */
    public SortedMap<String, String> customizeMenuTemplates(Locale locale) {
        SortedMap<String, String> templates = this.menuTemplatesCache.get(locale);

        if (templates == null) {
            templates = this.customizer.initMenuTemplates(locale);

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            SortedMap<?, ?> customizedTemplates = ((SortedMap<?, ?>) customizationAttributes.get(Customizable.MENU_TEMPLATE.toString() + locale));

            if (customizedTemplates != null) {
                Set<?> entrySet = customizedTemplates.entrySet();
                for (Object entry : entrySet) {
                    Entry<?, ?> customizedTemplate = (Entry<?, ?>) entry;
                    String key = (String) customizedTemplate.getKey();
                    String label = (String) customizedTemplate.getValue();

                    templates.put(key, label);
                }
            }

            this.menuTemplatesCache.put(locale, templates);
        }

        return templates;
    }


    /**
     * Customize CMS item adapter modules.
     *
     * @return CMS item adapter modules
     */
    public List<ICmsItemAdapterModule> customizeCmsItemAdapterModules() {
        if (this.cmsItemAdapterModulesCache == null) {
            // Modules
            this.cmsItemAdapterModulesCache = this.customizer.initCmsItemAdapterModules();

            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            // Customized modules
            List<?> customizedModules = (List<?>) attributes.get(Customizable.CMS_ITEM_ADAPTER_MODULE.toString());
            if (customizedModules != null) {
                CollectionUtils.addAll(this.cmsItemAdapterModulesCache, customizedModules.iterator());
            }
        }

        return this.cmsItemAdapterModulesCache;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDeployment() {
        // Reinit timestamp
        this.customizationDeployementTS = System.currentTimeMillis();

        // Reset caches
        this.dynamicModules = null;
        this.typesCache = null;
        this.cmsItemAdapterModulesCache = null;

        // Clear caches
        this.customizationAttributesCache.clear();
        this.destDispatcher.clear();
        this.fragmentsCache.clear();
        this.ewCache.clear();
        this.templatesCache.clear();
        this.menubarCache.clear();
        this.menuTemplatesCache.clear();
    }

}
