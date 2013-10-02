package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;


public class CMSToWebPathAdapter {
    public String adaptCMSPathToWeb(CMSServiceCtx cmsCtx, String basePath, String requestPath, boolean webPath)  throws CMSException {
        
        if( webPath){
            return basePath + requestPath;
        }   else    {
                if (requestPath.startsWith(basePath))
                    return requestPath.substring(basePath.length());
                else 
                    return null;
            } 
        }
}
