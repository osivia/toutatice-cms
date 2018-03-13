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
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.ICustomizationModule;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarModule;
import org.osivia.portal.api.player.IPlayerModule;
import org.osivia.portal.api.taskbar.TaskbarItems;
import org.osivia.portal.api.theming.TabGroup;
import org.osivia.portal.api.theming.TemplateAdapter;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.DomainContextualization;
import org.osivia.portal.core.customization.ICMSCustomizationObserver;
import org.osivia.portal.core.customization.ICustomizationService;

import fr.toutatice.portail.cms.nuxeo.api.Customizable;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.INavigationAdapterModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;


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

    /** customizeJSPLockTable */
    private static final Hashtable<String, ReentrantLock> customizeJSPLockTable = new Hashtable<String, ReentrantLock>();

    /** CMS customizer. */
    private final DefaultCMSCustomizer customizer;
    /** Customization service. */
    private final ICustomizationService customizationService;

    /** Customization attributes cache. */
    private final Map<Locale, Map<String, Object>> customizationAttributesCache;
    /** Customized JavaServer pages cache. */
    private final Map<String, CustomizedJsp> customizedJavaServerPagesCache;
    /** Fragments cache. */
    private final Map<Locale, Map<String, FragmentType>> fragmentsCache;
    /** Editable window cache. */
    private final Map<Locale, Map<String, EditableWindow>> ewCache;
    /** List templates cache. */
    private final Map<Locale, List<ListTemplate>> templatesCache;
    /** Menu templates cache. */
    private final Map<Locale, SortedMap<String, String>> menuTemplatesCache;

    /** JSP reentrant locks. */
    private final Map<String, ReentrantLock> jspLocks;


    /** Dynamic modules that defines players . */
    private List<IPlayerModule> dynamicModules;
    /** Types cache. */
    private Map<String, DocumentType> typesCache;
    /** Navigation adapters cache. */
    private List<INavigationAdapterModule> navigationAdaptersCache;
    /** Domain contextualization cache. */
    private List<DomainContextualization> domainContextualizationCache;
    /** Tab groups cache. */
    private Map<String, TabGroup> tabGroupsCache;
    /** Taskbar items cache. */
    private TaskbarItems taskbarItemsCache;
    /** Menubar modules cache. */
    private List<MenubarModule> menubarModulesCache;
    /** Template adapters cache. */
    private List<TemplateAdapter> templateAdaptersCache;
    /** Navigation adapters cache. */
    private Map<String, FormFilter> formFiltersCache;

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
        customizationService = Locator.findMBean(ICustomizationService.class, ICustomizationService.MBEAN_NAME);
        customizationService.setCMSObserver(this);

        customizationAttributesCache = new ConcurrentHashMap<>();
        customizedJavaServerPagesCache = new ConcurrentHashMap<>();
        fragmentsCache = new ConcurrentHashMap<>();
        ewCache = new ConcurrentHashMap<>();
        templatesCache = new ConcurrentHashMap<>();
        menuTemplatesCache = new ConcurrentHashMap<>();

        // JSP locks
        jspLocks = new ConcurrentHashMap<>();

        customizationDeployementTS = System.currentTimeMillis();
    }


    /**
     * Gets the customization attributes.
     *
     * @param locale the locale
     * @return the customization attributes
     */
    private Map<String, Object> getCustomizationAttributes(Locale locale) {
        Map<String, Object> attributes = customizationAttributesCache.get(locale);
        if (attributes == null) {
            Map<String, Object> customizationAttributes = new ConcurrentHashMap<>();
            CustomizationContext customizationContext = new CustomizationContext(customizationAttributes, locale);

            /* Inject default types */
            List<DocumentType> defaultTypes = customizer.getDefaultCMSItemTypes();
            Map<String, DocumentType> types = Collections.synchronizedMap(new LinkedHashMap<String, DocumentType>(defaultTypes.size()));

            for (DocumentType defaultType : defaultTypes) {
                types.put(defaultType.getName(), defaultType.clone());
            }

            List<DocumentType> customizedTypes = customizer.getCustomizedCMSItemTypes();
            for (DocumentType customizedType : customizedTypes) {
                types.put(customizedType.getName(), customizedType.clone());
            }


            customizationAttributes.put(Customizable.DOC_TYPE.toString(), types);

            customizationService.customize(ICustomizationModule.PLUGIN_ID, customizationContext);


            customizationAttributesCache.put(locale, customizationAttributes);
        }

        return customizationAttributesCache.get(locale);
    }


    /**
     * Customize JavaServer page.
     *
     * @param name JSP name
     * @param portletContext portlet context
     * @param request portlet request
     * @return customized JavaServer page
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public CustomizedJsp customizeJSP(String name, PortletContext portletContext, PortletRequest request) throws IOException {
        // Customized JavaServer page
        CustomizedJsp customizedPage = customizedJavaServerPagesCache.get(name);

        if (customizedPage == null && name != null) {

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
                    Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

                    // Default initialization
                    customizedPage = new CustomizedJsp(name, null);

                    // Customized JavaServer pages
                    Map<String, CustomizedJsp> customizedPages = (Map<String, CustomizedJsp>) customizationAttributes.get(Customizable.JSP.toString());
                    if (name != null && customizedPages != null) {
                        String relativePath = StringUtils.removeStart(name, WEB_INF_JSP);
                        CustomizedJsp page = customizedPages.get(relativePath);

                        if (page != null && name.contains(".")) {
                            // Destination
                            StringBuilder destination = new StringBuilder();
                            destination.append(StringUtils.substringBeforeLast(name, "."));
                            destination.append(CUSTOM_JSP_EXTENTION);
                            destination.append(customizationDeployementTS);
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
                    customizedJavaServerPagesCache.put(name, customizedPage);
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
     * @param customizer the customizer
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<ListTemplate> customizeListTemplates(Locale locale) {
        List<ListTemplate> templates = templatesCache.get(locale);

        if (templates == null) {
            templates = customizer.initListTemplates(locale);

            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            Map<String, ListTemplate> templatesMap = (Map<String, ListTemplate>) customizationAttributes.get(Customizable.LIST_TEMPLATE.toString() + locale);

            if (templatesMap != null) {
                Set<Entry<String, ListTemplate>> customTemplates = templatesMap.entrySet();
                for (Entry<String, ListTemplate> customTemplate : customTemplates) {
                    templates.add(customTemplate.getValue());
                }
            }

            templatesCache.put(locale, templates);
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
        Map<String, FragmentType> fragments = fragmentsCache.get(locale);

        if (fragments == null) {
            List<FragmentType> initList = customizer.initListFragments(locale);
            fragments = new Hashtable<>();
            for (FragmentType fragment : initList) {
                fragments.put(fragment.getKey(), fragment);
            }

            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            List<FragmentType> fragmentsList = (List<FragmentType>) customizationAttributes.get(Customizable.FRAGMENT.toString() + locale);

            if (fragmentsList != null) {
                for (FragmentType customFragment : fragmentsList) {
                    fragments.put(customFragment.getKey(), customFragment);
                }
            }

            fragmentsCache.put(locale, fragments);
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
        Map<String, EditableWindow> ew = ewCache.get(locale);

        if (ew == null) {
            Map<String, EditableWindow> initList = customizer.initEditableWindows(locale);
            ew = new Hashtable<>();
            for (Map.Entry<String, EditableWindow> customEw : initList.entrySet()) {
                ew.put(customEw.getKey(), customEw.getValue());
            }

            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            Map<String, EditableWindow> ewList = (Map<String, EditableWindow>) customizationAttributes.get(Customizable.EDITABLE_WINDOW.toString() + locale);

            if (ewList != null) {
                for (Map.Entry<String, EditableWindow> customEw : ewList.entrySet()) {
                    ew.put(customEw.getKey(), customEw.getValue());
                }
            }

            ewCache.put(locale, ew);
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
        if (typesCache == null) {
            Map<String, Object> customizationAttributes = getCustomizationAttributes(Locale.getDefault());
            typesCache = (Map<String, DocumentType>) customizationAttributes.get(Customizable.DOC_TYPE.toString());
        }
        return typesCache;
    }


    /**
     * Customize list templates.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<MenubarModule> customizeMenubarModules() {
        if (menubarModulesCache == null) {
            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

            menubarModulesCache = (List<MenubarModule>) attributes.get(Customizable.MENUBAR.toString());
            if (menubarModulesCache == null) {
                menubarModulesCache = new ArrayList<>(0);
            }
        }
        return menubarModulesCache;
    }


    /**
     * Customize modules.
     *
     * @param ctx the ctx
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<IPlayerModule> customizeModules() {
        if (dynamicModules == null) {
            Map<String, Object> customizationAttributes = getCustomizationAttributes(Locale.getDefault());
            List<IPlayerModule> players = (List<IPlayerModule>) customizationAttributes.get(Customizable.PLAYER.toString());

            this.dynamicModules = new ArrayList<IPlayerModule>();
            if (players != null) {
                dynamicModules.addAll(players);
            }

        }

        return dynamicModules;

    }


    /**
     * Customize menu templates.
     *
     * @param locale current user locale
     * @return menu templates
     */
    public SortedMap<String, String> customizeMenuTemplates(Locale locale) {
        SortedMap<String, String> templates = menuTemplatesCache.get(locale);

        if (templates == null) {
            templates = customizer.initMenuTemplates(locale);

            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

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

            menuTemplatesCache.put(locale, templates);
        }

        return templates;
    }


    /**
     * Customize navigation adapters.
     *
     * @return navigation adapters
     */
    public List<INavigationAdapterModule> customizeNavigationAdapters() {
        if (navigationAdaptersCache == null) {
            // Cache
            navigationAdaptersCache = new ArrayList<>();

            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

            // Customized modules
            List<?> customizedModules = (List<?>) attributes.get(Customizable.NAVIGATION_ADAPTERS.toString());
            if (customizedModules != null) {
                CollectionUtils.addAll(navigationAdaptersCache, customizedModules.iterator());
            }
        }

        return navigationAdaptersCache;
    }


    /**
     * Customize domain contextualization.
     *
     * @return domain contextualization
     */
    public List<DomainContextualization> customizeDomainContextualization() {
        if (domainContextualizationCache == null) {
            // Cache
            domainContextualizationCache = new ArrayList<>();

            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

            // Customized modules
            List<?> customizedModules = (List<?>) attributes.get(Customizable.DOMAIN_CONTEXTUALIZATION.toString());
            if (customizedModules != null) {
                CollectionUtils.addAll(domainContextualizationCache, customizedModules.iterator());
            }
        }

        return domainContextualizationCache;
    }


    /**
     * Customize tab groups.
     *
     * @return tab groups
     */
    @SuppressWarnings("unchecked")
    public Map<String, TabGroup> customizeTabGroups() {
        if (tabGroupsCache == null) {
            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

            // Customized tab groups
            Map<String, TabGroup> tabGroups = (Map<String, TabGroup>) attributes.get(Customizable.TAB_GROUPS.toString());
            if (tabGroups == null) {
                tabGroupsCache = new ConcurrentHashMap<>();
            } else {
                tabGroupsCache = tabGroups;
            }

        }
        return tabGroupsCache;
    }


    /**
     * Customize taskbar items.
     *
     * @return taskbar items
     * @throws CMSException
     */
    public TaskbarItems customizeTaskbarItems() throws CMSException {
        if (taskbarItemsCache == null) {
            // Default taskbar items
            TaskbarItems defaultTaskbarItems = customizer.getDefaultTaskbarItems();

            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

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
        return taskbarItemsCache;
    }


    /**
     * Customize template adapters.
     *
     * @return template adapters
     */
    @SuppressWarnings("unchecked")
    public List<TemplateAdapter> customizeTemplateAdapters() {
        if (templateAdaptersCache == null) {
            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

            templateAdaptersCache = (List<TemplateAdapter>) attributes.get(Customizable.TEMPLATE_ADAPTERS.toString());
            if (templateAdaptersCache == null) {
                templateAdaptersCache = new ArrayList<>(0);
            }
        }
        return templateAdaptersCache;
    }


    /**
     * Customize form filters.
     *
     * @return form filters
     */
    @SuppressWarnings("unchecked")
    public Map<String, FormFilter> getFormFilters() {
        if (formFiltersCache == null) {
            // Customization attributes
            Map<String, Object> attributes = getCustomizationAttributes(Locale.getDefault());

            formFiltersCache = (Map<String, FormFilter>) attributes.get(Customizable.FORM_FILTERS.toString());
            if (formFiltersCache == null) {
                formFiltersCache = new ConcurrentHashMap<>(0);
            }
        }

        return formFiltersCache;
    }

    /**
     * lists the names of registered plugins
     *
     */
    public List<String> getRegisteredPluginNames() {
        return customizationService.getRegisteredPluginNames();
    }

    /**
     * Checks if a plugin with the provided name is registered
     *
     * @param pluginName
     */
    public boolean isPluginRegistered(String pluginName) {
        return customizationService.isPluginRegistered(pluginName);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDeployment() {
        // Reinit timestamp
        customizationDeployementTS = System.currentTimeMillis();

        // Reset caches
        dynamicModules = null;
        typesCache = null;
        navigationAdaptersCache = null;
        domainContextualizationCache = null;
        tabGroupsCache = null;
        taskbarItemsCache = null;
        menubarModulesCache = null;
        templateAdaptersCache = null;
        formFiltersCache = null;

        // Clear caches
        customizationAttributesCache.clear();
        customizedJavaServerPagesCache.clear();
        fragmentsCache.clear();
        ewCache.clear();
        templatesCache.clear();
        menuTemplatesCache.clear();
    }

}
