package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.internationalization.Bundle;


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
            	
            	procLogger.info("* call filter "+formFilterI.getId()+" ("+formFilterI.getName()+") for "+filterContext.getProcedureInstanceUuid());
            	
                FormFilter formFilter = formFilterI.getFormFilter();
                if(formFilter!=null){
                    try {
                        formFilter.execute(filterContext,
                                new FormFilterExecutor(filtersByParentPathMap, formFilterI.getPath(), formFilterI.getId(), bundle));
                    } catch (FormFilterException | PortalException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new PortalException(formFilterI.getName(), e);
                    }
                }else{
                    String msg = bundle.getString("FORMS_FILTER_NOT_FOUND", formFilterI.getName());
                    throw new PortalException(msg);
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
