package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Map;

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
    String getLabelKey();

    /**
     * get the form filter description internationalization key
     * 
     * @return t
     */
    String getDescriptionKey();


    /**
     * get the parameters used by the filter
     * 
     * @return a map listing the parameters and their types
     */
    Map<String, FormFilterParameterType> getParameters();

    /**
     * return true if the filter calls the execution of children filter
     * 
     * @return
     */
    boolean hasChildren();

    /**
     * Execute form filter.
     * 
     * @param context form filter context
     * @param executor form filter executor
     */
    void execute(FormFilterContext context, FormFilterExecutor executor) throws FormFilterException;

}
