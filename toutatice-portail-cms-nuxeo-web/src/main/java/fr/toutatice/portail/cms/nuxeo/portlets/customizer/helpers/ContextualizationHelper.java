/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.apache.commons.lang.BooleanUtils;
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


    public static String getLivePath(String path) {
        String result = path;
        if (path.endsWith(".proxy")) {
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
        // boolean isInContextualizedMode = false;

        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");

        if (window != null) {

            // Maximized windows
            if ("1".equals(window.getDeclaredProperty("osivia.cms.contextualization")))
                return true;

            // for spaceMenuBar fragment
            if( BooleanUtils.isTrue((Boolean) cmsCtx.getRequest().getAttribute("osivia.cms.menuBar.forceContextualization")))  {
               return true;
            }
           
            
        }


        return false;

    }


    /**
     * Détermine si le path courant impacte la navigation
     * 
     * @param cmsCtx
     * @param path
     * @return
     * @throws CMSException
     */
    public static Page getCurrentPage(CMSServiceCtx cmsCtx) throws CMSException {


        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
        if (window != null) {
            return window.getPage();
        }
        return null;

    }

}
