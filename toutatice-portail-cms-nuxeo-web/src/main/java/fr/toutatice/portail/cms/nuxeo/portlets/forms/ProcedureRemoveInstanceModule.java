package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.transaction.IPostcommitResource;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

/**
 * The Class ProcedureSendMailModule 
 * 
 * This class is implemented as postcommit because we the procedure instance must be deleted after commit
 */
public class ProcedureRemoveInstanceModule implements IPostcommitResource {


    /** The cms service. */
    ICMSService cmsService;
    
    /** The cms context. */

    CMSServiceCtx cmsContext;
    
    /** The procedure instance path. */
    String procedureInstancePath;

    /** Log. */
    private final static Log log = LogFactory.getLog(ProcedureRemoveInstanceModule.class);


    private final static Log procLogger = LogFactory.getLog("procedures");

    /**
     * Instantiates a new procedure remove instance module.
     *
     * @param cmsContext the cms context
     * @param cmsService the cms service
     * @param procedureInstancePath the procedure instance path
     */
    ProcedureRemoveInstanceModule( CMSServiceCtx cmsContext, ICMSService cmsService, String procedureInstancePath) {

        this.cmsContext = cmsContext;
        this.cmsService = cmsService;
        this.procedureInstancePath = procedureInstancePath;

    }

    @Override
    public void run() {
        try {
            // Save current scope
            String savedScope = cmsContext.getScope();
            String savedForcedScope = cmsContext.getForcePublicationInfosScope();
            boolean saveForceReload = cmsContext.isForceReload();

            try {
                // A procedure instance can be assigned model path several times
                cmsContext.setForceReload(true);
                cmsContext.setScope("superuser_no_cache");
                cmsContext.setForcePublicationInfosScope("superuser_no_cache");

                cmsService.deleteDocument(cmsContext, procedureInstancePath);
            } catch (CMSException e) {
                throw new PortalException(e);
            } finally {
                cmsContext.setForceReload(saveForceReload);
                cmsContext.setScope(savedScope);
                cmsContext.setForcePublicationInfosScope(savedForcedScope);
            }      

        
        } catch (Exception e) {
            this.log.error("ProcedureRemoveInstanceModule error - ", e.getCause());
        }
    }

}
