package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * Default player (use view document portlet).
 * 
 */
public class DefaultPlayer implements IPlayer {

    public CMSHandlerProperties play(CMSServiceCtx ctx, Document doc) throws Exception {


        Map<String, String> windowProperties = new HashMap<String, String>();
        return play(ctx, doc, windowProperties);

    }

    public CMSHandlerProperties play(CMSServiceCtx ctx, Document doc, Map<String, String> windowProperties) throws Exception {
        /* windowProperties.put("osivia.cms.scope", ctx.getScope()); */
        windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
        windowProperties.put("osivia.cms.uri", doc.getPath());
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");


        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewDocumentPortletInstance");

        return linkProps;
    }

}
