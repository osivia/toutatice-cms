/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindowHelper;
import fr.toutatice.portail.cms.nuxeo.api.fragment.FragmentModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.SummaryEditableWindow;


/**
 * Summary fragment module.
 *
 * @author David Chevrier
 * @see FragmentModule
 */
public class SummaryFragmentModule extends FragmentModule {

    /** Generic fragment properties */
    public enum GenericProperties {
        uri, regionId, order, title, hideTitle
    }

    /** Summary fragment identifier. */
    public static final String ID = "fgts_summary";

    /** JSP name. */
    private static final String VIEW_JSP_NAME = "summary";


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     */
    public SummaryFragmentModule(PortletContext portletContext) {
        super(portletContext);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException {
        Map<String, String> resultFragments = new LinkedHashMap<String, String>();
        // Request
        RenderRequest request = (RenderRequest) portalControllerContext.getRequest();
        // Response
        RenderResponse response = (RenderResponse) portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Nuxeo path
        String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);

        if (StringUtils.isNotEmpty(nuxeoPath)) {
            nuxeoPath = nuxeoController.getComputedPath(nuxeoPath);

            // Fetch document
            Document document = nuxeoController.fetchDocument(nuxeoPath);
            Object regions = document.getProperties().get(SummaryEditableWindow.SUMMARY_SCHEMA);

            if (regions instanceof PropertyList) {
                PropertyList regionsList = (PropertyList) regions;
                if ((regionsList != null) && (regionsList.size() > 0)) {
                    for (int index = 0; index < regionsList.size(); index++) {
                        PropertyMap regionMap = ((PropertyList) regions).getMap(index);
                        String region = regionMap.getString(GenericProperties.regionId.name());
                        PropertyList fragments = document.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);
                        if ((fragments != null) && !fragments.isEmpty()) {
                            resultFragments.putAll(this.filterNOrderByRegion(fragments, region));
                        }
                    }
                }
            }
            if (!resultFragments.isEmpty()) {
                request.setAttribute("fragments", resultFragments);
            }
        }
    }

    private Map<String, String> filterNOrderByRegion(PropertyList fragments, String region) {
        Map<String, String> resultFgts = new LinkedHashMap<String, String>();

        PropertyList filteredFragments = this.filterFragments(fragments, region);
        PropertyList orderedFragments = this.orderFragments(filteredFragments);

        for (int index = 0; index < orderedFragments.size(); index++) {
            PropertyMap fragment = orderedFragments.getMap(index);
            if (!fragment.getBoolean(GenericProperties.hideTitle.name())) {
                String uri = fragment.getString(GenericProperties.uri.name());
                String title = fragment.getString(GenericProperties.title.name());
                resultFgts.put("#" + uri, title);
            }
        }

        return resultFgts;
    }


    private PropertyList filterFragments(PropertyList fragments, String region) {

        List<Object> filteredFgts = new ArrayList<Object>(fragments.size());

        for (Object fragmentObj : fragments.list()) {
            PropertyMap fragment = (PropertyMap) fragmentObj;
            if (region.equals(fragment.getString(GenericProperties.regionId.name()))) {
                filteredFgts.add(fragmentObj);
            }
        }

        return new PropertyList(filteredFgts);
    }


    private PropertyList orderFragments(PropertyList fragments) {

        List<Object> fgtsAsList = new ArrayList<Object>(fragments.list());
        Collections.sort(fgtsAsList, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                PropertyMap pm1 = (PropertyMap) o1;
                Long order1 = pm1.getLong(GenericProperties.order.name());
                PropertyMap pm2 = (PropertyMap) o2;
                Long order2 = pm2.getLong(GenericProperties.order.name());
                return order1.compareTo(order2);
            }

        });

        return new PropertyList(fgtsAsList);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewJSPName() {
        return VIEW_JSP_NAME;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return null;
    }


}
