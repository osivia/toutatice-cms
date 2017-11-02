package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.internationalization.Bundle;


/**
 * @author dorian
 */
public class FormFilterExecutor {

    /** filtersByParentPathMap */
    private Map<String, List<FormFilterInstance>> filtersByParentPathMap;

    /** currentPath */
    private String currentPath;

    /** currentFilterInstanceId */
    private String currentFilterInstanceId;

    /** bundle */
    private Bundle bundle;

    /**
     * @param filtersByParentPathMap
     * @param currentPath
     */
    public FormFilterExecutor(Map<String, List<FormFilterInstance>> filtersByParentPathMap, String currentPath, String currentFilterInstanceId, Bundle bundle) {
        this.filtersByParentPathMap = filtersByParentPathMap;
        this.currentPath = currentPath;
        this.currentFilterInstanceId = currentFilterInstanceId;
        this.bundle = bundle;
    }

    /**
     * call the execute method on each of the direct children of the currentPath
     * 
     * @param filterContext
     */
    public void executeChildren(FormFilterContext filterContext) throws FormFilterException, PortalException {
        List<FormFilterInstance> filters = filtersByParentPathMap.get(currentPath);
        if (filters != null) {
            Collections.sort(filters);
            for (FormFilterInstance formFilterI : filters) {
                FormFilter formFilter = formFilterI.getFormFilter();
                try {
                    formFilter.execute(filterContext,
                            new FormFilterExecutor(filtersByParentPathMap, formFilterI.getPath(), formFilterI.getId(), bundle));
                } catch (FormFilterException | PortalException e) {
                    throw e;
                } catch (Exception e) {
                    String msg = bundle.getString(formFilter.getLabelKey(), formFilter.getClass().getClassLoader());
                    throw new PortalException(msg, e);
                }
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
