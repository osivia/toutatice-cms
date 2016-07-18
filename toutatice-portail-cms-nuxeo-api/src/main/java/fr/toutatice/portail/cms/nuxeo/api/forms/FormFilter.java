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
     * @param context form filter context
     */
    void execute(FormFilterContext context);

}
