package org.osivia.portal.core.cms.spi;

import org.osivia.portal.api.cms.service.Result;

/**
 * The Class NuxeoResult.
 */
public class NuxeoResult implements Result {
    
    /** The result. */
    private final Object result;



    /**
     * Instantiates a new nuxeo result.
     *
     * @param result the result
     */
    public NuxeoResult(Object result) {
        super();
        this.result = result;
    }
    
    
    /**
     * Gets the result.
     *
     * @return the result
     */
    public Object getResult() {
        return result;
    }

}
