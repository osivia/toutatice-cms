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
package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletException;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.CustomizationModuleMetadatas;
import org.osivia.portal.api.customization.ICustomizationModule;
import org.osivia.portal.api.customization.ICustomizationModulesRepository;
import org.osivia.portal.api.editor.EditorModule;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarModule;
import org.osivia.portal.api.player.IPlayerModule;
import org.osivia.portal.api.portlet.PortalGenericPortlet;
import org.osivia.portal.api.set.SetType;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.taskbar.TaskbarFactory;
import org.osivia.portal.api.taskbar.TaskbarItems;
import org.osivia.portal.api.theming.TabGroup;
import org.osivia.portal.api.theming.TemplateAdapter;
import org.osivia.portal.core.cms.DomainContextualization;

import fr.toutatice.portail.cms.nuxeo.api.Customizable;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;

/**
 * Plugin portlet abstract super-class.
 *
 * @see PortalGenericPortlet
 * @see ICustomizationModule
 */
public abstract class AbstractPluginPortlet extends PortalGenericPortlet implements ICustomizationModule {

    /** Customization modules repository attribute name. */
    private static final String ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY = "CustomizationModulesRepository";
    /** Customization modules repository attribute name. */
    public static final int DEFAULT_DEPLOYMENT_ORDER = 100;


    /** Customization module metadatas. */
    private CustomizationModuleMetadatas metadatas;
    /** Customization modules repository. */
    private ICustomizationModulesRepository repository;


    /** Class loader. */
    private final ClassLoader classLoader;

    /** Taskbar service. */
    private final ITaskbarService taskbarService;


    /**
     * Constructor.
     */
    public AbstractPluginPortlet() {
        super();
        this.classLoader = this.getClass().getClassLoader();

        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
    }


    /**
     * Get deployment order.
     * 
     * @return order
     */
    public int getOrder() {
        return DEFAULT_DEPLOYMENT_ORDER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws PortletException {
        super.init();

        // Metadatas
        this.metadatas = new CustomizationModuleMetadatas();
        this.metadatas.setName(this.getPluginName());
        this.metadatas.setModule(this);
        this.metadatas.setCustomizationIDs(Arrays.asList(ICustomizationModule.PLUGIN_ID));
        this.metadatas.setOrder(this.getOrder());

        // Repository
        this.repository = (ICustomizationModulesRepository) this.getPortletContext().getAttribute(ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY);
        this.repository.register(this.metadatas);
    }


    /**
     * Get plugin name.
     *
     * @return plugin name
     */
    protected abstract String getPluginName();


    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        this.repository.unregister(this.metadatas);
    }


    /**
     * Parse and register customized JavaServer pages.
     *
     * @param directoryPath directory path
     * @param directory directory
     * @param customizedPages customized JavaServer pages
     */
    public void parseJavaServerPages(String directoryPath, File directory, Map<String, CustomizedJsp> customizedPages) {
        // Directory children
        File[] children = directory.listFiles();
        for (File child : children) {
            if (child.isFile()) {
                String absolutePath = child.getAbsolutePath();
                String relativePath = StringUtils.removeStart(absolutePath, directoryPath);
                CustomizedJsp customizedPage = new CustomizedJsp(absolutePath, this.classLoader);

                customizedPages.put(relativePath, customizedPage);
            }
            if (child.isDirectory()) {
                this.parseJavaServerPages(directoryPath, child, customizedPages);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void customize(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        CLASS_LOADER_CONTEXT.set(this.classLoader);

        this.customizeCMSProperties(context);

        // Customized JavaServer pages
        Map<String, CustomizedJsp> customizedPages = (Map<String, CustomizedJsp>) attributes.get(Customizable.JSP.toString());
        if (customizedPages == null) {
            customizedPages = new ConcurrentHashMap<String, CustomizedJsp>();
            attributes.put(Customizable.JSP.toString(), customizedPages);
        }

        // Parse JavaServer pages
        String directoryPath = this.getPortletContext().getRealPath("/WEB-INF/custom/jsp");
        File directory = new File(directoryPath);
        if (directory.exists()) {
            this.parseJavaServerPages(directoryPath, directory, customizedPages);
        }
    }


    /**
     * Generate customization module metadatas.
     *
     * @param context customization context
     */
    protected abstract void customizeCMSProperties(CustomizationContext context);


    /**
     * Gets the players.
     *
     * @param context the context
     * @return the players
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<IPlayerModule> getPlayers(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Players
        List<IPlayerModule> players = (List<IPlayerModule>) attributes.get(Customizable.PLAYER.toString());
        if (players == null) {
            players = new ArrayList<IPlayerModule>();
            attributes.put(Customizable.PLAYER.toString(), players);
        }
        return players;
    }


    /**
     * Get list templates.
     *
     * @param context the context
     * @return list templates
     */
    @SuppressWarnings("unchecked")
    protected Map<String, ListTemplate> getListTemplates(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // List templates
        Map<String, ListTemplate> templates = (Map<String, ListTemplate>) attributes.get(Customizable.LIST_TEMPLATE.toString() + context.getLocale());
        if (templates == null) {
            templates = new ConcurrentHashMap<String, ListTemplate>();
            attributes.put(Customizable.LIST_TEMPLATE.toString() + context.getLocale(), templates);
        }
        return templates;
    }


    /**
     * Get document types.
     *
     * @param context customization context
     * @return document types
     */
    @SuppressWarnings("unchecked")
    protected Map<String, DocumentType> getDocTypes(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Document types
        Map<String, DocumentType> docTypes = (Map<String, DocumentType>) attributes.get(Customizable.DOC_TYPE.toString());
        if (docTypes == null) {
            docTypes = new ConcurrentHashMap<String, DocumentType>();
            attributes.put(Customizable.DOC_TYPE.toString(), docTypes);
        }
        return docTypes;
    }


    /**
     * Add document subtype.
     *
     * @param context customization context
     * @param parentDocTypeName parent document type name
     * @param childDocTypeName child document type name
     */
    protected void addSubtype(CustomizationContext context, String parentDocTypeName, String childDocTypeName) {
        Map<String, DocumentType> docTypes = this.getDocTypes(context);

        DocumentType parentDocType = docTypes.get(parentDocTypeName);
        if (parentDocType != null) {
            parentDocType.getSubtypes().add(childDocTypeName);
        }
    }


    /**
     * Get the fragment types.
     *
     * @param context the context
     * @return the fragment types
     */
    @SuppressWarnings("unchecked")
    protected List<FragmentType> getFragmentTypes(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Fragment types
        List<FragmentType> fragmentTypes = (List<FragmentType>) attributes.get(Customizable.FRAGMENT.toString() + context.getLocale());
        if (fragmentTypes == null) {
            fragmentTypes = new ArrayList<FragmentType>();
            attributes.put(Customizable.FRAGMENT.toString() + context.getLocale(), fragmentTypes);
        }
        return fragmentTypes;
    }

    /**
     * Get the editable windows types
     *
     * @param context the context
     * @return the EW list
     */
    @SuppressWarnings("unchecked")
    protected Map<String, EditableWindow> getEditableWindows(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Editable windows
        Map<String, EditableWindow> ew = (Map<String, EditableWindow>) attributes.get(Customizable.EDITABLE_WINDOW.toString() + context.getLocale());
        if (ew == null) {
            ew = new ConcurrentHashMap<String, EditableWindow>();
            attributes.put(Customizable.EDITABLE_WINDOW.toString() + context.getLocale(), ew);
        }
        return ew;
    }


    /**
     * Get menubar modules.
     *
     * @param context customization modules
     * @return menubar modules
     */
    @SuppressWarnings("unchecked")
    protected List<MenubarModule> getMenubarModules(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Players
        List<MenubarModule> menubars = (List<MenubarModule>) attributes.get(Customizable.MENUBAR.toString());
        if (menubars == null) {
            menubars = new ArrayList<MenubarModule>();
            attributes.put(Customizable.MENUBAR.toString(), menubars);
        }
        return menubars;
    }


    /**
     * Get menu templates.
     *
     * @param context customization context
     * @return menu templates
     */
    @SuppressWarnings("unchecked")
    protected SortedMap<String, String> getMenuTemplates(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Menu templates
        SortedMap<String, String> templates = (SortedMap<String, String>) attributes.get(Customizable.MENU_TEMPLATE.toString() + context.getLocale());
        if (templates == null) {
            templates = new TreeMap<String, String>();
            attributes.put(Customizable.MENU_TEMPLATE.toString() + context.getLocale(), templates);
        }
        return templates;
    }


    /**
     * Get navigation adapters.
     *
     * @param context customization context
     * @return navigation adapters
     */
    @SuppressWarnings("unchecked")
    protected List<INavigationAdapterModule> getNavigationAdapters(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Navigation adapters
        List<INavigationAdapterModule> adapters = (List<INavigationAdapterModule>) attributes.get(Customizable.NAVIGATION_ADAPTERS.toString());
        if (adapters == null) {
            adapters = new ArrayList<INavigationAdapterModule>();
            attributes.put(Customizable.NAVIGATION_ADAPTERS.toString(), adapters);
        }
        return adapters;
    }


    /**
     * Get domain contextualizations.
     *
     * @param context customization context
     * @return domain contextualizations
     */
    @SuppressWarnings("unchecked")
    protected List<DomainContextualization> getDomainContextualizations(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Navigation adapters
        List<DomainContextualization> contextualizations = (List<DomainContextualization>) attributes.get(Customizable.DOMAIN_CONTEXTUALIZATION.toString());
        if (contextualizations == null) {
            contextualizations = new ArrayList<DomainContextualization>();
            attributes.put(Customizable.DOMAIN_CONTEXTUALIZATION.toString(), contextualizations);
        }
        return contextualizations;
    }


    /**
     * Get tab groups.
     *
     * @param context customization context
     * @return tab groups
     */
    @SuppressWarnings("unchecked")
    protected Map<String, TabGroup> getTabGroups(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Tab groups
        Map<String, TabGroup> tabGroups = (Map<String, TabGroup>) attributes.get(Customizable.TAB_GROUPS.toString());
        if (tabGroups == null) {
            tabGroups = new ConcurrentHashMap<String, TabGroup>();
            attributes.put(Customizable.TAB_GROUPS.toString(), tabGroups);
        }
        return tabGroups;
    }


    /**
     * Get taskbar items.
     *
     * @param context customization context
     * @return taskbar items
     */
    protected TaskbarItems getTaskbarItems(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        TaskbarItems taskbarItems = (TaskbarItems) attributes.get(Customizable.TASKBAR_ITEMS.toString());
        if (taskbarItems == null) {
            TaskbarFactory factory = this.taskbarService.getFactory();
            taskbarItems = factory.createTaskbarItems();
            attributes.put(Customizable.TASKBAR_ITEMS.toString(), taskbarItems);
        }
        return taskbarItems;
    }


    /**
     * Get template adapters.
     *
     * @param context customization context
     * @return template adapters
     */
    @SuppressWarnings("unchecked")
    protected List<TemplateAdapter> getTemplateAdapters(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        List<TemplateAdapter> templateAdapters = (List<TemplateAdapter>) attributes.get(Customizable.TEMPLATE_ADAPTERS.toString());
        if (templateAdapters == null) {
            templateAdapters = new ArrayList<TemplateAdapter>();
            attributes.put(Customizable.TEMPLATE_ADAPTERS.toString(), templateAdapters);
        }
        return templateAdapters;
    }


    /**
     * Get form filters.
     *
     * @param context customization context
     * @return form filters
     */
    @SuppressWarnings("unchecked")
    protected Map<String, FormFilter> getFormFilters(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        Map<String, FormFilter> filters = (Map<String, FormFilter>) attributes.get(Customizable.FORM_FILTERS.toString());
        if (filters == null) {
            filters = new ConcurrentHashMap<String, FormFilter>();
            attributes.put(Customizable.FORM_FILTERS.toString(), filters);
        }
        return filters;
    }

    /**
     * Get Set types
     * @param context customization context
     * @return set types
     */
    @SuppressWarnings("unchecked")
	protected Map<String, SetType> getSetTypes(CustomizationContext context) {
    	// Customization context attributes
        Map<String, Object> attributes = context.getAttributes();
        
        Map<String, SetType> setTypes = (Map<String, SetType>) attributes.get(Customizable.SET_TYPES.toString());
        if (setTypes == null)
        {
        	setTypes = new ConcurrentHashMap<>();
        	attributes.put(Customizable.SET_TYPES.toString(), setTypes);
        }
        return setTypes;
    }


    /**
     * Get editor modules.
     *
     * @param customizationContext customization context
     * @return editor modules
     */
    protected List<EditorModule> getEditorModules(CustomizationContext customizationContext) {
        // Customization context attributes
        Map<String, Object> attributes = customizationContext.getAttributes();

        // Editor modules
        List<EditorModule> modules = (List<EditorModule>) attributes.get(Customizable.EDITOR_MODULES.toString());
        if (modules == null) {
            modules = new ArrayList<>();
            attributes.put(Customizable.EDITOR_MODULES.toString(), modules);
        }

        return modules;
    }


    /**
     * Getter for taskbarService.
     *
     * @return the taskbarService
     */
    public ITaskbarService getTaskbarService() {
        return this.taskbarService;
    }

}
