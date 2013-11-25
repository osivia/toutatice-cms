package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.page.PageProperties;

/**
 * Assistant pour gestion des objets de conf.
 * 
 */
public class WebConfigurationHelper {

    /** id 1 des objets de conf. */
    public static final String CODE = "wconf:code";
    /** id 2 des objets de conf. */
    public static final String CODECOMP = "wconf:code2";
    /** paramètres. */
    public static final String OPTIONS = "wconf:options";
    /** clé du paramnètre. */
    public static final String OPTION_KEY = "propertyName";
    /** valeur du paramètre. */
    public static final String OPTION_VALUE = "propertyValue";

    /**
     * Retourne le domain Nuxeo à partir de la page courante.
     * 
     * @param ctx contexte cms
     * @return domaine
     */
    public static String getDomainPath(CMSServiceCtx ctx) {
        String domainPath = null;
        String portalName = PageProperties.getProperties().getPagePropertiesMap().get("portalName");

        // Dans certaines cas, le nom du portail n'est pas connu
        // cas des stacks server (par exemple, le pre-cahrgement des pages)
        if (portalName != null) {
            PortalObjectContainer portalObjectContainer = (PortalObjectContainer) ctx.getServerInvocation().getAttribute(Scope.REQUEST_SCOPE,
                    "osivia.portalObjectContainer");

            Portal portal = (Portal) portalObjectContainer.getObject(PortalObjectId.parse("", "/" + portalName, PortalObjectPath.CANONICAL_FORMAT));

            if (InternalConstants.PORTAL_TYPE_SPACE.equals(portal.getDeclaredProperty("osivia.portal.portalType"))) {
                domainPath = portal.getDefaultPage().getDeclaredProperty("osivia.cms.basePath");
                if (domainPath != null) {
                    domainPath = domainPath.split("/")[1];
                    domainPath = "/".concat(domainPath).concat("/");
                }
            }

        }

        return domainPath;
    }
}
