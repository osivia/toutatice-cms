package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetUserProfileCommand;

/**
 * @author Jean-Sébastien
 *
 */
public class AvatarUtils {

	/**
	 * Gets the user profile. One request for all users for one timestamp
	 *
	 * @param portletCtx the portlet ctx
	 * @param userId the user id
	 * @param timestamp the timestamp
	 * @return the user profile
	 */
	public static Document getUserProfile(PortletContext portletCtx, String userId, String timestamp) {
		
		NuxeoController ctx = new NuxeoController(portletCtx);
		ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
		ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

		Document fetchedUserProfile;

		GetUserProfileCommand userProfileCommand = new GetUserProfileCommand(userId);
		userProfileCommand.setTimestamp(timestamp);
		Document userProfile = (Document) ctx.executeNuxeoCommand(userProfileCommand);

		if (userProfile != null) {
			DocumentFetchLiveCommand fetchLiveCommand = new DocumentFetchLiveCommand(userProfile.getPath(), "Read");
			fetchLiveCommand.setTimestamp(timestamp);
			fetchedUserProfile = (Document) ctx.executeNuxeoCommand(fetchLiveCommand);
		} else
			fetchedUserProfile = null;

		return fetchedUserProfile;
	}

}
