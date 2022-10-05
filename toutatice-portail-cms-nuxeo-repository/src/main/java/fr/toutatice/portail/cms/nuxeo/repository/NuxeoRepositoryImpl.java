package fr.toutatice.portail.cms.nuxeo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.EcmDocument;
import org.osivia.portal.api.cms.Symlinks;
import org.osivia.portal.api.cms.UniversalID;
import org.osivia.portal.api.cms.UpdateInformations;
import org.osivia.portal.api.cms.UpdateScope;
import org.osivia.portal.api.cms.exception.CMSException;
import org.osivia.portal.api.cms.exception.DocumentNotFoundException;
import org.osivia.portal.api.cms.model.Document;
import org.osivia.portal.api.cms.model.NavigationItem;
import org.osivia.portal.api.cms.model.Profile;
import org.osivia.portal.api.cms.repository.BaseUserRepository;
import org.osivia.portal.api.cms.repository.RepositoryFactory;
import org.osivia.portal.api.cms.repository.UserStorage;
import org.osivia.portal.api.cms.repository.cache.SharedRepository;
import org.osivia.portal.api.cms.repository.cache.SharedRepositoryKey;
import org.osivia.portal.api.cms.repository.model.shared.RepositoryDocument;
import org.osivia.portal.api.cms.service.GetChildrenRequest;
import org.osivia.portal.api.cms.service.Request;
import org.osivia.portal.api.cms.service.Result;
import org.osivia.portal.api.cms.service.SpaceCacheBean;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.DocumentsMetadata;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.cms.spi.NuxeoRepository;
import org.osivia.portal.core.cms.spi.NuxeoRequest;
import org.osivia.portal.core.cms.spi.NuxeoResult;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.producers.test.AdvancedRepository;



public class NuxeoRepositoryImpl extends BaseUserRepository implements NuxeoRepository, AdvancedRepository {


    private INuxeoService nuxeoService;

    /** Remote proxy webid marker. */
    public static final String RPXY_WID_MARKER = "_c_";

    /**
     * CMS service locator.
     */
    private final ICMSServiceLocator cmsServiceLocator;
    
    
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(NuxeoRepositoryImpl.class);

    private INuxeoService getNuxeoService() {

        if (this.nuxeoService == null) {

            this.nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");

        }
        return this.nuxeoService;
    }

    @Override
    public NuxeoUserStorage getUserStorage() {

        return (NuxeoUserStorage) super.getUserStorage();
    }

    public NuxeoRepositoryImpl(RepositoryFactory repositoryFactory, SharedRepositoryKey repositoryKey, BaseUserRepository publishRepository, String userName) {
        super(repositoryFactory, repositoryKey, publishRepository, userName, new NuxeoUserStorage());

        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);

    }

    @Override
    public Result executeRequest(Request request) throws CMSException {
        if (request instanceof NuxeoRequest) {
            return ((NuxeoUserStorage) super.getUserStorage()).executeCommand(((NuxeoRequest) request).getCommandContext(),
                    ((NuxeoRequest) request).getCommand());
        } else if (request instanceof GetChildrenRequest) {
            Document parent = getDocument(((GetChildrenRequest) request).getParentId().getInternalID());
            org.nuxeo.ecm.automation.client.model.Document nuxeoDoc = (org.nuxeo.ecm.automation.client.model.Document) parent.getNativeItem();
            return ((NuxeoUserStorage) super.getUserStorage()).executeCommand(new GetChildrenCommand(nuxeoDoc.getId()));
        }

        throw new CMSException(new IllegalArgumentException("Request not implemented"));
    }


    @Override
    public boolean supportPreview() {
        return true;
    }

    @Override
    protected void initDocuments() {
    }


    /**
     * Creates the command context.
     *
     * @param cache the cache
     * @return the nuxeo command context
     */
    private NuxeoCommandContext createCommandContext(boolean cache) {

        NuxeoCommandContext commandCtx = null;

        PortletContext cmsPortletContext = getNuxeoService().getCMSCustomizer().getPortletContext();

        commandCtx = new NuxeoCommandContext(cmsPortletContext);
        commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        if (cache)
            commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
        else
            commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_NONE);
        return commandCtx;
    }

    /**
     * Creates the command context.
     *
     * @return the nuxeo command context
     */
    private NuxeoCommandContext createCommandContext() {
        return createCommandContext(true);
    }
    
   
    
    /* Experimental feature for publish space                         */
    /* Optimized for performance : do not need to fetch each document */
    /* Page Refreshed are ignored                                     */

    
    
   private static DocumentsMetadata cacheMetadatas = null;
   private static Long cacheTs = null;   
    
   
   private synchronized DocumentsMetadata  getCacheMetaDataCommunityPrototype(String parentWebId) throws CMSException {

       CMSServiceCtx cmsContext = getNavigationCMSContext();

       try {

           SpaceCacheBean spaceCacheInformations = getSpaceCacheInformations(parentWebId);
           if (cacheTs == null 
              ||
              (spaceCacheInformations != null && spaceCacheInformations.getLastSpaceModification() != null &&  spaceCacheInformations.getLastSpaceModification() > cacheTs)
              ||
              ( cacheTs + SharedRepository.DOCUMENT_RELOAD_TIMEOUT < System.currentTimeMillis())
              )    {
 
               Symlinks symLinks = new Symlinks();
               symLinks.setLinks(new ArrayList<>());
              // Nuxeo command
               INuxeoCommand command = new DocumentsMetadataCommand( "/default-domain/communaute", RequestPublishStatus.published, symLinks, null);

               // Super-user scope
               String savedScope = cmsContext.getScope();
               cmsContext.setScope("superuser_no_cache");


               try {
                   cacheMetadatas = (DocumentsMetadata)  ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(false), command).getResult();
               } catch (CMSException e) {
                   throw e;
               } catch (Exception e) {
                   throw new CMSException(e);
               } finally {
                   cmsContext.setScope(savedScope);
               }
               
               cacheTs = System.currentTimeMillis();
           }

       } catch (Exception e) {
           throw new CMSException(e);
       }

       return cacheMetadatas;
   }
 
    

    @Override
    public String getInternalId(String path) throws CMSException {


        org.nuxeo.ecm.automation.client.model.Document nxDocument = null;
        String parentWebId = null;

        //TODO il faudrait fetcher tous les spaces pour voir auquel appartient le document
        // (le plus proche)
        
        if (path.startsWith("/default-domain/communaute/")) {

            try {
                CMSServiceCtx cmsContext = getNavigationCMSContext();
                
                
                CMSItem spaceConfig = cmsServiceLocator.getCMSService().getSpaceConfig(cmsContext, "/default-domain/communaute");
                parentWebId = spaceConfig.getWebId();
                  
                
                DocumentsMetadata metadatas = getCacheMetaDataCommunityPrototype( parentWebId);
                nxDocument = null;

                for (EcmDocument ecmDocument : metadatas.getDocuments()) {

                    org.nuxeo.ecm.automation.client.model.Document iterDoc = (org.nuxeo.ecm.automation.client.model.Document) ecmDocument;

                    if (iterDoc.getPath().equals(path)) {
                        // Ensure facet is set on remote proxy
                        if (!iterDoc.getFacets().list().contains("isRemoteProxy")) {
                            if (!iterDoc.getPath().endsWith(".proxy")) {
                                // Add facet
                                ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(false), new AddRemoteProxyFacetCommand(path)).getResult();
                                notifyUpdate(new UpdateInformations(new UniversalID("nx",(String) iterDoc.getString("ttc:webid")), new UniversalID("nx",parentWebId), UpdateScope.SCOPE_SPACE, false));
                                // reload
                                NuxeoCommandContext ctx = createCommandContext(false);
                                // to prevent to take the already fetch in same request
                                ctx.setForceReload(true);
                                iterDoc = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(ctx, new DocumentFetchPublishedCommand(path)).getResult();
                            }
                        }
                        
                        nxDocument = iterDoc;
                        break;
                    }
                }
                

                
            } catch (org.osivia.portal.core.cms.CMSException e) {
                throw new CMSException(e);
            }
            
            
            if( nxDocument == null) {
                LOG.warn("Document with path "+ path+" has not been found. Maybe cache is not refreshed");
            }
        } 
        
        
        if( nxDocument == null)  {
            /* Default treatment */

            CMSPublicationInfos res = (CMSPublicationInfos) ((NuxeoResult) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(), new PublishInfosCommand(path))).getResult();


            List<Integer> errors = res.getErrorCodes();
            if (errors != null) {
                if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_NOT_FOUND)) {
                    throw new DocumentNotFoundException();
                }
            }


            if (!res.isPublished())
                nxDocument = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(), new DocumentFetchLiveCommand(res.getDocumentPath(), "Read")).getResult();
            else {
                nxDocument = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(), new DocumentFetchPublishedCommand(res.getDocumentPath())).getResult();
            }
        }

        
        String internalId = (String) nxDocument.getString("ttc:webid");

        
        
        if (nxDocument.getFacets().list().contains("isRemoteProxy")) {
            // Get parent
            if( parentWebId == null)    {
                org.nuxeo.ecm.automation.client.model.Document parent = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(), new GetParentCommand(nxDocument)).getResult();
                parentWebId =  (String) parent.getString("ttc:webid");
            }
            
            internalId = internalId + RPXY_WID_MARKER + parentWebId;
        }


        return internalId;
    }

    @Override
    public String getPath(String internalId) throws CMSException {

        
        CMSPublicationInfos res = (CMSPublicationInfos) ((NuxeoResult) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(),
                new PublishInfosCommand(IWebIdService.FETCH_PATH_PREFIX + internalId))).getResult();
        


        return res.getDocumentPath();

    }


    /**
     * Gets the navigation CMS context.
     *
     * @return the navigation CMS context
     */
    private CMSServiceCtx getNavigationCMSContext() {
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(getPortalContext());
        if( isPreviewRepository())
            cmsContext.setDisplayLiveVersion("1");
        return cmsContext;
    }

    
    
    
    
    @Override
    public NavigationItem getNavigationItem(String internalId) throws CMSException {
        

        
        Document doc = getDocument(internalId);

        String spacePath = getPath(doc.getSpaceId().getInternalID());
        String docPath = getPath(internalId);


        
        CMSServiceCtx cmsContext = getNavigationCMSContext();


        // One level up
        CMSItem parent = null;

        String pathToCheck = docPath;
        do {
            if (pathToCheck.contains(spacePath)) {
                try {
                    parent = cmsServiceLocator.getCMSService().getPortalNavigationItem(cmsContext, spacePath, pathToCheck);
                } catch (org.osivia.portal.core.cms.CMSException e) {
                    throw new CMSException(e);
                }
                if (parent != null)
                    break;
            } else
                break;
            
            final CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
            pathToCheck = parentPath.toString();            
            
        } while (true);

//        if( internalId.equals("kFG8vy"))
//            System.out.println("**** GETNAV "+ docPath+ " " + parent.getWebId());
//        
//        
        
        NuxeoNavigationItem navItem;
        if( parent != null) {
            navItem = new NuxeoNavigationItem(this, new UniversalID(getRepositoryName(), parent.getWebId()), parent.getProperties().get("title"), doc.getSpaceId(), spacePath, parent.getPath());
            String pageTemplate = parent.getProperties().get("pageTemplate");
            if( pageTemplate!= null && pageTemplate.startsWith("/"))   {
                // Old pattern /page1/page2/page3
                String items[]  = pageTemplate.substring(1).split( "/");
                
                String internalID = "";
                for(int i=0; i<items.length;i++) {
                    if( internalID.length() > 0)
                        internalID+= "_";
                    internalID += items[i].toUpperCase();
                }
                navItem.setCustomizedTemplateId(new UniversalID(System.getProperty("osivia.cms.template.repository"), internalID));
            }
        }
        else 
            navItem = null;

        return navItem;

    }


    /**
     * Compute parent navigation item.
     *
     * @param spacePath the space path
     * @param docPath the doc path
     * @param spaceId the space id
     * @return the navigation item
     * @throws CMSException the CMS exception
     */
    public NavigationItem computeParentNavigationItem(String spacePath, String docPath, String spaceId) throws CMSException {
        
        CMSServiceCtx cmsContext = getNavigationCMSContext();

        String pathToCheck = docPath;
        CMSItem parent = null;
        do {
            final CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
            pathToCheck = parentPath.toString();

            if (pathToCheck.contains(spacePath)) {
                try {
                    parent = cmsServiceLocator.getCMSService().getPortalNavigationItem(cmsContext, spacePath, pathToCheck);
                } catch (org.osivia.portal.core.cms.CMSException e) {
                    throw new CMSException(e);
                }
                if (parent != null)
                    break;
            } else
                break;
        } while (true);

        return new NuxeoNavigationItem(this, new UniversalID(getRepositoryName(), parent.getWebId()), parent.getProperties().get("title"),
                 new UniversalID(getRepositoryName(), spaceId), spacePath, parent.getPath());
    }

    
    
    
    /**
     * Compute children navigation item.
     *
     * @param spacePath the space path
     * @param docPath the doc path
     * @param spaceId the space id
     * @return the list
     * @throws CMSException the CMS exception
     */
    public List<NavigationItem> computeChildrenNavigationItem(String spacePath, String docPath, String spaceId) throws CMSException {
        CMSServiceCtx cmsContext = getNavigationCMSContext();
        
        List<NavigationItem> navChildren = new ArrayList<NavigationItem>();
        try {
            List<CMSItem> cmsChildren = cmsServiceLocator.getCMSService().getPortalNavigationSubitems(cmsContext, spacePath, docPath);
            for (CMSItem child : cmsChildren) {
                navChildren.add(new NuxeoNavigationItem(this, new UniversalID(getRepositoryName(), child.getWebId()), child.getProperties().get("title"),
                         new UniversalID(getRepositoryName(), spaceId), spacePath,child.getPath()));
            }
        } catch (org.osivia.portal.core.cms.CMSException e) {
            throw new CMSException(e);
        }

        return navChildren;
    }

    @Override
    public void publish(String id) throws CMSException {

    }



    @Override
    public void addWindow(String id, String name, String portletName, String region, int position, String pageId, Map<String, String> properties) throws CMSException {
        // TODO Auto-generated method stub
        
    }



    @Override
    public void addDocument(String id, String type,  String name, String parentId) throws CMSException {
      getUserStorage().addDocument(id, type, name, batchMode);
      
      if(!batchMode)  {        
          RepositoryDocument doc = (RepositoryDocument) getDocument(id);
          UpdateInformations infos = new UpdateInformations(new UniversalID(getRepositoryName(), id), doc.getSpaceId(), UpdateScope.SCOPE_SPACE, false);
          getSharedRepository().notifyUpdate( getUserStorage(), infos);
      }  
        
    }

    @Override
    public void unpublish(String id) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean supportPageEdition() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Document> getChildren(String id) throws CMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setACL(String id, List<String> acls) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getACL(String id) throws CMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteDocument(String id) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renameDocument(String id, String title) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void moveDocument(String srcId, String beforedestId, boolean endOfList) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reloadDatas() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addDocument(String internalID, RepositoryDocument document) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateDocument(String internalID, RepositoryDocument document) throws CMSException {
       getUserStorage().updateDocument(internalID, document, batchMode);
       
       if(!batchMode)  {        
           UpdateInformations infos = new UpdateInformations(new UniversalID(getRepositoryName(), internalID), document.getSpaceId(), UpdateScope.SCOPE_SPACE, false);
           getSharedRepository().notifyUpdate( getUserStorage(), infos);
       }    
        
    }

    @Override
    public void setNewId(String internalID, String newId) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setProfiles(String id, List<Profile> profiles) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setStyles(String id, List<String> styles) throws CMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTemplateId(String id, UniversalID templateID) throws CMSException {
        RepositoryDocument doc = (RepositoryDocument) getDocument(id);
        if( templateID != null)
            doc.getProperties().put("ttc:pageTemplate", templateID.toString());
        else
            doc.getProperties().remove("ttc:pageTemplate");
        
        updateDocument(id, doc);
        
    }

}
