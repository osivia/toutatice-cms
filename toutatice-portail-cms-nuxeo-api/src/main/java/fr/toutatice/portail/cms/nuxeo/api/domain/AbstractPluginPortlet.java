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

import javax.portlet.PortletException;

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


    /** Customization module metadatas. */
    private CustomizationModuleMetadatas metadatas;
    /** Customization modules repository. */
    private ICustomizationModulesRepository repository;


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
        this.metadatas.setOrder(100);

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
     * Parses and register the custom jsp.
     *
     * @param customDirPath the custom dir path
     * @param file the file
     * @param jsp the jsp
     */
    public void parseJSP(String customDirPath, File file, Map<String, String> jsp) {
        File[] filesList = file.listFiles();
        for (File child : filesList) {
            if (child.isFile()) {

                String relativePath = child.getAbsolutePath().substring(customDirPath.length());
                jsp.put(relativePath, child.getAbsolutePath());
            }

            if (child.isDirectory()) {
                this.parseJSP(customDirPath, child, jsp);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void customize(String customizationID, CustomizationContext context) {
        ClassLoader restoreLoader = null;

        try {
            // save current class loader
            Map<String, Object> attributes = context.getAttributes();

            this.customizeCMSProperties(customizationID, context);

            // Parse and register JSP
            Map<String, String> jsp = (Map<String, String>) attributes.get(Customizable.JSP.toString());
            if (jsp == null) {
                jsp = new Hashtable<String, String>();
                attributes.put(Customizable.JSP.toString(), jsp);
            }

            String dirPath = this.getPortletContext().getRealPath("/WEB-INF/custom/jsp");
            File f = new File(dirPath);
            if (f.exists()) {
                this.parseJSP(dirPath, f, jsp);
            }
        } finally {
            if (restoreLoader != null) {
                Thread.currentThread().setContextClassLoader(restoreLoader);
            }
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
    protected List<IPlayerModule<?>> getPlayers(CustomizationContext context) {
        // Players
        List<IPlayerModule<?>> players = (List<IPlayerModule<?>>) context.getAttributes().get(Customizable.PLAYER.toString());
        if (players == null) {
            players = new ArrayList<IPlayerModule<?>>();
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
     * Get CMS item adapter modules.
     *
     * @param context customization context
     * @return CMS item adapter modules
     */
    @SuppressWarnings("unchecked")
    protected List<ICmsItemAdapterModule> getCmsItemAdapterModules(CustomizationContext context) {
        // Customization context attributes
        Map<String, Object> attributes = context.getAttributes();

        // Modules
        List<ICmsItemAdapterModule> modules = (List<ICmsItemAdapterModule>) attributes.get(Customizable.CMS_ITEM_ADAPTER_MODULE.toString());
        if (modules == null) {
            modules = new ArrayList<ICmsItemAdapterModule>();
            attributes.put(Customizable.CMS_ITEM_ADAPTER_MODULE.toString(), modules);
        }
        return modules;
    }

}
