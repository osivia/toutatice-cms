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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;

import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.CustomizationModuleMetadatas;
import org.osivia.portal.api.customization.ICustomizationModulesRepository;
import org.osivia.portal.core.cms.CMSItemType;


// TODO: Auto-generated Javadoc
/**
 * The Class CMSCustomizerPortlet.
 */
public abstract class AbstractPluginPortlet extends GenericPortlet {


    /** Customization modules repository. */
    private ICustomizationModulesRepository repository;
    /** Internationalization customization module metadatas. */
    protected final CustomizationModuleMetadatas metadatas;


    /** The cl. */
    ClassLoader cl;

    /** Customization modules repository attribute name. */
    private static final String ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY = "CustomizationModulesRepository";


    /**
     * Constructor.
     */
    public AbstractPluginPortlet() {
        super();
        this.metadatas = this.generateMetadatas();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws PortletException {
        super.init();

  
        cl = Thread.currentThread().getContextClassLoader();

        this.repository = (ICustomizationModulesRepository) this.getPortletContext().getAttribute(ATTRIBUTE_CUSTOMIZATION_MODULES_REPOSITORY);
        this.repository.register(this.metadatas);
    }


    /**
     * Utility method used to generate attributes bundles customization module metadatas.
     * 
     * @return metadatas
     */
    protected abstract CustomizationModuleMetadatas generateMetadatas();


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
                parseJSP(customDirPath, child, jsp);
            }
        }
    }


    

    /**
     * {@inheritDoc}
     */
    public void customize(String customizationID, CustomizationContext context) {
        ClassLoader restoreLoader = null;

        try {
            // save current class loader


            Map<String, Object> attributes = context.getAttributes();

            customizeCMSProperties(customizationID,  context);
            
            // Parse and register JSP
            Map<String, String> jsp = (Map<String, String>) attributes.get("osivia.customizer.cms.jsp");
            if (jsp == null) {
                jsp = new Hashtable<String, String>();
                attributes.put("osivia.customizer.cms.jsp", jsp);
            }

            String dirPath = getPortletContext().getRealPath("/WEB-INF/custom/jsp");
            File f = new File(dirPath);
            if (f.exists())
                parseJSP(dirPath, f, jsp);


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
        List<IPlayerModule> modules = (List<IPlayerModule>) context.getAttributes().get("osivia.customizer.cms.modules");
        if (modules == null) {
            modules = new ArrayList<IPlayerModule>();
            context.getAttributes().put("osivia.customizer.cms.modules", modules);
        }
        return modules;
    }


    /**
     * Gets the list templates.
     *
     * @param context the context
     * @return the list templates
     */
    protected Map<String, ListTemplate> getListTemplates(CustomizationContext context) {
        // Lists
        Map<String, ListTemplate> templates = (Map<String, ListTemplate>) context.getAttributes().get("osivia.customizer.cms.template." + context.getLocale());
        if (templates == null) {
            templates = new Hashtable<String, ListTemplate>();
            context.getAttributes().put("osivia.customizer.cms.template." + context.getLocale(), templates);
        }
        return templates;
    }


    /**
     * Gets the doc types.
     *
     * @param context the context
     * @return the doc types
     */
    protected Map<String, CMSItemType> getDocTypes(CustomizationContext context) {
        Map<String, CMSItemType> docTypes = (Map<String, CMSItemType>) context.getAttributes().get("osivia.customizer.cms.doctype");
        if (docTypes == null) {
            docTypes = new Hashtable<String, CMSItemType>();
            context.getAttributes().put("osivia.customizer.cms.doctype", docTypes);
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

        List<FragmentType> fragmentTypes = (List<FragmentType>) context.getAttributes().get("osivia.customizer.cms.fragments." + context.getLocale());
        if (fragmentTypes == null) {
            fragmentTypes = new ArrayList<FragmentType>();
            context.getAttributes().put("osivia.customizer.cms.fragments." + context.getLocale(), fragmentTypes);
        }
        return fragmentTypes;
    }

    /**
     * Get the editable windows types
     * @param context the context
     * @return the EW list
     */
    protected Map<String, EditableWindow> getEditableWindows(CustomizationContext context) {
        Map<String, EditableWindow> ew = (Map<String, EditableWindow>) context.getAttributes().get("osivia.customizer.cms.ew." + context.getLocale());
        if (ew == null) {
            ew = new Hashtable<String, EditableWindow>();
            context.getAttributes().put("osivia.customizer.cms.ew." + context.getLocale(), ew);
        }
        return ew;
    }

}
