package fr.toutatice.portail.cms.nuxeo.api.services.dao;

import java.util.Date;

import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;

import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


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


    /** Person service. */
    private final PersonService personService;


    /**
     * Private constructor.
     */
    private CommentDAO() {
        super();

        this.personService = DirServiceFactory.getService(PersonService.class);
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
        Person person = this.personService.getPerson(author);
        dto.setPerson(person);

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
