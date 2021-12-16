package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.concurrent.TimeUnit;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.FileContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetUserProfileCommand;

/**
 * @author Jean-Sébastien
 *
 */
public class AvatarUtils {


    /**
     * Log.
     */
    private static final Log log = LogFactory.getLog(AvatarUtils.class);


    /** The portlet context. */
    protected static PortletContext portletContext;

    /* External modification timeout (cluster) */
    private static Long timeout = TimeUnit.MINUTES.toMillis(10);

    /**
     * Gets the user profile. O
     *
     * @param portletCtx the portlet ctx
     * @param userId the user id
     * @param timestamp the timestamp
     * @return the user profile
     */
    public static Document getUserProfile(String userId, boolean cache) {

        NuxeoController ctx = new NuxeoController(portletContext);
        ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
        ctx.setCacheTimeOut(timeout);
        if (cache == false) {
            ctx.setForceReload(true);
        } 

        Document fetchedUserProfile;

        try {
            GetUserProfileCommand userProfileCommand = new GetUserProfileCommand(userId);

            Document userProfile = (Document) ctx.executeNuxeoCommand(userProfileCommand);

            if (userProfile != null) {
                DocumentFetchLiveCommand fetchLiveCommand = new DocumentFetchLiveCommand(userProfile.getPath(), "Read");
                fetchedUserProfile = (Document) ctx.executeNuxeoCommand(fetchLiveCommand);
            } else
                fetchedUserProfile = null;
        } catch (Exception e) {
            log.error(e);
            fetchedUserProfile = null;
        }

        return fetchedUserProfile;
    }


    /**
     * Gets the avatar content.
     *
     * @param userProfile the user profile
     * @param cache the cache
     * @return the avatar content
     */
    public static CMSBinaryContent getAvatarContent(Document userProfile, boolean cache) {

        NuxeoController ctx = new NuxeoController(portletContext);
        ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
        ctx.setCacheTimeOut(timeout);
        if (cache == false) {
            ctx.setForceReload(true);
        } 

        CMSBinaryContent content;
        try {
            FileContentCommand command = new FileContentCommand(userProfile, "userprofile:avatar");

            content = (CMSBinaryContent) ctx.executeNuxeoCommand(command);
            return content;
        } catch (Exception e) {
            log.error(e);
            content = null;
        }
        return content;
    }

    /**
     * Fill avatar with refreshed informations
     *
     * @param username the username
     * @param avatar the avatar
     */

    public static void fillAvatar(String username, AvatarInfo avatar) {
        Document fetchedUserProfile = AvatarUtils.getUserProfile(username, false);

        if (fetchedUserProfile != null) {
            avatar.setDigest(getAvatarDigest(fetchedUserProfile));
        }

        if (fetchedUserProfile == null || avatar.getDigest() == null) {
            avatar.setGenericResource(true);
        } else {
            if (AvatarUtils.getAvatarContent(fetchedUserProfile, false) != null) {
                avatar.setGenericResource(false);
            } else {
                avatar.setGenericResource(true);
            }
        }
    }


    /**
     * Gets the avatar digest.
     *
     * @param fetchedUserProfile the fetched user profile
     * @return the avatar digest
     */
    public static String getAvatarDigest(Document fetchedUserProfile) {
        String digest;
        if (fetchedUserProfile != null) {
            PropertyMap avatarMap = FileContentCommand.getFileMap(fetchedUserProfile, "userprofile:avatar");
            if (avatarMap != null) {
                digest = avatarMap.getString("digest");
            } else {
                digest = null;
            }
        } else {
            digest = null;
        }
        return digest;
    }


}
