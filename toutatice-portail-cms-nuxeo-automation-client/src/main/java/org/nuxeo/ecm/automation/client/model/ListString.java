/**
 * 
 */
package org.nuxeo.ecm.automation.client.model;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;



/**
 * Adapt List<String> to String like "a, b, c, .." to pass
 * as StringList operation's parameter.
 * 
 * @author david
 *
 */
public class ListString {
    
    /** Singleton instance. */
    private static ListString instance;

    /**
     * Constructor.
     */
    private ListString() {
        super();
    }
    
    /**
     * Singleton getter.
     * 
     * @return ListString instance
     */
    public static synchronized ListString getInstance(){
        if(instance == null){
            instance = new ListString();
        }
        return instance;
    }
    
    public String getAsString(List<String> list){
        StringBuffer commaString = new StringBuffer();
        
        if(CollectionUtils.isNotEmpty(list)) {
            Iterator<String> listIt = list.iterator();
            while (listIt.hasNext()){
                commaString.append(listIt.next());
                
                if(listIt.hasNext()){
                    commaString.append(",");
                }
            }
        }
        
        return commaString.toString();
    }

}
