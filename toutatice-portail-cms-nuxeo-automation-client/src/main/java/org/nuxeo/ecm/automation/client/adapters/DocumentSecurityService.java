/**
 * 
 */
package org.nuxeo.ecm.automation.client.adapters;

import java.util.List;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.DocumentPermissions;
import org.nuxeo.ecm.automation.client.model.ListString;


/**
 * 
 * @author david
 *
 */
public class DocumentSecurityService {
    
    /** Local ACL identifier. */
    public static final String LOCAL_ACL = "local";
    /** Inherited ACL identidier. */
    public static final String INHERITED_ACL = "inherited";
    
    /** Add permissions operation identifier. */
    public static final String ADD_PERMISSIONS_OP = "Document.AddACEs";
    /** Remove permissions operation identifier. */
    public static final String REMOVE_PERMISSIONS_OP = "Document.RemoveACEs";
    /** Remove group (inherited / local) of permissions operation identifier. */
    public static final String REMOVE_GROUP_PERMISSIONS_OP = "Document.RemoveACL";
    
    /**
     * Client session.
     */
    protected Session session;
    
    /**
     * Constructor.
     */
    public DocumentSecurityService(Session session) {
        this.session = session;
    }
    
    /**
     * Getter for session.
     * 
     * @return session
     */
    public Session getSession(){
        return this.session;
    }
    
    /**
     * Adds permissions in given ACL 
     * blocking or not inheritance.
     * 
     * @param document
     * @param permissions
     * @param blockInheritance
     * @return document
     * @throws Exception
     */
    public Document addPermissions(Document document, DocumentPermissions permissions, String acl, boolean blockInheritance) throws Exception {
        // Coherence check
        if(blockInheritance && INHERITED_ACL.equalsIgnoreCase(acl)){
            throw new Exception("You can not add permissions in inherited ACL if you block inheritance");
        }
        
        OperationRequest request = getSession().newRequest(ADD_PERMISSIONS_OP)
            .setInput(document)
            .set("acl", acl)
            .set("aces", permissions.toString())
            .set("blockInheritance", blockInheritance);
        return (Document) request.execute(); 
    }
    
    /**
     * Removes ACEs in given ACL, blocking or not inheritance:
     * - if removeAll is true, removes all ACEs of given ACL
     * - if userNames is set, removes all ACEs of given users.
     * 
     * @param docRef
     * @param permissions
     * @param userNames
     * @param acl
     * @param removeAll
     * @param blockInheritance
     * @return document
     * @throws Exception
     */
    public Document removePermissions(DocRef docRef, DocumentPermissions permissions, List<String> userNames, String acl, boolean removeAll,
            boolean blockInheritance) throws Exception {
        // Coherence check
        if(blockInheritance && INHERITED_ACL.equalsIgnoreCase(acl)){
            throw new Exception("You can not remove permissions in inherited ACL if you block inheritance");
        }
        
        OperationRequest request = getSession().newRequest(REMOVE_PERMISSIONS_OP)
                .setInput(docRef)
            .set("acl", acl)
            .set("aces", permissions)
            .set("userNames", ListString.getInstance().getAsString(userNames))
            .set("all", removeAll)
            .set("blockInheritance", blockInheritance);
        return (Document) request.execute(); 
    }
    
    /**
     * Remove given ACL on document,
     * i.e. inherited or local ACL.
     * 
     * @param docRef
     * @param acl
     * @return document
     * @throws Exception
     */
    public Document removeGroupPermissions(DocRef docRef, String acl) throws Exception {
        OperationRequest request = getSession().newRequest(REMOVE_PERMISSIONS_OP)
                .setInput(docRef)
                .set("acl", acl);
        return (Document) request.execute();
    }
    
}
