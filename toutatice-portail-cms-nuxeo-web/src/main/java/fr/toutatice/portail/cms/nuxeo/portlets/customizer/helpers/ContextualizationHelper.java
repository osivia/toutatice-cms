/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.context.ControllerContextAdapter;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * @author david
 * 
 */
public abstract class ContextualizationHelper {
    
    
    public static String  getLivePath(String path){
        String result = path;
        if( path.endsWith(".proxy")) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }

    /**
     * Méthode permettant de déterminer si une portlet s'affiche
     * en mode contextualisé.
     * 
     * @param cmsCtx
     * @return
     * @throws CMSException
     */
    public static boolean isCurrentDocContextualized(CMSServiceCtx cmsCtx) throws CMSException {
  //      boolean isInContextualizedMode = false;
        
       Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
       
       if (window != null) {
           
           //Maximized windows
           if( "1".equals(window.getDeclaredProperty("osivia.cms.contextualization")))
               return true;
       
           // Non player items
           String basePath = window.getPage().getProperty("osivia.cms.basePath");
           boolean isPluggedOnSpace = StringUtils.isNotEmpty(basePath);

       
           if( isPluggedOnSpace && cmsCtx.getDoc() != null)    {
               String path = ((Document) cmsCtx.getDoc()).getPath();
               String navigationPath =  cmsCtx.getRequest().getParameter("osivia.cms.path");
               
               if( navigationPath != null && path != null && navigationPath.equals(getLivePath(path)))
                   return true;
               }
           }
     

        
        return false;
        
              /*
        if (window != null) {
            Page currentPage = window.getPage();
            String basePath = currentPage.getProperty("osivia.cms.basePath");
            boolean isPluggedOnSpace = StringUtils.isNotEmpty(basePath);
            
            
            if (isPluggedOnSpace) {
                if( cmsCtx.getDoc() != null)    {
                if( ((Document) cmsCtx.getDoc()).getPath().startsWith(basePath)) {
                    ICMSService cmsService = NuxeoController.getCMSService();
                    CMSItem infosConfig = cmsService.getSpaceConfig(cmsCtx, basePath);
                    String spaceContextualizeStatus = infosConfig.getProperties().get("contextualizeInternalContents");
                    isInContextualizedMode = "1".equals(spaceContextualizeStatus);
                    }
                }
            }
        }
        return isInContextualizedMode;
        */
    }

   
    
    /**
     * Détermine si le path courant impacte la navigation
     * 
     * @param cmsCtx
     * @param path
     * @return
     * @throws CMSException
     */
    public static Page  getCurrentPage(CMSServiceCtx cmsCtx) throws CMSException {


        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
        if (window != null) {
            return window.getPage();
        }
        return null;
        
    }

}
