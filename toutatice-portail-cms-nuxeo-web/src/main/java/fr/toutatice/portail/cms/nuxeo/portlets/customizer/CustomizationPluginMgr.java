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
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.ICustomizationModule;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.customization.ICMSCustomizationObserver;
import org.osivia.portal.core.customization.ICustomizationService;

import fr.toutatice.portail.cms.nuxeo.api.Customizable;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.IMenubarModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.IPlayerModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;


/**
 * The Class CustomizationUtils.
 */
public class CustomizationPluginMgr implements ICMSCustomizationObserver {

    private static final Log logger = LogFactory.getLog(CustomizationPluginMgr.class);


    DefaultCMSCustomizer customizer;


    public CustomizationPluginMgr(DefaultCMSCustomizer customizer) {
        super();
        this.customizer = customizer;

        getCustomizationService().setCMSObserver(this);
    }



    /** The customization attributes cache. */
    private Map<Locale, Map<String, Object>> customizationAttributesCache = new Hashtable<Locale, Map<String, Object>>();

    /** The dest dispatcher for customized JSP */
    private Map<String, String> destDispatcher = new Hashtable<String, String>();

    /** The modules that defines players . */
    private List<IPlayerModule> dynamicModules = null;


    /** The customization cache ts. */
    private Map<Locale, Map<String, FragmentType>> fragmentsCache = new ConcurrentHashMap<Locale, Map<String, FragmentType>>();

    /** The editable window cache ts. */
    private Map<Locale, Map<String, EditableWindow>> ewCache = new ConcurrentHashMap<Locale, Map<String, EditableWindow>>();
    
    /** The liste templates. */
    private Map<Locale, List<ListTemplate>> templatesCache = new ConcurrentHashMap<Locale, List<ListTemplate>>();
    
    /** The menubar contributions. */
    private Map<Locale, List<IMenubarModule>> menubarCache = new ConcurrentHashMap<Locale, List<IMenubarModule>>();
    
    
    /** The types cache. */
    Map<String, CMSItemType> typesCache = null;
    
    /** The customization deployement ts. */
    long customizationDeployementTS=System.currentTimeMillis();


    /** Portal URL factory. */
    private ICustomizationService customizationService;


    private static final String CUSTOM_JSP_EXTENTION = "-custom-";
    private static final String WEB_INF_JSP = "/WEB-INF/jsp";


    /**
     * Gets the customization service.
     * 
     * @return the customization service
     */
    public ICustomizationService getCustomizationService() {
        if (customizationService == null) {
            customizationService = Locator.findMBean(ICustomizationService.class, ICustomizationService.MBEAN_NAME);
        }

        return customizationService;
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
            Map<String, Object> customizationAttributes = new Hashtable<String, Object>();
            CustomizationContext customizationContext = new CustomizationContext(customizationAttributes, locale);

            customizationService.customize(ICustomizationModule.PLUGIN_ID, customizationContext);

            customizationAttributesCache.put(locale, customizationAttributes);

        }

        return customizationAttributesCache.get(locale);


    }



    /**
     * Customize jsp new.
     * 
     * @param name the name
     * @param portletContext the portlet context
     * @param request the request
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String customizeJSP(String name, PortletContext portletContext, PortletRequest request) throws IOException {

        String customJSPName = destDispatcher.get(name);      
        if( customJSPName == null)
        {

            // Locale
            Locale locale = request.getLocale();
            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);
            
            // Default initialization
            destDispatcher.put(name, name); 

            Map<String, String> jsp = (Map<String, String>) customizationAttributes.get(Customizable.JSP.toString());
            if (name != null && jsp != null) {
                String jspKey = StringUtils.removeStart(name, WEB_INF_JSP);
                String originalPath = jsp.get(jspKey);
                if (originalPath != null) {

                    int extension = name.lastIndexOf('.');
                    if (extension != -1) {
                        // append custom name (the jsp musn't be overwritten)

                        String destinationName = WEB_INF_JSP + "/" + name.substring(0, extension) + CUSTOM_JSP_EXTENTION
                                + customizationDeployementTS + "." + name.substring(extension + 1);

                        destDispatcher.put(name, destinationName);

                        // Copy the original JSP
                        String dirPath = portletContext.getRealPath("/");
                        File destPath = new File(new File(dirPath), destDispatcher.get(name));

                        destPath.delete();

                        FileUtils.copyFile(new File(originalPath), destPath);
                    }
                }
            }   


        }



        return destDispatcher.get(name);
    }


    /**
     * Customize list templates.
     * 
     * @param locale the locale
     * @param customizer the customizer
     * @return the list
     */
    public List<ListTemplate> customizeListTemplates(Locale locale) {


        List<ListTemplate> templates = templatesCache.get(locale);

        if (templates == null) {

            templates = customizer.initListTemplates(locale);


            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            Map<String, ListTemplate> templatesMap = ((Map<String, ListTemplate>) customizationAttributes.get(Customizable.LIST_TEMPLATE.toString() + locale));

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
    public Map<String, FragmentType> getFragments(Locale locale) {


        Map<String, FragmentType> fragments = fragmentsCache.get(locale);

        if (fragments == null) {

            List<FragmentType> initList = customizer.initListFragments(locale);

            fragments = new Hashtable<String, FragmentType>();

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
    public Map<String, EditableWindow> customizeEditableWindows(Locale locale) {


        Map<String, EditableWindow> ew = ewCache.get(locale);

        if (ew == null) {

        	Map<String, EditableWindow> initList = customizer.initListEditableWindows(locale);

            ew = new Hashtable<String, EditableWindow>();

            for (Map.Entry<String, EditableWindow> customEw : initList.entrySet()) {
                ew.put(customEw.getKey(), customEw.getValue());
            }


            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            Map<String, EditableWindow> ewList = (Map<String, EditableWindow>) customizationAttributes.get(Customizable.EDITABLE_WINDOW.toString() + locale);

            if (ewList != null) {

                for (Map.Entry<String, EditableWindow> customEw  : ewList.entrySet()) {
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
    public Map<String, CMSItemType> customizeCMSItemTypes() {


        if (typesCache == null) {


            List<CMSItemType> defaultTypes = customizer.getDefaultCMSItemTypes();
            typesCache = new LinkedHashMap<String, CMSItemType>(defaultTypes.size());
            
            
            for (CMSItemType defaultType : defaultTypes) {
                typesCache.put(defaultType.getName(), defaultType);
            }

            List<CMSItemType> customizedTypes = customizer.getCustomizedCMSItemTypes();
            for (CMSItemType customizedType : customizedTypes) {
                typesCache.put(customizedType.getName(), customizedType);
            }


            Map<String, Object> customizationAttributes = getCustomizationAttributes(Locale.getDefault());

            Map<String, CMSItemType> customDocTypes = (Map<String, CMSItemType>) customizationAttributes.get(Customizable.DOC_TYPE.toString());

            if (customDocTypes != null) {
                Set<Entry<String, CMSItemType>> customTypes = customDocTypes.entrySet();
                for (Entry<String, CMSItemType> customType : customTypes) {
                    typesCache.put(customType.getKey(), customType.getValue());
                }
            }


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
    public List<IMenubarModule> customizeMenubars(Locale locale) {


        List<IMenubarModule> menubars = menubarCache.get(locale);

        if (menubars == null) {
        	
        	menubars = new ArrayList<IMenubarModule>();

            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            List<IMenubarModule> menubarList = (List<IMenubarModule>) customizationAttributes.get(Customizable.MENUBAR.toString());

            if (menubarList != null) {

                for (IMenubarModule customMenubarModule : menubarList) {
                    menubars.add(customMenubarModule);
                }
            }

            menubarCache.put(locale, menubars);
        }


        return menubars;

    }

    

    /**
     * Customize modules.
     * 
     * @param ctx the ctx
     * @return the list
     */
    public List<IPlayerModule> customizeModules(CMSServiceCtx ctx) {
        if (dynamicModules == null) {
            Map<String, Object> customizationAttributes = getCustomizationAttributes(Locale.getDefault());
            List<IPlayerModule> players = (List<IPlayerModule>) customizationAttributes.get(Customizable.PLAYER.toString());

            dynamicModules = new ArrayList<IPlayerModule>();
            if (players != null)
                dynamicModules.addAll(players);

        }

        return dynamicModules;

    }


    @Override
    public void notifyDeployment() {
        // Init all local caches
        
        customizationDeployementTS = System.currentTimeMillis();

        customizationAttributesCache = new Hashtable<Locale, Map<String, Object>>();

        destDispatcher = new Hashtable<String, String>();

        dynamicModules = null;

        fragmentsCache = new ConcurrentHashMap<Locale, Map<String, FragmentType>>();
        
        ewCache = new ConcurrentHashMap<Locale, Map<String, EditableWindow>>();

        templatesCache = new ConcurrentHashMap<Locale, List<ListTemplate>>();
        
        typesCache = null;
        
        menubarCache = new ConcurrentHashMap<Locale, List<IMenubarModule>>();


    }


}
