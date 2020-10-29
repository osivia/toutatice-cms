package fr.toutatice.portail.cms.nuxeo.api.avatar;

/**
 * Avatar module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface AvatarModule {

    /**
     * Get avatar URL.
     *
     * @param username username
     * @return URL
     */
    String getUrl(String username);

}
