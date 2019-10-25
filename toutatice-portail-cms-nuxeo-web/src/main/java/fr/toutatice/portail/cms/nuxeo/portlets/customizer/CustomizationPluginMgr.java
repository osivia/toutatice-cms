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

import fr.toutatice.portail.cms.nuxeo.api.Customizable;
import fr.toutatice.portail.cms.nuxeo.api.domain.*;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;
import fr.toutatice.portail.cms.nuxeo.api.portlet.IPortletModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.ICustomizationModule;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarModule;
import org.osivia.portal.api.player.IPlayerModule;
import org.osivia.portal.api.set.SetType;
import org.osivia.portal.api.taskbar.TaskbarItems;
import org.osivia.portal.api.tasks.TaskModule;
import org.osivia.portal.api.theming.TabGroup;
import org.osivia.portal.api.theming.TemplateAdapter;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.DomainContextualization;
import org.osivia.portal.core.customization.ICMSCustomizationObserver;
import org.osivia.portal.core.customization.ICustomizationService;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Customization plugin manager
 *
 * @see ICMSCustomizationObserver
 */
public class CustomizationPluginMgr implements ICMSCustomizationObserver {

    /**
     * Custom JSP extension.
     */
    private static final String CUSTOM_JSP_EXTENSION = "-custom-";
    /**
     * JSP directory.
     */
    private static final String WEB_INF_JSP = "/WEB-INF/jsp";

    /**
     * customizeJSPLockTable
     */
    private static final Hashtable<String, ReentrantLock> customizeJSPLockTable = new Hashtable<>();

    /**
     * CMS customizer.
     */
    private final DefaultCMSCustomizer customizer;
    /**
     * Customization service.
     */
    private final ICustomizationService customizationService;

    /**
     * Customization attributes cache.
     */
    private final Map<Locale, Map<String, Object>> customizationAttributesCache;
    /**
     * Customized JavaServer pages cache.
     */
    private final Map<String, CustomizedJsp> customizedJavaServerPagesCache;
    /**
     * Fragments cache.
     */
    private final Map<Locale, Map<String, FragmentType>> fragmentsCache;
    /**
     * Editable window cache.
     */
    private final Map<Locale, Map<String, EditableWindow>> ewCache;
    /**
     * List templates cache.
     */
    private final Map<Locale, List<ListTemplate>> templatesCache;
    /**
     * Menu templates cache.
     */
    private final Map<Locale, SortedMap<String, String>> menuTemplatesCache;
    /**
     * Document modules cache.
     */
    private final Map<String, List<IPortletModule>> documentModulesCache;


    /**
     * Dynamic modules that defines players .
     */
    private List<IPlayerModule> dynamicModules;
    /**
     * Types cache.
     */
    private Map<String, DocumentType> typesCache;
    /**
     * Navigation adapters cache.
     */
    private List<INavigationAdapterModule> navigationAdaptersCache;
    /**
     * Domain contextualization cache.
     */
    private List<DomainContextualization> domainContextualizationCache;
    /**
     * Tab groups cache.
     */
    private Map<String, TabGroup> tabGroupsCache;
    /**
     * Taskbar items cache.
     */
    private TaskbarItems taskbarItemsCache;
    /**
     * Menubar modules cache.
     */
    private List<MenubarModule> menubarModulesCache;
    /**
     * Template adapters cache.
     */
    private List<TemplateAdapter> templateAdaptersCache;
    /**
     * Navigation adapters cache.
     */
    private Map<String, FormFilter> formFiltersCache;
    /**
     * Set types cache.
     */
    private Map<String, SetType> setTypesCache;
    /**
     * Task modules cache.
     */
    private List<TaskModule> taskModulesCache;


    /**
     * Customization deployement ts.
     */
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

        this.customizationAttributesCache = new ConcurrentHashMap<>();
        this.customizedJavaServerPagesCache = new ConcurrentHashMap<>();
        this.fragmentsCache = new ConcurrentHashMap<>();
        this.ewCache = new ConcurrentHashMap<>();
        this.templatesCache = new ConcurrentHashMap<>();
        this.menuTemplatesCache = new ConcurrentHashMap<>();
        this.documentModulesCache = new ConcurrentHashMap<>();

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
            Map<String, Object> customizationAttributes = new ConcurrentHashMap<>();
            CustomizationContext customizationContext = new CustomizationContext(customizationAttributes, locale);

            /* Inject default types */
            List<DocumentType> defaultTypes = this.customizer.getDefaultCMSItemTypes();
            Map<String, DocumentType> types = Collections.synchronizedMap(new LinkedHashMap<String, DocumentType>(defaultTypes.size()));

            for (DocumentType defaultType : defaultTypes) {
                types.put(defaultType.getName(), defaultType.clone());
            }

            List<DocumentType> customizedTypes = this.customizer.getCustomizedCMSItemTypes();
            for (DocumentType customizedType : customizedTypes) {
                types.put(customizedType.getName(), customizedType.clone());
            }


            customizationAttributes.put(Customizable.DOC_TYPE.toString(), types);

            this.customizationService.customize(ICustomizationModule.PLUGIN_ID, customizationContext);


            this.customizationAttributesCache.put(locale, customizationAttributes);
        }

        return this.customizationAttributesCache.get(locale);
    }


    /**
     * Customize JavaServer page.
     *
     * @param name           JSP name
     * @param portletContext portlet context
     * @param request        portlet request
     * @return customized JavaServer page
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public CustomizedJsp customizeJSP(String name, PortletContext portletContext, PortletRequest request) throws IOException {
        // Customized JavaServer page
        CustomizedJsp customizedPage = this.customizedJavaServerPagesCache.get(name);

        if ((customizedPage == null) && (name != null)) {

            ReentrantLock customizeJSPLock = customizeJSPLockTable.get(name);
            if (customizeJSPLock == null) {
                customizeJSPLock = new ReentrantLock();
                customizeJSPLockTable.put(name, customizeJSPLock);
            }
            customizeJSPLock.lock();
            try {
                customizedPage = this.customizedJavaServerPagesCache.get(name);
                if (customizedPage == null) {
                    // Locale
                    Locale locale = request.getLocale();
                    Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

                    // Default initialization
                    customizedPage = new CustomizedJsp(name, null);

                    // Customized JavaServer pages
                    Map<String, CustomizedJsp> customizedPages = (Map<String, CustomizedJsp>) customizationAttributes.get(Customizable.JSP.toString());
                    if ((name != null) && (customizedPages != null)) {
                        String relativePath = StringUtils.removeStart(name, WEB_INF_JSP);
                        CustomizedJsp page = customizedPages.get(relativePath);

                        if ((page != null) && name.contains(".")) {
                            // Destination
                            StringBuilder destination = new StringBuilder();
                            destination.append(StringUtils.substringBeforeLast(name, "."));
                            destination.append(CUSTOM_JSP_EXTENSION);
                            destination.append(this.customizationDeployementTS);
                            destination.append(".");
                            destination.append(StringUtils.substringAfterLast(name, "."));

                            // Copy original JSP
                            String directoryPath = portletContext.getRealPath("/");
                            File directory = new File(directoryPath);
                            File destinationFile = new File(directory, destination.toString());
                            destinationFile.delete();
                            File sourceFile = new File(page.getName());
                            FileUtils.copyFile(sourceFile, destinationFile);


                            customizedPage = new CustomizedJsp(destination.toString(), page.getClassLoader());
                        }
                    }
                    this.customizedJavaServerPagesCache.put(name, customizedPage);
                }
            } finally {
                customizeJSPLock.unlock();
            }
        }
        return customizedPage;
    }


    /**
     * Customize list templates.
     *
     * @param locale the locale
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<ListTemplate> customizeListTemplates(Locale locale) {
        List<ListTemplate> templates = this.templatesCache.get(locale);

        if (templates == null) {
            templates = this.customizer.initListTemplates(locale);

            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            Map<String, ListTemplate> templatesMap = (Map<String, ListTemplate>) customizationAttributes.get(Customizable.LIST_TEMPLATE.toString() + locale);

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
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, FragmentType> getFragments(Locale locale) {
        Map<String, FragmentType> fragments = this.fragmentsCache.get(locale);

        if (fragments == null) {
            List<FragmentType> initList = this.customizer.initListFragments(locale);
            fragments = new Hashtable<>();
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
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, EditableWindow> customizeEditableWindows(Locale locale) {
        Map<String, EditableWindow> ew = this.ewCache.get(locale);

        if (ew == null) {
            Map<String, EditableWindow> initList = this.customizer.initEditableWindows(locale);
            ew = new Hashtable<>();
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
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, DocumentType> customizeCMSItemTypes() {
        if (this.typesCache == null) {
            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(Locale.getDefault());
            this.typesCache = (Map<String, DocumentType>) customizationAttributes.get(Customizable.DOC_TYPE.toString());
        }
        return this.typesCache;
    }


    /**
     * Customize list templates.
     *
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<MenubarModule> customizeMenubarModules() {
        if (this.menubarModulesCache == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            this.menubarModulesCache = (List<MenubarModule>) attributes.get(Customizable.MENUBAR.toString());
            if (this.menubarModulesCache == null) {
                this.menubarModulesCache = new ArrayList<>(0);
            }
        }
        return this.menubarModulesCache;
    }


    /**
     * Customize modules.
     *
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<IPlayerModule> customizeModules() {
        if (this.dynamicModules == null) {
            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(Locale.getDefault());
            List<IPlayerModule> players = (List<IPlayerModule>) customizationAttributes.get(Customizable.PLAYER.toString());

            this.dynamicModules = new ArrayList<IPlayerModule>();
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

            SortedMap<?, ?> customizedTemplates = (SortedMap<?, ?>) customizationAttributes.get(Customizable.MENU_TEMPLATE.toString() + locale);

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
     * Customize navigation adapters.
     *
     * @return navigation adapters
     */
    public List<INavigationAdapterModule> customizeNavigationAdapters() {
        if (this.navigationAdaptersCache == null) {
            // Cache
            this.navigationAdaptersCache = new ArrayList<>();

            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            // Customized modules
            List<?> customizedModules = (List<?>) attributes.get(Customizable.NAVIGATION_ADAPTERS.toString());
            if (customizedModules != null) {
                CollectionUtils.addAll(this.navigationAdaptersCache, customizedModules.iterator());
            }
        }

        return this.navigationAdaptersCache;
    }


    /**
     * Customize domain contextualization.
     *
     * @return domain contextualization
     */
    public List<DomainContextualization> customizeDomainContextualization() {
        if (this.domainContextualizationCache == null) {
            // Cache
            this.domainContextualizationCache = new ArrayList<>();

            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            // Customized modules
            List<?> customizedModules = (List<?>) attributes.get(Customizable.DOMAIN_CONTEXTUALIZATION.toString());
            if (customizedModules != null) {
                CollectionUtils.addAll(this.domainContextualizationCache, customizedModules.iterator());
            }
        }

        return this.domainContextualizationCache;
    }


    /**
     * Customize tab groups.
     *
     * @return tab groups
     */
    @SuppressWarnings("unchecked")
    public Map<String, TabGroup> customizeTabGroups() {
        if (this.tabGroupsCache == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            // Customized tab groups
            Map<String, TabGroup> tabGroups = (Map<String, TabGroup>) attributes.get(Customizable.TAB_GROUPS.toString());
            if (tabGroups == null) {
                this.tabGroupsCache = new ConcurrentHashMap<>();
            } else {
                this.tabGroupsCache = tabGroups;
            }

        }
        return this.tabGroupsCache;
    }


    /**
     * Customize taskbar items.
     *
     * @return taskbar items
     * @throws CMSException
     */
    public TaskbarItems customizeTaskbarItems() throws CMSException {
        if (this.taskbarItemsCache == null) {
            // Default taskbar items
            TaskbarItems defaultTaskbarItems = this.customizer.getDefaultTaskbarItems();

            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            // Customized taskbar items
            TaskbarItems customizedTaskbarItems = (TaskbarItems) attributes.get(Customizable.TASKBAR_ITEMS.toString());

            if (defaultTaskbarItems != null) {
                this.taskbarItemsCache = defaultTaskbarItems;
                if (customizedTaskbarItems != null) {
                    this.taskbarItemsCache.add(customizedTaskbarItems.getAll());
                }
            } else if (customizedTaskbarItems != null) {
                this.taskbarItemsCache = customizedTaskbarItems;
            }
        }
        return this.taskbarItemsCache;
    }


    /**
     * Customize template adapters.
     *
     * @return template adapters
     */
    @SuppressWarnings("unchecked")
    public List<TemplateAdapter> customizeTemplateAdapters() {
        if (this.templateAdaptersCache == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            this.templateAdaptersCache = (List<TemplateAdapter>) attributes.get(Customizable.TEMPLATE_ADAPTERS.toString());
            if (this.templateAdaptersCache == null) {
                this.templateAdaptersCache = new ArrayList<>(0);
            }
        }
        return this.templateAdaptersCache;
    }


    /**
     * Customize form filters.
     *
     * @return form filters
     */
    @SuppressWarnings("unchecked")
    public Map<String, FormFilter> getFormFilters() {
        if (this.formFiltersCache == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            this.formFiltersCache = (Map<String, FormFilter>) attributes.get(Customizable.FORM_FILTERS.toString());
            if (this.formFiltersCache == null) {
                this.formFiltersCache = new ConcurrentHashMap<>(0);
            }
        }

        return this.formFiltersCache;
    }


    /**
     * Customize set types.
     *
     * @return set types
     */
    @SuppressWarnings("unchecked")
    public Map<String, SetType> getSetTypes() {
        if (this.setTypesCache == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());
            this.setTypesCache = (Map<String, SetType>) attributes.get(Customizable.SET_TYPES.toString());
            if (this.setTypesCache == null) {
                this.setTypesCache = new ConcurrentHashMap<>(0);
            }
        }
        return this.setTypesCache;
    }


    /**
     * Get task modules.
     *
     * @return task modules
     */
    public List<TaskModule> getTaskModules() {
        if (this.taskModulesCache == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            this.taskModulesCache = (List<TaskModule>) attributes.get(Customizable.TASK_MODULES.toString());
            if (this.taskModulesCache == null) {
                this.taskModulesCache = new ArrayList<>(0);
            }
        }
        return this.taskModulesCache;
    }


    /**
     * Get document modules.
     *
     * @param type document type
     * @return document modules
     */
    public List<IPortletModule> getDocumentModules(String type) {
        List<IPortletModule> modules = this.documentModulesCache.get(type);

        if (modules == null) {
            // Customization attributes
            Map<String, Object> attributes = this.getCustomizationAttributes(Locale.getDefault());

            // Attribute value
            Map<String, List<IPortletModule>> value = (Map<String, List<IPortletModule>>) attributes.get(Customizable.DOCUMENT_MODULES.toString());

            if (MapUtils.isNotEmpty(value)) {
                modules = value.get(type);
            }

            if (modules == null) {
                modules = new ArrayList<>();
            }

            // Update cache
            this.documentModulesCache.put(type, modules);
        }

        return modules;
    }


    /**
     * lists the names of registered plugins
     */
    public List<String> getRegisteredPluginNames() {
        return this.customizationService.getRegisteredPluginNames();
    }

    /**
     * Checks if a plugin with the provided name is registered
     *
     * @param pluginName
     */
    public boolean isPluginRegistered(String pluginName) {
        return this.customizationService.isPluginRegistered(pluginName);

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
        this.navigationAdaptersCache = null;
        this.domainContextualizationCache = null;
        this.tabGroupsCache = null;
        this.taskbarItemsCache = null;
        this.menubarModulesCache = null;
        this.templateAdaptersCache = null;
        this.formFiltersCache = null;
        this.setTypesCache = null;
        this.taskModulesCache = null;

        // Clear caches
        this.customizationAttributesCache.clear();
        this.customizedJavaServerPagesCache.clear();
        this.fragmentsCache.clear();
        this.ewCache.clear();
        this.templatesCache.clear();
        this.menuTemplatesCache.clear();
        this.documentModulesCache.clear();
    }

}
