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


// TODO: Auto-generated Javadoc
/**
 * The Class CMSCustomizerPortlet.
 */
public abstract class AbstractPluginPortlet extends PortalGenericPortlet implements ICustomizationModule{


    /** Customization modules repository. */
    private ICustomizationModulesRepository repository;

    /** Internationalization customization module metadatas. */
    private CustomizationModuleMetadatas metadatas;


    /** The cl. */
    ClassLoader cl;

    /** Customization modules repository attribute name. */
    private static final String ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY = "CustomizationModulesRepository";

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws PortletException {
        super.init();


        this.cl = Thread.currentThread().getContextClassLoader();


//        if (this.getClass().getAnnotation(Plugin.class) != null) {
//            this.repository = (ICustomizationModulesRepository) this.getPortletContext().getAttribute(ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY);
//
    		CustomizationModuleMetadatas metadatas = new CustomizationModuleMetadatas();
//
//    		Plugin plugin = this.getClass().getAnnotation(Plugin.class);
//
    		metadatas.setName(this.getPluginName());
    		metadatas.setModule(this);
    		metadatas.setCustomizationIDs(Arrays.asList(ICustomizationModule.PLUGIN_ID));
    		metadatas.setOrder(100);

            this.metadatas = metadatas;
            //this.repository.register(this.metadatas);
//        }
//        else throw new PortletException("You should declare an id value with the @Plugin(''myPluginName'') annotation. ");

        this.repository = (ICustomizationModulesRepository) this.getPortletContext().getAttribute(ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY);
        this.repository.register(this.metadatas);
    }


	/**
	 * @return
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
    @Override
    public void customize(String customizationID, CustomizationContext context) {
        ClassLoader restoreLoader = null;

        try {
            // save current class loader


            Map<String, Object> attributes = context.getAttributes();

            this.customizeCMSProperties(customizationID,  context);

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
    protected Map<String, ListTemplate> getListTemplates(CustomizationContext context) {
        // Lists
        Map<String, ListTemplate> templates = (Map<String, ListTemplate>) context.getAttributes().get(Customizable.LIST_TEMPLATE.toString() + context.getLocale());
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
     * @param context the context
     * @return the EW list
     */
    protected Map<String, EditableWindow> getEditableWindows(CustomizationContext context) {
        Map<String, EditableWindow> ew = (Map<String, EditableWindow>) context.getAttributes().get(Customizable.EDITABLE_WINDOW.toString() + context.getLocale());
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

}
