package fr.toutatice.portail.cms.nuxeo.api.services;

/**
 * Task directives enumeration.
 * 
 * @author Cédric Krommenhoek
 */
public enum TaskDirective {

    /** Serial document review. */
    SERIAL_DOCUMENT_REVIEW("wf.serialDocumentReview.AcceptReject"),
    /** Parallel document review. */
    PARALLEL_DOCUMENT_REVIEW("wf.parallelDocumentReview.give_opinion.directive");


    /** Directive identifier. */
    private final String id;


    /**
     * Constructor.
     * 
     * @param id task directive identifier
     */
    private TaskDirective(String id) {
        this.id = id;
    }


    /**
     * Get task directive from identifier.
     * 
     * @param id task directive identifier
     * @return task directive
     */
    public static TaskDirective fromId(String id) {
        TaskDirective result = null;

        for (TaskDirective value : TaskDirective.values()) {
            if (value.id.equals(id)) {
                result = value;
                break;
            }
        }

        return result;
    }


    /**
     * Getter for id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

}
