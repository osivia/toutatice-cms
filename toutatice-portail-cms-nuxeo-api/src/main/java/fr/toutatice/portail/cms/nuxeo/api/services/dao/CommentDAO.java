package fr.toutatice.portail.cms.nuxeo.api.services.dao;

import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;


/**
 * Nuxeo document comment data access object.
 *
 * @author Cédric Krommenhoek
 * @see IDAO
 * @see JSONObject
 * @see CommentDTO
 */
public final class CommentDAO implements IDAO<JSONObject, CommentDTO> {

    /** Singleton instance. */
    private static CommentDAO instance;


    /** Directory service locator. */
    private final IDirectoryServiceLocator directoryServiceLocator;


    /**
     * Private constructor.
     */
    private CommentDAO() {
        super();

        // Directory service locator
        this.directoryServiceLocator = Locator.findMBean(IDirectoryServiceLocator.class, IDirectoryServiceLocator.MBEAN_NAME);
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static CommentDAO getInstance() {
        if (instance == null) {
            instance = new CommentDAO();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CommentDTO toDTO(JSONObject jsonObject) {
        CommentDTO dto = new CommentDTO();

        // Identifier
        dto.setId(jsonObject.getString("id"));

        // Path
        dto.setPath(jsonObject.getString("path"));

        // Author
        String author = jsonObject.getString("author");
        dto.setAuthor(author);

        // LDAP person
        dto.setPerson(this.directoryServiceLocator.getDirectoryService().getPerson(author));

        // Creation date
        JSONObject jsonDate = jsonObject.getJSONObject("creationDate");
        dto.setCreationDate(new Date(jsonDate.getLong("timeInMillis")));

        // Content
        String content = jsonObject.getString("content");
        dto.setContent(content);

        // Deletable indicator
        dto.setDeletable(jsonObject.getBoolean("canDelete"));

        // Children
        JSONArray jsonChildren = jsonObject.getJSONArray("children");
        for (int i = 0; i < jsonChildren.size(); i++) {
            JSONObject jsonChild = jsonChildren.getJSONObject(i);
            CommentDTO childDTO = this.toDTO(jsonChild);
            dto.getChildren().add(childDTO);
        }

        return dto;
    }

}
