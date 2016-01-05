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
import java.util.Hashtable;
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
import org.osivia.portal.api.player.IPlayerModule;
import org.osivia.portal.api.portlet.PortalGenericPortlet;

import fr.toutatice.portail.cms.nuxeo.api.Customizable;

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


    /**
     * Constructor.
     */
    public AbstractPluginPortlet() {
        super();
        this.classLoader = this.getClass().getClassLoader();
    }
    
    
    /**
     * Constructor.
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
        this.metadatas.setOrder(getOrder());

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
    public void customize(String customizationID, CustomizationContext context) {
        Map<String, Object> attributes = context.getAttributes();

        this.customizeCMSProperties(customizationID, context);

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
     * Utility method used to generate attributes bundles customization module metadatas.
     *
     * @param customizationID the customization id
     * @param context the context
     * @return metadatas
     */
    protected abstract void customizeCMSProperties(String customizationID, CustomizationContext context);


    /**
     * Gets the players.
     *
     * @param context the context
     * @return the players
     */
    @SuppressWarnings("unchecked")
    protected List<IPlayerModule> getPlayers(CustomizationContext context) {
        // Players
        List<IPlayerModule> players = (List<IPlayerModule>) context.getAttributes().get(Customizable.PLAYER.toString());
        if (players == null) {
            players = new ArrayList<IPlayerModule>();
            context.getAttributes().put(Customizable.PLAYER.toString(), players);
        }
        return players;
    }


    /**
     * Gets the list templates.
     *
     * @param context the context
     * @return the list templates
     */
    @SuppressWarnings("unchecked")
    protected Map<String, ListTemplate> getListTemplates(CustomizationContext context) {
        // Lists
        Map<String, ListTemplate> templates = (Map<String, ListTemplate>) context.getAttributes().get(
                Customizable.LIST_TEMPLATE.toString() + context.getLocale());
        if (templates == null) {
            templates = new Hashtable<String, ListTemplate>();
            context.getAttributes().put(Customizable.LIST_TEMPLATE.toString() + context.getLocale(), templates);
        }
        return templates;
    }


    /**
     * Gets the doc types.
     *
     * @param context the context
     * @return the doc types
     */
    @SuppressWarnings("unchecked")
    protected Map<String, DocumentType> getDocTypes(CustomizationContext context) {
        Map<String, DocumentType> docTypes = (Map<String, DocumentType>) context.getAttributes().get(Customizable.DOC_TYPE.toString());
        if (docTypes == null) {
            docTypes = new Hashtable<String, DocumentType>();
            context.getAttributes().put(Customizable.DOC_TYPE.toString(), docTypes);
        }
        return docTypes;
    }
    
    
    /**
     * @param context
     * @param parentDocTypeName
     * @param childDocTypeName
     */
    protected void addSubType(CustomizationContext context, String parentDocTypeName, String childDocTypeName) {
        Map<String, DocumentType> docTypes = this.getDocTypes(context);
        
        DocumentType parentDocType = docTypes.get(parentDocTypeName);
        if(parentDocType != null)   {
            parentDocType.getPortalFormSubTypes().add(childDocTypeName);
         }
    }


    /**
     * Gets the fragment types.
     *
     * @param context the context
     * @return the fragment types
     */
    @SuppressWarnings("unchecked")
    protected List<FragmentType> getFragmentTypes(CustomizationContext context) {

        List<FragmentType> fragmentTypes = (List<FragmentType>) context.getAttributes().get(Customizable.FRAGMENT.toString() + context.getLocale());
        if (fragmentTypes == null) {
            fragmentTypes = new ArrayList<FragmentType>();
            context.getAttributes().put(Customizable.FRAGMENT.toString() + context.getLocale(), fragmentTypes);
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
        Map<String, EditableWindow> ew = (Map<String, EditableWindow>) context.getAttributes().get(
                Customizable.EDITABLE_WINDOW.toString() + context.getLocale());
        if (ew == null) {
            ew = new Hashtable<String, EditableWindow>();
            context.getAttributes().put(Customizable.EDITABLE_WINDOW.toString() + context.getLocale(), ew);
        }
        return ew;
    }


    /**
     * Gets the menubar contributors.
     *
     * @param context the context
     * @return the players
     */
    @SuppressWarnings("unchecked")
    protected List<IMenubarModule> getMenubars(CustomizationContext context) {
        // Players
        List<IMenubarModule> menubars = (List<IMenubarModule>) context.getAttributes().get(Customizable.MENUBAR.toString());
        if (menubars == null) {
            menubars = new ArrayList<IMenubarModule>();
            context.getAttributes().put(Customizable.MENUBAR.toString(), menubars);
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
        SortedMap<String, String> templates = (SortedMap<String, String>) context.getAttributes().get(
                Customizable.MENU_TEMPLATE.toString() + context.getLocale());
        if (templates == null) {
            templates = new TreeMap<String, String>();
            context.getAttributes().put(Customizable.MENU_TEMPLATE.toString() + context.getLocale(), templates);
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

}
