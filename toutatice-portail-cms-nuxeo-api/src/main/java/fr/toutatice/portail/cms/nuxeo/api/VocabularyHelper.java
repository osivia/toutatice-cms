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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/**
 * Vocabulary helper.
 *
 * Useful utilities for manipulating nuxeo vocabularies
 */
public class VocabularyHelper {

    /**
     * Call vocabulary command.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyNames vocabulary names
     * @param multiLevel multi-level vocabulary indicator (eg: parent/child)
     * @return vocabulary entry
     */
    private static VocabularyEntry callCommand(NuxeoController nuxeoController, List<String> vocabularyNames, boolean multiLevel) {
        NuxeoController vocabularyController = new NuxeoController(nuxeoController.getRequest(), nuxeoController.getResponse(), nuxeoController.getPortletCtx());
        vocabularyController.setCacheTimeOut(TimeUnit.HOURS.toMillis(1));
        vocabularyController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        vocabularyController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        VocabularyLoaderCommand command = new VocabularyLoaderCommand(vocabularyNames, multiLevel);
        return (VocabularyEntry) vocabularyController.executeNuxeoCommand(command);
    }


    /**
     * Get vocabulary label.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyNames vocabulary names
     * @param key vocabulary key
     * @return vocabulary label
     */
    public static String getVocabularyLabel(NuxeoController nuxeoController, List<String> vocabularyNames, String key) {
        String label = null;

        VocabularyEntry vocabularyEntry = callCommand(nuxeoController, vocabularyNames, false);
        if (vocabularyEntry != null) {
            VocabularyEntry child = vocabularyEntry.getChild(key);
            if (child != null) {
                label = child.getLabel();
            }
        }

        return label;
    }


    /**
     * Get vocabulary label.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyName vocabulary name
     * @param key vocabulary key
     * @return vocabulary label
     */
    public static String getVocabularyLabel(NuxeoController nuxeoController, String vocabularyName, String key) {
        List<String> vocabularyNames = new ArrayList<String>(1);
        vocabularyNames.add(vocabularyName);
        return getVocabularyLabel(nuxeoController, vocabularyNames, key);
    }


    /**
     * Get vocabulary entry.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyNames vocabulary names
     * @param multiLevel multi-level vocabulary indicator (eg: parent/child)
     * @return vocabulary entry
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController nuxeoController, List<String> vocabularyNames, Boolean multiLevel) {
        return callCommand(nuxeoController, vocabularyNames, multiLevel);
    }


    /**
     * Get vocabulary entry.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyName vocabulary name
     * @param multiLevel multi-level vocabulary indicator (eg: parent/child)
     * @return vocabulary entry
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController nuxeoController, String vocabularyName, boolean multiLevel) {
        List<String> vocabularyNames = new ArrayList<String>(1);
        vocabularyNames.add(vocabularyName);
        return getVocabularyEntry(nuxeoController, vocabularyNames, multiLevel);
    }


    /**
     * Get vocabulary entry.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyName vocabulary name
     * @return vocabulary entry
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController nuxeoController, String vocabularyName) {
        return getVocabularyEntry(nuxeoController, vocabularyName, false);
    }


    /**
     * Get vocabulary entry.
     *
     * @param nuxeoController Nuxeo controller
     * @param vocabularyNames vocabulary names
     * @return vocabulary entry
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController nuxeoController, List<String> vocabularyNames) {
        return getVocabularyEntry(nuxeoController, vocabularyNames, false);
    }


    /**
     * Vocabulary loader command.
     *
     * @see INuxeoCommand
     */
    private static class VocabularyLoaderCommand implements INuxeoCommand {

        /** Vocabulary names, separated by ";". */
        private String vocabularyNames;
        /** Multi-level vocabulary indicator. */
        private final boolean multiLevel;


        /**
         * Constructor.
         *
         * @param vocabularyNames vocabulary names
         * @param multiLevel multi-level vocabulary indicator (eg: parent/child)
         */
        public VocabularyLoaderCommand(List<String> vocabNames, boolean multiLevel) {
            super();
            this.vocabularyNames = StringUtils.join(vocabNames, ";");
            this.multiLevel = multiLevel;
        }


        /**
         * Parses the vocabularies.
         *
         * @param parent the parent
         * @param vocabulariesObj the vocabularies obj
         * @return the vocabulary entry
         * @throws UnsupportedEncodingException the unsupported encoding exception
         */
        private VocabularyEntry parseVocabularies(VocabularyEntry parent, JSONArray vocabulariesObj) throws UnsupportedEncodingException {
            VocabularyEntry entry = null;

            if (null == parent) {
                parent = new VocabularyEntry("root", "root");
            }

            if (!vocabulariesObj.isEmpty()) {
                Iterator<?> iterator = vocabulariesObj.iterator();
                while (iterator.hasNext()) {
                    JSONObject vocabulary = (JSONObject) iterator.next();
                    String key = vocabulary.getString("key");
                    String label = vocabulary.getString("value");
                    String parentId = null;
                    if (vocabulary.containsKey("parent")) {
                        parentId = vocabulary.getString("parent");
                    }

                    if (label.startsWith("label.directories")) {
                        label = key;
                    }
                    String DecodedLabel = URLDecoder.decode(label, "UTF-8");

                    entry = new VocabularyEntry(key, DecodedLabel);


                    JSONArray children = null;
                    if (vocabulary.has("children")) {
                        children = vocabulary.getJSONArray("children");
                        if (null != children) {
                            this.parseVocabularies(entry, children);
                        }
                    }

                    if ((this.multiLevel == true) && StringUtils.isNotBlank(parentId)) {
                        VocabularyEntry parentVoc = parent.getChildren().get(parentId);
                        if (parentVoc != null) { // DCH FIXME: temporary fix in case of more than 2 levels vocabularies
                            parentVoc.getChildren().put(entry.getId(), entry);
                        }
                    } else {
                        parent.getChildren().put(entry.getId(), entry);
                    }
                }
            }

            return entry;
        }


        /**
         * Checks if is a child.
         *
         * @param id the id
         * @param vocab the vocab
         * @return true, if is a child
         */
        public boolean isAChild(String id, VocabularyEntry vocab) {
            for (Map.Entry<String, VocabularyEntry> entry : vocab.getChildren().entrySet()) {
                VocabularyEntry child = entry.getValue();

                if (!"key_root".equals(vocab.getId())) {
                    if (id.equals(child.getId())) {
                        return true;
                    }
                }

                if (this.isAChild(id, child)) {
                    return true;
                }
            }

            return false;
        }


        /**
         * Permet de filtrer les élements qui sont également des enfants
         *
         * (cas des hiérarchies décrites dans un seul vocabulaire . ex : niveaux d'enseignements)
         *
         * @param vocab the vocab
         */
        public void removeDuplicatedChilds(VocabularyEntry vocab) {
            List<String> removedEntries = new ArrayList<String>();

            for (Map.Entry<String, VocabularyEntry> entry : vocab.getChildren().entrySet()) {
                VocabularyEntry child = entry.getValue();
                if (this.isAChild(child.getId(), vocab)) {
                    removedEntries.add(child.getId());
                }
            }

            for (String key : removedEntries) {
                vocab.getChildren().remove(key);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Object execute(Session nuxeoSession) throws Exception {
            OperationRequest operationRequest = nuxeoSession.newRequest("Document.GetVocabularies");
            operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
            operationRequest.set("vocabularies", this.vocabularyNames);
            Blob blob = (Blob) operationRequest.execute();
            String content = IOUtils.toString(blob.getStream(), "UTF-8");

            JSONObject rootObject = new JSONObject();
            rootObject.element("key", "key_root");
            rootObject.element("value", "value_root");
            rootObject.element("children", JSONArray.fromObject(content));
            JSONArray root = new JSONArray();
            root.element(rootObject);

            VocabularyEntry entries = this.parseVocabularies(null, root);
            this.removeDuplicatedChilds(entries);
            return entries;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String getId() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.getClass().getCanonicalName());
            builder.append("/");
            builder.append(this.vocabularyNames);
            builder.append("/");
            builder.append(this.multiLevel);
            return builder.toString();
        }

    }

}
