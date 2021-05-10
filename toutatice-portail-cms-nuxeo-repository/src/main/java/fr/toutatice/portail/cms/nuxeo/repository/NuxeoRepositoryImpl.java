package fr.toutatice.portail.cms.nuxeo.repository;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletContext;

import org.apache.commons.lang3.StringUtils;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.UniversalID;
import org.osivia.portal.api.cms.exception.CMSException;
import org.osivia.portal.api.cms.model.Document;
import org.osivia.portal.api.cms.model.NavigationItem;
import org.osivia.portal.api.cms.repository.BaseUserRepository;
import org.osivia.portal.api.cms.repository.cache.SharedRepositoryKey;
import org.osivia.portal.api.cms.repository.model.user.NavigationItemImpl;
import org.osivia.portal.api.cms.service.GetChildrenRequest;
import org.osivia.portal.api.cms.service.Request;
import org.osivia.portal.api.cms.service.Result;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.cms.spi.NuxeoRepository;
import org.osivia.portal.core.cms.spi.NuxeoRequest;
import org.osivia.portal.core.cms.spi.NuxeoResult;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

public class NuxeoRepositoryImpl extends BaseUserRepository implements NuxeoRepository {


    private INuxeoService nuxeoService;

    /** Remote proxy webid marker. */
    public static final String RPXY_WID_MARKER = "_c_";

    /**
     * CMS service locator.
     */
    private final ICMSServiceLocator cmsServiceLocator;


    private INuxeoService getNuxeoService() {

        if (this.nuxeoService == null) {

            this.nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");

        }
        return this.nuxeoService;
    }

    public NuxeoRepositoryImpl(SharedRepositoryKey repositoryKey, BaseUserRepository publishRepository, String userName) {
        super(repositoryKey, publishRepository, userName, new NuxeoUserStorage());

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
        return false;
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

    @Override
    public String getInternalId(String path) throws CMSException {

        // Get result by cache pattern
        // TODO : update if the document is modified


        CMSPublicationInfos res = (CMSPublicationInfos) ((NuxeoResult) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(),
                new PublishInfosCommand(path))).getResult();

        org.nuxeo.ecm.automation.client.model.Document nxDocument;

        if (!res.isPublished())

            nxDocument = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage())
                    .executeCommand(createCommandContext(), new DocumentFetchLiveCommand(res.getDocumentPath(), "Read")).getResult();
        else
            nxDocument = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage())
                    .executeCommand(createCommandContext(), new DocumentFetchPublishedCommand(res.getDocumentPath())).getResult();


        String internalId = (String) nxDocument.getString("ttc:webid");


        if (nxDocument.getFacets().list().contains("isRemoteProxy")) {
            // Get parent
            org.nuxeo.ecm.automation.client.model.Document parent = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage())
                    .executeCommand(createCommandContext(), new GetParentCommand(nxDocument)).getResult();


            internalId = internalId + RPXY_WID_MARKER + (String) parent.getString("ttc:webid");
        }


        return internalId;
    }

    @Override
    public String getPath(String internalId) throws CMSException {

        // Get result by cache pattern
        // TODO : update if the document is modified


        CMSPublicationInfos res = (CMSPublicationInfos) ((NuxeoResult) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(),
                new PublishInfosCommand(IWebIdService.FETCH_PATH_PREFIX + internalId))).getResult();

        List<Integer> errors = res.getErrorCodes();
        if (errors != null) {
            if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_NOT_FOUND)) {
                if (internalId.contains(RPXY_WID_MARKER)) {
                    // TODO : dans le cas d'un remote proxy coté Nuxeo le fetch combiné [WEB_ID_DOC]_c_[WEB_ID_SECTION]ne donne rien
                    // coté nuxeo le : le ecm:mixinType = 'isRemoteProxy' du CMSPublicationInfos ne renvoie rien
                    // Apparemment, il est uniquement mis à jour sur ToutaticeCoreProxyWithWorkflowFactory (publication par workflow)


                    String[] webIds = StringUtils.splitByWholeSeparator(internalId, RPXY_WID_MARKER);
                    // Remote proxy webid is same as live
                    String liveWId = webIds[0];
                    // Webid of section where live is published (section is parent of remote proxy)
                    String sectionWId = webIds[1];

                    try {
                        // Get proxy
                        org.nuxeo.ecm.automation.client.model.Document proxy = (org.nuxeo.ecm.automation.client.model.Document) ((NuxeoUserStorage) super.getUserStorage())
                                .executeCommand(createCommandContext(), new FetchByWebIdCommand(liveWId)).getResult();


                        // Add facet
                        ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(), new AddRemoteProxyFacetCommand(proxy.getPath()))
                                .getResult();

                        // Refetch
                        res = (CMSPublicationInfos) ((NuxeoResult) ((NuxeoUserStorage) super.getUserStorage()).executeCommand(createCommandContext(false),
                                new PublishInfosCommand(IWebIdService.FETCH_PATH_PREFIX + internalId))).getResult();
                    } catch (Exception e) {
                        // DO NOTHING
                    }

                }
            }
        }


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
        return cmsContext;
    }

    
    
    
    
    @Override
    public NavigationItem getNavigationItem(String internalId) throws CMSException {
        
        // Get result by cache pattern
        // TODO : update if the document is modified

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

        NavigationItem navItem;
        if( parent != null)
            navItem = new NuxeoNavigationItem(this, new UniversalID(getRepositoryName(), parent.getWebId()), parent.getProperties().get("dc:title"), doc.getSpaceId(), spacePath, parent.getPath());
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

        return new NuxeoNavigationItem(this, new UniversalID(getRepositoryName(), parent.getWebId()), parent.getProperties().get("dc:title"),
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
                navChildren.add(new NuxeoNavigationItem(this, new UniversalID(getRepositoryName(), child.getWebId()), child.getProperties().get("dc:title"),
                         new UniversalID(getRepositoryName(), spaceId), spacePath,child.getPath()));
            }
        } catch (org.osivia.portal.core.cms.CMSException e) {
            throw new CMSException(e);
        }

        return navChildren;
    }

}
