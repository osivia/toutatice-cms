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
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.customization.ICustomizationModule;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.customization.ICMSCustomizationObserver;
import org.osivia.portal.core.customization.ICustomizationService;

import fr.toutatice.portail.cms.nuxeo.api.Customizable;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.IMenubarModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.player.INuxeoPlayerModule;


/**
 * The Class CustomizationUtils.
 */
public class CustomizationPluginMgr implements ICMSCustomizationObserver {

    private static final Log logger = LogFactory.getLog(CustomizationPluginMgr.class);


    DefaultCMSCustomizer customizer;


    public CustomizationPluginMgr(DefaultCMSCustomizer customizer) {
        super();
        this.customizer = customizer;

        this.getCustomizationService().setCMSObserver(this);
    }



    /** The customization attributes cache. */
    private Map<Locale, Map<String, Object>> customizationAttributesCache = new Hashtable<Locale, Map<String, Object>>();

    /** The dest dispatcher for customized JSP */
    private Map<String, String> destDispatcher = new Hashtable<String, String>();

    /** The modules that defines players . */
    private List<INuxeoPlayerModule> dynamicModules = null;


    /** The customization cache ts. */
    private Map<Locale, Map<String, FragmentType>> fragmentsCache = new ConcurrentHashMap<Locale, Map<String, FragmentType>>();

    /** The editable window cache ts. */
    private Map<Locale, Map<String, EditableWindow>> ewCache = new ConcurrentHashMap<Locale, Map<String, EditableWindow>>();

    /** The liste templates. */
    private Map<Locale, List<ListTemplate>> templatesCache = new ConcurrentHashMap<Locale, List<ListTemplate>>();

    /** The menubar contributions. */
    private Map<Locale, List<IMenubarModule>> menubarCache = new ConcurrentHashMap<Locale, List<IMenubarModule>>();

    /** Menu templates cache. */
    private Map<Locale, SortedMap<String, String>> menuTemplatesCache = new ConcurrentHashMap<Locale, SortedMap<String, String>>();


    /** The types cache. */
    Map<String, DocumentType> typesCache = null;

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
        if (this.customizationService == null) {
            this.customizationService = Locator.findMBean(ICustomizationService.class, ICustomizationService.MBEAN_NAME);
        }

        return this.customizationService;
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
     * Customize jsp new.
     *
     * @param name the name
     * @param portletContext the portlet context
     * @param request the request
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String customizeJSP(String name, PortletContext portletContext, PortletRequest request) throws IOException {

        String customJSPName = this.destDispatcher.get(name);
        if( customJSPName == null)
        {

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
                        // append custom name (the jsp musn't be overwritten)

                        String destinationName = WEB_INF_JSP + "/" + name.substring(0, extension) + CUSTOM_JSP_EXTENTION
                                + this.customizationDeployementTS + "." + name.substring(extension + 1);

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
    public Map<String, EditableWindow> customizeEditableWindows(Locale locale) {


        Map<String, EditableWindow> ew = this.ewCache.get(locale);

        if (ew == null) {

        	Map<String, EditableWindow> initList = this.customizer.initListEditableWindows(locale);

            ew = new Hashtable<String, EditableWindow>();

            for (Map.Entry<String, EditableWindow> customEw : initList.entrySet()) {
                ew.put(customEw.getKey(), customEw.getValue());
            }


            Map<String, Object> customizationAttributes = this.getCustomizationAttributes(locale);

            Map<String, EditableWindow> ewList = (Map<String, EditableWindow>) customizationAttributes.get(Customizable.EDITABLE_WINDOW.toString() + locale);

            if (ewList != null) {

                for (Map.Entry<String, EditableWindow> customEw  : ewList.entrySet()) {
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

            SortedMap<?, ?> templatesMap = ((SortedMap<?, ?>) customizationAttributes.get(Customizable.MENU_TEMPLATE.toString() + locale));

            if (templatesMap != null) {
                Set<?> customTemplates = templatesMap.entrySet();
                for (Object customTemplate : customTemplates) {
                    Entry<?, ?> entry = (Entry<?, ?>) customTemplate;
                    String key = (String) entry.getKey();
                    String label = (String) entry.getValue();

                    templates.put(key, label);
                }
            }

            this.menuTemplatesCache.put(locale, templates);
        }

        return templates;
    }


    @Override
    public void notifyDeployment() {
        // Init all local caches

        this.customizationDeployementTS = System.currentTimeMillis();

        this.customizationAttributesCache = new Hashtable<Locale, Map<String, Object>>();

        this.destDispatcher = new Hashtable<String, String>();

        this.dynamicModules = null;

        this.fragmentsCache = new ConcurrentHashMap<Locale, Map<String, FragmentType>>();

        this.ewCache = new ConcurrentHashMap<Locale, Map<String, EditableWindow>>();

        this.templatesCache = new ConcurrentHashMap<Locale, List<ListTemplate>>();

        this.typesCache = null;

        this.menubarCache = new ConcurrentHashMap<Locale, List<IMenubarModule>>();

        this.menuTemplatesCache.clear();
    }


}
