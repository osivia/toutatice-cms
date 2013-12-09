package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;


/**
 * Portlet de selection de liste par mot clé.
 *
 * @see CMSPortlet
 */
public class KeywordsSelectorPortlet extends CMSPortlet {

    /** Logger. */
    private static final Log logger = LogFactory.getLog(KeywordsSelectorPortlet.class);
    /** Delete prefix. */
    public static final String DELETE_PREFIX = "delete_";


    /**
     * Default constructor.
     */
    public KeywordsSelectorPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {
        logger.debug("processAction ");

        PortalWindow window = WindowFactory.getWindow(req);

        if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("modifierPrefs") != null)) {

            if (req.getParameter("selectorId").length() > 0) {
                window.setProperty("osivia.selectorId", req.getParameter("selectorId"));
            } else if (window.getProperty("osivia.selectorId") != null) {
                window.setProperty("osivia.selectorId", null);
            }


            if (req.getParameter("libelle").length() > 0) {
                window.setProperty("osivia.libelle", req.getParameter("libelle"));
            } else if (window.getProperty("osivia.libelle") != null) {
                window.setProperty("osivia.libelle", null);
            }

            if ("1".equals(req.getParameter("keywordMonoValued"))) {
                window.setProperty("osivia.keywordMonoValued", "1");
            } else if (window.getProperty("osivia.keywordMonoValued") != null) {
                window.setProperty("osivia.keywordMonoValued", null);
            }

            /* Initialisation des mots-clés suite à configuration. */
            Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
            if (selectors != null) {
                List<String> keywords = selectors.get(req.getParameter("selectorId"));
                if ((keywords != null) && (keywords.size() > 0)) {
                    keywords.clear();
                    res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
                }
            }

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("annuler") != null)) {

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        // Pour supporter le mode Ajax, il faut également test le add sans l'extension '.x'
        boolean isAddAction = (req.getParameter("add.x") != null) || (req.getParameter("add") != null);
        boolean isMonoValuedAddAction = (req.getParameter("monoAdd.x") != null) || (req.getParameter("monoAdd") != null);

        if ("view".equals(req.getPortletMode().toString()) && (isAddAction || isMonoValuedAddAction)) {

            // Set public parameter
            String selectorId = window.getProperty("osivia.selectorId");
            if (selectorId != null) {

                String keyword = req.getParameter("keyword");

                Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));

                List<String> keywords = selectors.get(selectorId);
                if (keywords == null) {
                    keywords = new ArrayList<String>();
                    selectors.put(selectorId, keywords);
                }

                if ("1".equals(window.getProperty("osivia.keywordMonoValued"))) {
                    /*
                     * On ne conserve qu'une valeur dans le cas d'un
                     * sélecteur mono-valué.
                     */
                    keywords.clear();
                }

                if ((keyword != null) && (keyword.length() > 0)) {
                    keywords.add(keyword);
                }


                res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

                // Réinitialisation des fenetres en mode NORMAL
                req.setAttribute("osivia.unsetMaxMode", "true");

            }

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        // Delete
        if ("view".equals(req.getPortletMode().toString()) && "delete".equals(req.getParameter("action"))) {
            int occ = new Integer(req.getParameter("occ")) - 1;

            Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
            String selectorId = window.getProperty("osivia.selectorId");

            List<String> keywords = selectors.get(selectorId);
            if ((keywords != null) && (keywords.size() > occ)) {

                keywords.remove(occ);
                res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

                // Réinitialisation des fenetres en mode NORMAL
                req.setAttribute("osivia.unsetMaxMode", "true");
            }
        }
    }


    /**
     * Admin view display.
     *
     * @param req request
     * @param res response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {
        res.setContentType("text/html");
        PortletRequestDispatcher rd = null;

        PortalWindow window = WindowFactory.getWindow(req);

        String selectorId = window.getProperty("osivia.selectorId");
        if (selectorId == null) {
            selectorId = "";
        }
        req.setAttribute("selectorId", selectorId);

        String libelle = window.getProperty("osivia.libelle");
        if (libelle == null) {
            libelle = "";
        }
        req.setAttribute("libelle", libelle);

        String keywordMonoValued = window.getProperty("osivia.keywordMonoValued");
        if (keywordMonoValued == null) {
            keywordMonoValued = "0";
        }
        req.setAttribute("keywordMonoValued", keywordMonoValued);

        rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/admin.jsp");
        rd.include(req, res);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
        logger.debug("doView");

        try {
            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);

            String selectorId = window.getProperty("osivia.selectorId");

            String libelle = window.getProperty("osivia.libelle");
            request.setAttribute("libelle", libelle);

            String keywordMonoValued = window.getProperty("osivia.keywordMonoValued");
            request.setAttribute("keywordMonoValued", keywordMonoValued);

            String keyword = request.getParameter("keyword");

            if (selectorId != null) {
                // Get public parameter

                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

                List<String> selector = selectors.get(selectorId);
                if (selector != null) {
                    String[] keywords = new String[selector.size()];
                    request.setAttribute("keywords", selector.toArray(keywords));
                } else {
                    request.setAttribute("keywords", ArrayUtils.EMPTY_STRING_ARRAY);
                }

                request.setAttribute("keyword", keyword);


                this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/view.jsp").include(request, response);

            } else {
                response.getWriter().print("<h2>Identifiant non défini</h2>");
                response.getWriter().close();
            }

        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        }

        catch (Exception e) {
            if (!(e instanceof PortletException)) {
                throw new PortletException(e);
            }
        }

        logger.debug("doView end");
    }

}
