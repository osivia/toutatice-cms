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
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSServiceCtx;



import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;


// TODO: Auto-generated Javadoc
/**
 * The Class NuxeoQueryFilter.
 * 
 * Adds filters to the original nuxeo request
 * 
 * @author Jean-Sébastien Steux
 */

public class NuxeoQueryFilter {
    

	
	/**
	 * Gets the CMS ctx.
	 *
	 * @return the CMS ctx
	 */
	public static CMSServiceCtx getCMSCtx()	{

		CMSServiceCtx cmsCtx = new  CMSServiceCtx();
		
	
		return cmsCtx;
	}
	
	
    /**
     * Query filter pattern.
     */
    private static final Pattern QUERY_FILTER_PATTERN = Pattern.compile("(.*)ORDER([ ]*)BY(.*)");
    
	/**
	 * Adds the publication filter.
	 *
	 * @param queryCtx the query ctx
	 * @param nuxeoRequest the nuxeo request
	 * @param boolean ignoreNavigationElement
	 * @return the string
	 */
	public static String addPublicationFilter(NuxeoQueryFilterContext queryCtx,String nuxeoRequest, boolean ignoreNavigationElement) {


	    // adapt thanks to CMSCustomizer
		
		INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
		
		CMSServiceCtx ctx = getCMSCtx();
		
		String state = String.valueOf(queryCtx.getState());
		ctx.setDisplayLiveVersion(state);
		
		try {
			return nuxeoService.getCMSCustomizer().addPublicationFilter(ctx, nuxeoRequest, queryCtx.getPolicy(), ignoreNavigationElement);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

      
	    
	}
	
	
	
	
	   /**
     * Adds the publication filter.
     *
     * @param queryCtx the query ctx
     * @param nuxeoRequest the nuxeo request
     * @return the string
     */
    public static String addPublicationFilter(NuxeoQueryFilterContext queryCtx,String nuxeoRequest) {

      return addPublicationFilter(queryCtx, nuxeoRequest, true);

    }
	
	
	/**
     * Adds the search filter.
     *
     * @param queryCtx the query ctx
     * @param nuxeoRequest the nuxeo request
     * @return the string
     */
    public static String addSearchFilter(NuxeoQueryFilterContext queryCtx,String nuxeoRequest) {

        // adapt thanks to CMSCustomizer
        
        INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        
        CMSServiceCtx ctx = getCMSCtx();
      
        try {
            return nuxeoService.getCMSCustomizer().addSearchFilter(ctx, nuxeoRequest, queryCtx.getPolicy());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
    /**
     * Get the query state according to given version.
     * 
     * @param version
     * @return the state;
     */
    public static final int getState(String version){
        int state = NuxeoQueryFilterContext.STATE_DEFAULT;
        
        if(StringUtils.isNotBlank(version)){
            
            if("1".equals(version)){
                state = NuxeoQueryFilterContext.STATE_LIVE;
            } else if("2".equals(version)){
                state = NuxeoQueryFilterContext.STATE_LIVE_N_PUBLISHED;
            }
            
        }
        
        return state;
    }

}
