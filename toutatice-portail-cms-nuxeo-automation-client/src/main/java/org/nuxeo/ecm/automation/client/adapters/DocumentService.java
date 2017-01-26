/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ecm.automation.client.adapters;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Blobs;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class DocumentService {

    public static final String FetchDocument = "Document.Fetch";

    public static final String CreateDocument = "Document.TTCCreate";
    
    public static final String SaveDocument = "Document.Save";

    public static final String DeleteDocument = "Document.Delete";

    public static final String CopyDocument = "Document.Copy";

    public static final String MoveDocument = "Document.Move";

    public static final String GetDocumentChildren = "Document.GetChildren";

    public static final String GetDocumentChild = "Document.GetChild";

    public static final String GetDocumentParent = "Document.GetParent";

    public static final String Query = "Document.Query";

    public static final String SetPermission = "Document.SetACE";

    public static final String AddPermission = "Document.AddPermission";

    public static final String RemovePermissions = "Document.RemovePermission";

    public static final String RemoveAcl = "Document.RemoveACL";

    public static final String SetDocumentState = "Document.SetLifeCycle";

    public static final String LockDocument = "Document.Lock";

    public static final String UnlockDocument = "Document.Unlock";

    public static final String SetProperty = "Document.SetProperty";

    public static final String RemoveProperty = "Document.RemoveProperty";

    public static final String UpdateDocument = "Document.TTCUpdate";

    public static final String PublishDocument = "Document.Publish";

    public static final String CreateRelation = "Relations.CreateRelation";

    public static final String GetRelations = "Relations.GetRelations";

    public static final String SetBlob = "Blob.Attach";

    public static final String RemoveBlob = "Blob.Remove";

    public static final String GetBlob = "Blob.Get";

    public static final String GetBlobs = "Blob.GetList";

    public static final String CreateVersion = "Document.CreateVersion";

    public static final String FireEvent = "Notification.SendEvent";

    // The following are not yet implemented

    public static final String CheckOut = "Document.CheckOut";

    public static final String CheckIn = "Document.CheckIn";

    // //TODO GetAcl?

    protected Session session;

    public DocumentService(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }

    public Document getDocument(String ref) throws Exception {
        return this.getDocument(DocRef.newRef(ref), null);
    }

    public Document getDocument(DocRef ref) throws Exception {
        return this.getDocument(ref, null);
    }

    public Document getDocument(DocRef ref, String schemas) throws Exception {
        OperationRequest req = this.session.newRequest(FetchDocument).set("value",
                ref);
        if (schemas != null) {
            req.setHeader(Constants.HEADER_NX_SCHEMAS, schemas);
        }
        return (Document) req.execute();
    }

    public Document getRootDocument() throws Exception {
        return this.getDocument(new PathRef("/"));
    }

    public Document createDocument(DocRef parent, String type, String name)
            throws Exception {
        return this.createDocument(parent, type, name, null);
    }

    public Document createDocument(DocRef parent, String type, String name,
            PropertyMap properties) throws Exception {
        OperationRequest req = this.session.newRequest(CreateDocument).setInput(
                parent).set("type", type).set("name", name);
        if ((properties != null) && !properties.isEmpty()) {
            req.set("properties", properties);
        }
        return (Document) req.execute();
    }
    
    public Document save(DocRef doc) throws Exception {
        return (Document) this.session.newRequest(SaveDocument).setInput(doc).execute();
    }

    public void remove(DocRef doc) throws Exception {
        this.session.newRequest(DeleteDocument).setInput(doc).execute();
    }

    public void remove(String ref) throws Exception {
        this.session.newRequest(DeleteDocument).setInput(DocRef.newRef(ref)).execute();
    }

    public Document copy(DocRef src, DocRef targetParent) throws Exception {
        return this.copy(src, targetParent, null);
    }

    public Document copy(DocRef src, DocRef targetParent, String name)
            throws Exception {
        OperationRequest req = this.session.newRequest(CopyDocument).setInput(src).set(
                "target", targetParent);
        if (name != null) {
            req.set("name", name);
        }
        return (Document) req.execute();
    }

    public Document move(DocRef src, DocRef targetParent) throws Exception {
        return this.move(src, targetParent, null);
    }

    public Document move(DocRef src, DocRef targetParent, String name)
            throws Exception {
        OperationRequest req = this.session.newRequest(MoveDocument).setInput(src).set(
                "target", targetParent);
        if (name != null) {
            req.set("name", name);
        }
        return (Document) req.execute();
    }

    public Documents getChildren(DocRef docRef) throws Exception {
        return (Documents) this.session.newRequest(GetDocumentChildren).setInput(
                docRef).execute();
    }

    public Document getChild(DocRef docRef, String name) throws Exception {
        return (Document) this.session.newRequest(GetDocumentChild).setInput(
                docRef).set("name", name).execute();
    }

    public Document getParent(DocRef docRef) throws Exception {
        return (Document) this.session.newRequest(GetDocumentParent).setInput(docRef).execute();
    }

    public Documents getParent(DocRef docRef, String type) throws Exception {
        return (Documents) this.session.newRequest(GetDocumentParent).setInput(
                docRef).set("type", type).execute();
    }

    public Documents query(String query) throws Exception {
        return (Documents) this.session.newRequest(Query).set("query", query).execute();
    }

    /**
     * Set a local ACE before block inheritance if it exists.
     */
    public Document addPermission(DocRef doc, String user, String permission) throws Exception {
        return this.addPermission(doc, user, permission, null, false);
    }

    public Document addPermission(DocRef doc, String user, String permission, String acl) throws Exception {
        return this.addPermission(doc, user, permission, acl, false);
    }

    public Document addPermission(DocRef doc, String user, String permission, String acl, boolean blockInheritance) throws Exception {
        OperationRequest req = this.session.newRequest(AddPermission).setInput(doc).set("user", user).set("permission", permission).set("blockInheritance",
                blockInheritance);
        return (Document) req.execute();
    }

    public Document setPermission(DocRef doc, String user, String permission)
            throws Exception {
        return this.setPermission(doc, user, permission, null, true);
    }

    public Document setPermission(DocRef doc, String user, String permission, boolean granted) throws Exception {
        return this.setPermission(doc, user, permission, null, granted);
    }

    public Document setPermission(DocRef doc, String user, String permission, String acl, boolean granted) throws Exception {
        OperationRequest req = this.session.newRequest(SetPermission).setInput(doc).set("user", user).set("permission", permission).set("grant", granted)
                .set("overwrite", false);
        if (acl != null) {
            req.set("acl", acl);
        }
        return (Document) req.execute();
    }

    public Document removePermissions(DocRef doc, String user, String acl) throws Exception {
        OperationRequest req = this.session.newRequest(RemovePermissions).setInput(doc).set("user", user);
        if (acl != null) {
            req.set("acl", acl);
        }
        return (Document) req.execute();
    }

    public Document removeAcl(DocRef doc, String acl) throws Exception {
        return (Document) this.session.newRequest(RemoveAcl).setInput(doc).set(
                "acl", acl).execute();
    }

    public Document setState(DocRef doc, String state) throws Exception {
        return (Document) this.session.newRequest(SetDocumentState).setInput(doc).set(
                "value", state).execute();
    }

    public Document lock(DocRef doc) throws Exception {
        return this.lock(doc, null);
    }

    public Document lock(DocRef doc, String lock) throws Exception {
        OperationRequest req = this.session.newRequest(LockDocument).setInput(doc);
        if (lock != null) {
            req.set("owner", lock);
        }
        return (Document) req.execute();
    }

    public Document unlock(DocRef doc) throws Exception {
        return (Document) this.session.newRequest(UnlockDocument).setInput(doc).execute();
    }

    // TODO: value Serializable?
    public Document setProperty(DocRef doc, String key, String value)
            throws Exception {
        return (Document) this.session.newRequest(SetProperty).setInput(doc).set(
                "xpath", key).set("value", value).execute();
    }

    public Document removeProperty(DocRef doc, String key) throws Exception {
        return (Document) this.session.newRequest(RemoveProperty).setInput(doc).set(
                "xpath", key).execute();
    }

    public Document update(DocRef doc, PropertyMap properties) throws Exception {
        return (Document) this.session.newRequest(UpdateDocument).setInput(doc).set(
                "properties", properties).execute();
    }

    public Document publish(DocRef doc, DocRef section) throws Exception {
        return this.publish(doc, section, true);
    }

    public Document publish(DocRef doc, DocRef section, boolean override)
            throws Exception {
        return (Document) this.session.newRequest(PublishDocument).setInput(doc).set(
                "target", section).set("override", override).execute();
    }

    public Document createRelation(DocRef subject, String predicate,
            DocRef object) throws Exception {
        return (Document) this.session.newRequest(CreateRelation).setInput(subject).set(
                "object", object).set("predicate", predicate).execute();
    }

    public Documents getRelations(DocRef doc, String predicate)
            throws Exception {
        return this.getRelations(doc, predicate, true);
    }

    public Documents getRelations(DocRef doc, String predicate, boolean outgoing)
            throws Exception {
        return (Documents) this.session.newRequest(GetRelations).setInput(doc).set(
                "predicate", predicate).set("outgoing", outgoing).execute();
    }

    /**
     * @since 5.5
     */
    public Documents getRelations(DocRef doc, String predicate, boolean outgoing, String graphName)
            throws Exception {
        return (Documents) this.session.newRequest(GetRelations).setInput(doc).set(
                "predicate", predicate).set("outgoing", outgoing).set("graphName", graphName).execute();
    }

    public void setBlob(DocRef doc, Blob blob) throws Exception {
        this.setBlob(doc, blob, null);
    }

    public void setBlob(DocRef doc, Blob blob, String xpath) throws Exception {
        OperationRequest req = this.session.newRequest(SetBlob).setInput(blob).set(
                "document", doc);
        if (xpath != null) {
            req.set("xpath", xpath);
        }
        req.setHeader(Constants.HEADER_NX_VOIDOP, "true");
        req.execute();
    }

    public void removeBlob(DocRef doc) throws Exception {
        this.removeBlob(doc, null);
    }

    public void removeBlob(DocRef doc, String xpath) throws Exception {
        OperationRequest req = this.session.newRequest(RemoveBlob).setInput(doc);
        if (xpath != null) {
            req.set("xpath", xpath);
        }
        req.setHeader(Constants.HEADER_NX_VOIDOP, "true");
        req.execute();
    }

    public FileBlob getBlob(DocRef doc) throws Exception {
        return this.getBlob(doc, null);
    }

    public FileBlob getBlob(DocRef doc, String xpath) throws Exception {
        OperationRequest req = this.session.newRequest(GetBlob).setInput(doc);
        if (xpath != null) {
            req.set("xpath", xpath);
        }
        return (FileBlob) req.execute();
    }

    public Blobs getBlobs(DocRef doc) throws Exception {
        return this.getBlobs(doc, null);
    }

    public Blobs getBlobs(DocRef doc, String xpath) throws Exception {
        OperationRequest req = this.session.newRequest(GetBlobs).setInput(doc);
        if (xpath != null) {
            req.set("xpath", xpath);
        }
        return (Blobs) req.execute();
    }

    public Document createVersion(DocRef doc) throws Exception {
        return this.createVersion(doc, null);
    }

    /**
     * Increment is one of "None", "Major", "Minor". If null the server default
     * will be used.
     *
     * See {@link VersionIncrement}
     */
    public Document createVersion(DocRef doc, String increment)
            throws Exception {
        OperationRequest req = this.session.newRequest(CreateVersion).setInput(doc);
        if (increment != null) {
            req.set("increment", increment);
        }
        return (Document) req.execute();
    }

    public void fireEvent(String event) throws Exception {
        this.fireEvent(null, event);
    }

    public void fireEvent(DocRef doc, String event) throws Exception {
        OperationRequest req = this.session.newRequest(CreateVersion).setInput(doc);
        req.setHeader(Constants.HEADER_NX_VOIDOP, "true");
        req.execute();
    }
}