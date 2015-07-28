package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.customization.CustomizationContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.customization.ICustomizationService;

import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.IPlayerModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;



/**
 * The Class CustomizationUtils.
 */
public class CustomizationUtils {


 
    /** The configuration cache ts. */
    private static Hashtable<MultiKey, Long> configurationCacheTs = new Hashtable<MultiKey, Long>();
    
    /** The customization cache ts. */
    private static Hashtable<Locale, Long> customizationCacheTs = new Hashtable<Locale, Long>();
    
    /** The customization attributes cache. */
    private static Map<Locale, Map<String, Object>> customizationAttributesCache = new Hashtable<Locale, Map<String, Object>>();
    
    /** The dest dispatcher. */
    private static Map<String, String> destDispatcher = new Hashtable<String, String>();
    
    /** The modules. */
    private static List<IPlayerModule> dynamicModules = new ArrayList<IPlayerModule>();

    /** Portal URL factory. */
    private static ICustomizationService customizationService;


    private static final String CUSTOM_JSP_EXTENTION = "-custom-";
    private static final String WEB_INF_JSP = "/WEB-INF/jsp";

    
    /**
     * Gets the customization service.
     *
     * @return the customization service
     */
    public static ICustomizationService getCustomizationService() {
        if (customizationService == null) {
            customizationService = (ICustomizationService) Locator.findMBean(ICustomizationService.class, ICustomizationService.MBEAN_NAME);
        }

        return customizationService;
    }


    /**
     * Checks for to be updated.
     *
     * @param datasName the datas name
     * @param locale the locale
     * @return true, if successful
     */
    private static boolean hasToBeUpdated(String datasName, Locale locale) {

        // TODO : optimiser dans request

        ICustomizationService customizationService = getCustomizationService();

        MultiKey cacheKey = new MultiKey(datasName, locale);
        Long updateTs = configurationCacheTs.get(cacheKey);
        Long firstCustomization = customizationService.getFirstCustomizationTimestamp("osivia.customizer.cms.id", locale);

        if (updateTs == null)
            return true;


        if (firstCustomization.longValue() == 0)
            return true;

        if (updateTs.compareTo(firstCustomization) < 0)
            return true;

        return false;

    }


    /**
     * Gets the customization attributes.
     *
     * @param locale the locale
     * @return the customization attributes
     */
    private static Map<String, Object> getCustomizationAttributes(Locale locale) {

        Long updateTs = customizationCacheTs.get(locale);


        Long firstCustomization = getCustomizationService().getFirstCustomizationTimestamp("osivia.customizer.cms.id", locale);


        boolean updateCache = false;
        if (updateTs == null) {
            updateCache = true;
        } else if (firstCustomization.longValue() == 0) {
            updateCache = true;
        } else if (updateTs.compareTo(firstCustomization) < 0) {
            updateCache = true;
        }

        if (updateCache) {
            Map<String, Object> customizationAttributes = new Hashtable<String, Object>();
            CustomizationContext customizationContext = new CustomizationContext(customizationAttributes, locale);

            customizationService.customize("osivia.customizer.cms.id", customizationContext);

            customizationAttributesCache.put(locale, customizationAttributes);
            updateTs = System.currentTimeMillis();
            customizationCacheTs.put(locale, updateTs);
        }

        return customizationAttributesCache.get(locale);


    }


    /**
     * Update.
     *
     * @param datasName the datas name
     * @param locale the locale
     */
    public static void update(String datasName, Locale locale) {

        MultiKey cacheKey = new MultiKey(datasName, locale);
        long updateTs = System.currentTimeMillis();
        configurationCacheTs.put(cacheKey, updateTs);

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
    public static String customizeJSP(String name, PortletContext portletContext, PortletRequest request) throws IOException {

        if (hasToBeUpdated("JSP-" + name, null)) {

            // Locale
            Locale locale = request.getLocale();
            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            Map<String, String> jsp = (Map<String, String>) customizationAttributes.get("osivia.customizer.cms.jsp");
            if (name != null && jsp != null) {
                String jspKey = StringUtils.removeStart(name, WEB_INF_JSP);
                String originalPath = jsp.get(jspKey);
                if (originalPath != null) {

                    int extension = name.lastIndexOf('.');
                    if (extension != -1) {
                        // append custom name (the jsp musn't be overwritten)
                        
                        String destinationName = WEB_INF_JSP + "/" + name.substring(0, extension) + CUSTOM_JSP_EXTENTION + customizationCacheTs.get(Locale.getDefault())+ "." + name.substring(extension + 1);

                        destDispatcher.put(name, destinationName);

                        // Copy the original JSP
                        String dirPath = portletContext.getRealPath("/");
                        File destPath = new File(new File(dirPath), destDispatcher.get(name));
                        
                        destPath.delete();
                        
                        FileUtils.copyFile(new File(originalPath), destPath);
                    }
                }
            }

            // update timestamp
            update("JSP-" + name, null);
        }

        String customJSPName = destDispatcher.get(name);
        if (customJSPName == null)
            customJSPName = name;

        return customJSPName;
    }


    /**
     * Customize list templates.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the list
     */
    public static List<ListTemplate> customizeListTemplates(Locale locale, DefaultCMSCustomizer customizer) {


        List<ListTemplate> updatedTemplates = null;

        if (hasToBeUpdated("LIST", locale)) {

            updatedTemplates = customizer.initListTemplates(locale);


            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            Map<String, ListTemplate> templatesMap = ((Map<String, ListTemplate>) customizationAttributes.get("osivia.customizer.cms.template." + locale));

            if (templatesMap != null) {
                Set<Entry<String, ListTemplate>> customTemplates = templatesMap.entrySet();
                for (Entry<String, ListTemplate> customTemplate : customTemplates) {
                    updatedTemplates.add(customTemplate.getValue());
                }
            }
        }// update timestamp
        update("LIST", locale);

        return updatedTemplates;

    }


    /**
     * Customize fragments.
     *
     * @param locale the locale
     * @param customizer the customizer
     * @return the map
     */
    public static Map<String, FragmentType> customizeFragments(Locale locale, DefaultCMSCustomizer customizer) {


        Map<String, FragmentType> updatedFragments = null;

        if (hasToBeUpdated("FRAGMENT", locale)) {

            List<FragmentType> initList = customizer.initListFragments(locale);

            updatedFragments = new Hashtable<String, FragmentType>();

            for (FragmentType fragment : initList) {
                updatedFragments.put(fragment.getKey(), fragment);
            }


            Map<String, Object> customizationAttributes = getCustomizationAttributes(locale);

            List<FragmentType> fragmentsList = (List<FragmentType>) customizationAttributes.get("osivia.customizer.cms.fragments." + locale);

            if (fragmentsList != null) {

                for (FragmentType customFragment : fragmentsList) {
                    updatedFragments.put(customFragment.getKey(), customFragment);
                }
            }
        }// update timestamp
        update("FRAGMENT", locale);

        return updatedFragments;
    }


    /**
     * Customize cms item types.
     *
     * @param customizer the customizer
     * @return the map
     */
    public static Map<String, CMSItemType> customizeCMSItemTypes(DefaultCMSCustomizer customizer) {
        Map<String, CMSItemType> updatedTypes = null;

        if (hasToBeUpdated("TYPE", null)) {


            List<CMSItemType> defaultTypes = customizer.getDefaultCMSItemTypes();
            updatedTypes = new LinkedHashMap<String, CMSItemType>(defaultTypes.size());
            for (CMSItemType defaultType : defaultTypes) {
                updatedTypes.put(defaultType.getName(), defaultType);
            }

            List<CMSItemType> customizedTypes = customizer.getCustomizedCMSItemTypes();
            for (CMSItemType customizedType : customizedTypes) {
                updatedTypes.put(customizedType.getName(), customizedType);
            }


            Map<String, Object> customizationAttributes = getCustomizationAttributes(Locale.getDefault());

            Map<String, CMSItemType> customDocTypes = (Map<String, CMSItemType>) customizationAttributes.get("osivia.customizer.cms.doctype");

            if (customDocTypes != null) {
                Set<Entry<String, CMSItemType>> customTypes = customDocTypes.entrySet();
                for (Entry<String, CMSItemType> customType : customTypes) {
                    updatedTypes.put(customType.getKey(), customType.getValue());
                }
            }


        }// update timestamp
        update("TYPE", null);

        return updatedTypes;
    }


    /**
     * Customize modules.
     *
     * @param ctx the ctx
     * @return the list
     */
    public static List<IPlayerModule> customizeModules(CMSServiceCtx ctx) {
        if (hasToBeUpdated("MODULES", Locale.getDefault())) {
            Map<String, Object> customizationAttributes = getCustomizationAttributes(Locale.getDefault());
            List<IPlayerModule> players = (List<IPlayerModule>) customizationAttributes.get("osivia.customizer.cms.modules");
            
             dynamicModules.clear();

             if( players != null)
                dynamicModules.addAll(players);

        }
        update("MODULES", Locale.getDefault());

        return dynamicModules;

    }


}
