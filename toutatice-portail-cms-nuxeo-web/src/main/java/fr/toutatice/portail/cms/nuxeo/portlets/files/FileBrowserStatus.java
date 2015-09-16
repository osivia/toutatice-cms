package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.HashMap;
import java.util.Map;

import org.osivia.portal.api.portlet.PortletStatus;

import fr.toutatice.portail.cms.nuxeo.api.FileBrowserView;

/**
 * File browser status.
 *
 * @author Cédric Krommenhoek
 * @see PortletStatus
 */
public class FileBrowserStatus implements PortletStatus {

    /** Task identifier, may be null. */
    private final String taskId;

    /** Views. */
    private final Map<String, FileBrowserView> views;


    /**
     * Constructor.
     * 
     * @param taskId task identifier, may be null
     */
    public FileBrowserStatus(String taskId) {
        super();
        this.taskId = taskId;
        this.views = new HashMap<String, FileBrowserView>();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PortletStatus clone() {
        FileBrowserStatus clone = new FileBrowserStatus(this.taskId);
        clone.views.putAll(this.views);
        return clone;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileBrowserStatus [taskId=");
        builder.append(this.taskId);
        builder.append(", views=");
        builder.append(this.views);
        builder.append("]");
        return builder.toString();
    }


    /**
     * Getter for taskId.
     *
     * @return the taskId
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * Getter for views.
     *
     * @return the views
     */
    public Map<String, FileBrowserView> getViews() {
        return this.views;
    }

}
