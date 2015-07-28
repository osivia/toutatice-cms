package fr.toutatice.portail.cms.nuxeo.api.domain;

import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;


public interface IPlayerModule {
    
    CMSHandlerProperties getCMSPlayer(CMSServiceCtx ctx, ICMSService cmsService) throws CMSException;

}
