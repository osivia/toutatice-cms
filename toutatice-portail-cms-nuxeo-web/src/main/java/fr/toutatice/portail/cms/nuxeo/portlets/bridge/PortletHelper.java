/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.bridge;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * @author david
 *
 */
public abstract class PortletHelper {
    
    /**
     * Méthode permettant de déterminer si une portlet s'affiche 
     * en mode contextualisé.
     * @param cmsCtx
     * @return
     * @throws CMSException
     */
    public static  boolean isInContextualizedMode(CMSServiceCtx cmsCtx) throws CMSException{
        boolean isInContextualizedMode = false;
        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
        if (window != null){
            Page currentPage = window.getPage();
            String basePath = currentPage.getProperty("osivia.cms.basePath");
            boolean isPluggedOnSpace = StringUtils.isNotEmpty(basePath);
            if(isPluggedOnSpace){
                ICMSService cmsService = NuxeoController.getCMSService();
                CMSItem infosConfig = cmsService.getSpaceConfig(cmsCtx, basePath);
                String spaceContextualizeStatus = infosConfig.getProperties().get("contextualizeInternalContents");
                isInContextualizedMode = "1".equals(spaceContextualizeStatus);
            }
        }
        return isInContextualizedMode;
    }
    
}
