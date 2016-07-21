package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author dorian
 */
public class FormFilterExecutor {

    /** filtersByParentPathMap */
    private Map<String, List<FormFilterInstance>> filtersByParentPathMap;

    /** currentPath */
    private String currentPath;

    /** currentFilterName */
    private String currentFilterName;

    /**
     * @param filtersByParentPathMap
     * @param currentPath
     */
    public FormFilterExecutor(Map<String, List<FormFilterInstance>> filtersByParentPathMap, String currentPath, String currentFilterName) {
        this.filtersByParentPathMap = filtersByParentPathMap;
        this.currentPath = currentPath;
        this.currentFilterName = currentFilterName;
    }

    /**
     * call the execute method on each of the direct children of the currentPath
     * 
     * @param filterContext
     */
    public void executeChildren(FormFilterContext filterContext) {
        List<FormFilterInstance> filters = filtersByParentPathMap.get(currentPath);
        if (filters != null) {
            Collections.sort(filters);
            for (FormFilterInstance formFilterI : filters) {
                formFilterI.getFormFilter()
                        .execute(filterContext, new FormFilterExecutor(filtersByParentPathMap, formFilterI.getPath(), formFilterI.getName()));
            }
        }
    }

    /**
     * Getter for currentFilterName.
     * @return the currentFilterName
     */
    public String getCurrentFilterName() {
        return currentFilterName;
    }

}
