/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.api;

import org.osivia.portal.core.constants.InternalConstants;


public class NuxeoQueryFilterContext {

    /** The Constant STATE_PUBLISHED. */
    public static final int STATE_DEFAULT = 0;

    /** Default context instance. */
    public static final NuxeoQueryFilterContext CONTEXT_DEFAULT = new NuxeoQueryFilterContext(STATE_DEFAULT);

    /** The Constant STATE_LIVE. */
    public static final int STATE_LIVE = 1;
    
    /** The constant STATE_LIVE_N_PUBLISHED. */
    public static final int STATE_LIVE_N_PUBLISHED = 2;

    /** Live context instance. */
    public static final NuxeoQueryFilterContext CONTEXT_LIVE = new NuxeoQueryFilterContext(STATE_LIVE);
    
    /** Live and published context instance. */
    public static final NuxeoQueryFilterContext CONTEXT_LIVE_N_PUBLISHED = new NuxeoQueryFilterContext(STATE_LIVE_N_PUBLISHED);


    
    /** Ignore navigation Element. */
    boolean ignoreNavigation = true;
    
  
    
    /**
     * Getter for ignoreNavigation.
     * @return the ignoreNavigation
     */
    public boolean isIgnoreNavigation() {
        return ignoreNavigation;
    }

    
    /**
     * Setter for ignoreNavigation.
     * @param ignoreNavigation the ignoreNavigation to set
     */
    public void setIgnoreNavigation(boolean ignoreNavigation) {
        this.ignoreNavigation = ignoreNavigation;
    }

    public NuxeoQueryFilterContext() {
        super();
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the state.
     * 
     * @param state the new state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Gets the policy.
     * 
     * @return the policy
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * Sets the policy.
     * 
     * @param policy the new policy
     */
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    

    
    /** The state. */
    private int state = STATE_DEFAULT;

    /** The policy. */
    private String policy = null;


    public NuxeoQueryFilterContext(int state) {
        super();
        this.state = state;
        this.policy = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER;
    }

    public NuxeoQueryFilterContext(int state, String policy) {
        super();
        this.state = state;
        this.policy = policy;
    }
    
    public NuxeoQueryFilterContext(int state, String policy, boolean ignoreNavigation) {
        super();
        this.state = state;
        this.policy = policy;
        this.ignoreNavigation = ignoreNavigation;
    }


}
