package fr.toutatice.portail.cms.nuxeo.repository;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.UniversalID;
import org.osivia.portal.api.cms.exception.CMSException;
import org.osivia.portal.api.cms.exception.DocumentForbiddenException;
import org.osivia.portal.api.cms.exception.DocumentNotFoundException;
import org.osivia.portal.api.cms.repository.BaseUserRepository;
import org.osivia.portal.api.cms.repository.BaseUserStorage;
import org.osivia.portal.api.cms.repository.UserData;
import org.osivia.portal.api.cms.repository.model.shared.RepositoryDocument;
import org.osivia.portal.api.cms.repository.model.shared.RepositorySpace;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.cms.Satellite;
import org.osivia.portal.core.cms.spi.NuxeoRepository;
import org.osivia.portal.core.cms.spi.NuxeoResult;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.workspace.WorkspaceType;


public class NuxeoUserStorage extends BaseUserStorage {


    private INuxeoService nuxeoService;

    private INuxeoCommandService nuxeoCommandService;

    private INuxeoService getNuxeoService() {

        if (this.nuxeoService == null) {

            this.nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");

        }
        return this.nuxeoService;
    }

    public INuxeoCommandService getNuxeoCommandService() throws Exception {
        if (this.nuxeoCommandService == null) {
            this.nuxeoCommandService = NuxeoCommandServiceFactory.getNuxeoCommandService(getNuxeoService().getCMSCustomizer().getPortletContext());
        }
        return this.nuxeoCommandService;
    }

    /**
     * Creates a default command context without cache
     *
     * @return the nuxeo command context
     */
    private NuxeoCommandContext createCommandContext(boolean superUser, boolean cache) {

        NuxeoCommandContext commandCtx = null;

        PortletContext cmsPortletContext = getNuxeoService().getCMSCustomizer().getPortletContext();

        if (getUserRepository().getPortalContext() != null) {
            commandCtx = new NuxeoCommandContext(cmsPortletContext, getUserRepository().getPortalContext());
        }

        if (commandCtx == null) {
            commandCtx = new NuxeoCommandContext(cmsPortletContext);
        }

        if (!cache)
            commandCtx.setForceReload(true);
        
        
        if( BaseUserRepository.SUPERUSER_NAME.equals(getUserRepository().getUserName()))
                superUser = true;

        // Par défaut
        if (superUser) {
            commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
            if (cache)
                commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
            else
                commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_NONE);
        } else {
            commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
            if (cache)
                commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_SESSION);
            else
                commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_NONE);
        }
        return commandCtx;
    }


    private NuxeoCommandContext createCommandContext(boolean superUser) {
        return createCommandContext(superUser, false);
    }


    @Override
    public RepositoryDocument reloadDocument(String internalID) throws CMSException {
       try {

           
             // Fetch doc (nocache)
           CMSPublicationInfos docPubInfos = (CMSPublicationInfos) ((NuxeoResult)executeCommand(createCommandContext( true), new PublishInfosCommand(IWebIdService.FETCH_PATH_PREFIX + internalID))).getResult();

//           if( internalID.equals("kFG8vy"))
//               System.out.println("**** RELOAD "+ internalID+ " " + docPubInfos.getDocumentPath());
           
           
           boolean getPublishedVersion;
           
           if( getUserRepository().isPreviewRepository())
               getPublishedVersion = false;
           else
               getPublishedVersion = docPubInfos.isPublished();
           
           Document nxDocument = fetchDocument(docPubInfos.getDocumentPath(), getPublishedVersion, false);
            
            Map<String, Object> properties = new HashMap<String, Object>();
            for (String key : nxDocument.getProperties().getKeys()) {
                properties.put(key, nxDocument.getProperties().get(key));
            }

              
            String spaceId = null;
            if( docPubInfos.getPublishSpacePath() != null)  {
                // Fetch space  (cache)
                CMSPublicationInfos spacePubInfos = (CMSPublicationInfos) ((NuxeoResult)executeCommand(createCommandContext( true, true), new PublishInfosCommand(docPubInfos.getPublishSpacePath() ))).getResult();

                Document space = fetchDocument(docPubInfos.getPublishSpacePath(), spacePubInfos.isPublished(), true);
                spaceId = space.getString("ttc:webid");
            }
                
            
            RepositoryDocument document;
            if( "Workspace".equals(nxDocument.getType()) || "PortalSite".equals(nxDocument.getType()))  {
                if( "Workspace".equals(nxDocument.getType()) )  {
                    properties.put("osivia.connect.templated", "false");
                    properties.put("osivia.connect.liveSpace", Boolean.TRUE);
                }
                String sTemplate = (String) properties.get("ttc:pageTemplate");
                
                UniversalID templateID;
                
                if(StringUtils.isNotEmpty(sTemplate)) {
                    templateID = new UniversalID( sTemplate);
                }   else
                    templateID = null;
                
                document= new RepositorySpace(getUserRepository(), nxDocument, internalID,
                        nxDocument.getPath().substring(nxDocument.getPath().lastIndexOf('/') + 1), null, internalID, null, properties, templateID);      
            }
            else
                document= new RepositoryDocument(getUserRepository(), nxDocument, internalID,
                    nxDocument.getPath().substring(nxDocument.getPath().lastIndexOf('/') + 1), null, spaceId, null, properties);
             
            return document;

        } catch (Exception e) {
            if (!(e instanceof CMSException))
                throw new CMSException(e);
            else
                throw (CMSException) e;
        } 
    }

    private Document fetchDocument(String path, boolean isPublished, boolean cache) throws CMSException {
        Document nxDocument;
        if (!isPublished)
            nxDocument = (Document) ((NuxeoResult) executeCommand(createCommandContext(true, cache), new DocumentFetchLiveCommand(path, "Read"))).getResult();
        else
            nxDocument = (Document) ((NuxeoResult) executeCommand(createCommandContext(true, cache), new DocumentFetchPublishedCommand(path))).getResult();
        return nxDocument;
    }


    @Override
    public UserData getUserData(String internalID) throws CMSException {

        CMSPublicationInfos res = null;

        res = (CMSPublicationInfos) ((NuxeoResult) executeCommand(createCommandContext(false, true),
                new PublishInfosCommand(IWebIdService.FETCH_PATH_PREFIX + internalID))).getResult();
        res.setSatellite(Satellite.MAIN);
        
        if(res.getErrorCodes().contains(CMSPublicationInfos.ERROR_CONTENT_FORBIDDEN))
            throw new DocumentForbiddenException();

        return res;
    }


    /**
     * Execute command.
     *
     * @param commandCtx the command ctx
     * @param command the command
     * @return the object
     * @throws Exception the exception
     */

    public NuxeoResult executeCommand(NuxeoCommandContext nuxeoCommandContext, INuxeoCommand command) throws CMSException {

        try {


            Object res = this.getNuxeoCommandService().executeCommand(nuxeoCommandContext, new INuxeoServiceCommand() {

                @Override
                public String getId() {
                    return command.getId();
                }

                @Override
                public Object execute(Session nuxeoSession) throws Exception {
                    return command.execute(nuxeoSession);
                }
                

            });


            return new NuxeoResult(res);


        } catch (Exception e) {
            if( e instanceof org.osivia.portal.core.cms.CMSException)   {
                org.osivia.portal.core.cms.CMSException cmsExc = (org.osivia.portal.core.cms.CMSException) e;
                if( cmsExc.getErrorCode() == cmsExc.ERROR_NOTFOUND)
                    throw new DocumentNotFoundException();
                if( cmsExc.getErrorCode() == cmsExc.ERROR_FORBIDDEN)
                    throw new DocumentForbiddenException();
             }
            if (!(e instanceof CMSException))
                throw new CMSException(e);
            else
                throw (CMSException) e;
        }
    }


    /**
     * Execute command.
     *
     * @param command the command
     * @return the nuxeo result
     * @throws CMSException the CMS exception
     */
    public NuxeoResult executeCommand(INuxeoCommand command) throws CMSException {

        return executeCommand(createCommandContext(false), command);

    }


    public void addDocument(String internalID, String type, String name,  boolean batchMode)  throws CMSException {
         if( "space".equals(type))  {
             executeCommand(createCommandContext(false, true),
                     new PublishSpaceCreationCommand(internalID)).getResult();
         }
    }


    public void updateDocument(String internalID, RepositoryDocument document, boolean batchMode)   throws CMSException {
        executeCommand(createCommandContext(false, true),
                new UpdateDocumentCommand(document)).getResult();
    }

    @Override
    public void endBatch() {
    }

    @Override
    public void beginBatch() {
    }


    public void deleteDocument(String internalID, boolean batchMode) throws CMSException {
       
    }

    @Override
    public void handleError() {
    }




}
