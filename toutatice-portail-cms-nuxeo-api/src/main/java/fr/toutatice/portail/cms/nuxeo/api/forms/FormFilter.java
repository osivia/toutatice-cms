package fr.toutatice.portail.cms.nuxeo.api.forms;

/**
 * Form filter.
 */
public interface FormFilter {

    /**
     * Get form filter identifier.
     * 
     * @return identifier
     */
    String getId();


    /**
     * Get form filter label internationalization key.
     * 
     * @return internationalization key
     */
    String getKey();


    /**
     * Execute form filter.
     * 
     * @param ctx form filter context
     * @param executor form filter executor
     */
    void execute(FormFilterContext ctx, FormFilterExecutor executor);

}
