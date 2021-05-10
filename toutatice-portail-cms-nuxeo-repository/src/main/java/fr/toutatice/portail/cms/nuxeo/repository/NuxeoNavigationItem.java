package fr.toutatice.portail.cms.nuxeo.repository;

import java.util.List;

import org.osivia.portal.api.cms.UniversalID;
import org.osivia.portal.api.cms.exception.CMSException;
import org.osivia.portal.api.cms.model.NavigationItem;

public class NuxeoNavigationItem implements NavigationItem {

    private NuxeoRepositoryImpl repository;
    private UniversalID documentId;
    private String title;
    private boolean root;
    private UniversalID spaceId;
    private String spacePath;
    private String docPath;


    public NuxeoNavigationItem(NuxeoRepositoryImpl repository, UniversalID documentId, String title, UniversalID spaceId, String spacePath, String docPath) {
        super();
        this.repository = repository;
        this.documentId = documentId;
        this.title = title;
        this.root = spacePath.equals(docPath);
        this.spaceId = spaceId;
        this.spacePath = spacePath;
        this.docPath = docPath;
    }

    @Override
    public boolean isRoot() {
        return root;
    }

    @Override
    public UniversalID getDocumentId() {
        return documentId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public NavigationItem getParent() throws CMSException {
        return repository.computeParentNavigationItem(spacePath, docPath, spaceId.getInternalID());

    }

    @Override
    public List<NavigationItem> getChildren() throws CMSException {
        return repository.computeChildrenNavigationItem(spacePath, docPath, spaceId.getInternalID());
    }

    @Override
    public UniversalID getSpaceId() {
        return spaceId;
    }


}
