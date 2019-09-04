package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author dorian
 */
public class FormFilterExecutor {

	
	private final static Log procLogger = LogFactory.getLog("procedures");
	
    /** filtersByParentPathMap */
    private Map<String, List<FormFilterInstance>> filtersByParentPathMap;

    /** currentPath */
    private String currentPath;

    /** currentFilterInstanceId */
    private String currentFilterInstanceId;

    /**
     * @param filtersByParentPathMap
     * @param currentPath
     */
    public FormFilterExecutor(Map<String, List<FormFilterInstance>> filtersByParentPathMap, String currentPath, String currentFilterInstanceId) {
        this.filtersByParentPathMap = filtersByParentPathMap;
        this.currentPath = currentPath;
        this.currentFilterInstanceId = currentFilterInstanceId;
    }

    /**
     * call the execute method on each of the direct children of the currentPath
     * 
     * @param filterContext
     */
    public void executeChildren(FormFilterContext filterContext) throws FormFilterException {
        List<FormFilterInstance> filters = filtersByParentPathMap.get(currentPath);
        if (filters != null) {
            Collections.sort(filters);
            for (FormFilterInstance formFilterI : filters) {
            	
            	procLogger.info("* call filter "+formFilterI.getId()+" ("+formFilterI.getName()+") for "+filterContext.getProcedureInstanceUuid());
            	
                formFilterI.getFormFilter().execute(filterContext, new FormFilterExecutor(filtersByParentPathMap, formFilterI.getPath(), formFilterI.getId()));
            }
        }
    }

    /**
     * Getter for currentFilterInstanceId.
     * 
     * @return the currentFilterInstanceId
     */
    public String getCurrentFilterInstanceId() {
        return currentFilterInstanceId;
    }

}
