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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/**
 * The Class VocabularyHelper.
 * 
 * Useful utilities for manipulating nuxeo vocabularies
 */
public class VocabularyHelper {

    /**
     * Call the vocabulary commands
     *
     * @param ctx the ctx
     * @param vocabularyNames the vocabulary name
     * @param multiLevel true if voc is multi level : parent/child
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    private static VocabularyEntry callCommand(NuxeoController ctx, List<String> vocabularyNames, boolean multiLevel)  {

        NuxeoController vocabCtx = new NuxeoController(ctx.getRequest(), ctx.getResponse(), ctx.getPortletCtx());

        vocabCtx.setCacheTimeOut(3600 * 1000L);

        vocabCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        vocabCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        VocabularyEntry vocab = (VocabularyEntry) vocabCtx.executeNuxeoCommand(new VocabularyLoaderCommand(vocabularyNames, multiLevel));

        return vocab;
    }


    /**
     * Gets the vocabulary label.
     *
     * @param ctx the ctx
     * @param vocabularyName the vocabulary name
     * @param key the key
     * @return the vocabulary label
     * @throws Exception the exception
     */
    public static String getVocabularyLabel(NuxeoController ctx, String vocabularyName, String key)  {

        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyLabel(ctx, vocabs, key);


    }


    /**
     * Gets the vocabulary label.
     *
     * @param ctx the ctx
     * @param vocabs the vocabs
     * @param key the key
     * @return the vocabulary label
     * @throws Exception the exception
     */
    public static String getVocabularyLabel(NuxeoController ctx, List<String> vocabs, String key) {

        VocabularyEntry vocab = callCommand(ctx, vocabs, false);

        if (vocab != null) {
            VocabularyEntry child = vocab.getChild(key);
            if (child != null)
                return vocab.getChild(key).getLabel();
        }

        return null;

    }


    /**
     * Gets the vocabulary entry.
     *
     * @param ctx the ctx
     * @param vocabularyName the vocabulary name
     * @param key the key
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, String vocabularyName, String key)  {
        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyEntry(ctx, vocabs, key);
    }

    
    /**
     * Gets the vocabulary entry.
     *
     * @param ctx the ctx
     * @param vocabs the vocabs
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, List<String> vocabs)  {
        VocabularyEntry vocab = callCommand(ctx, vocabs, false);
        return vocab;
    }

    /**
     * Gets the vocabulary entry.
     *
     * @param ctx the ctx
     * @param vocabs the vocabs
     * @param key the key
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, List<String> vocabs, String key)  {
        VocabularyEntry vocab = callCommand(ctx, vocabs, false);
        return vocab;
    }
    
    /**
     * Gets the vocabulary entry.
     *
     * @param ctx the ctx
     * @param vocabularyName the vocabulary name
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, String vocabularyName, Boolean multiLevel)  {
        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyEntry(ctx, vocabs, multiLevel);
    }

    
    
    /**
     * Gets the vocabulary entry.
     *
     * @param ctx the ctx
     * @param vocabularyName the vocabulary name
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, String vocabularyName)  {
        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyEntry(ctx, vocabs, false);
    }


    /**
     * Gets the vocabulary entry.
     *
     * @param ctx the ctx
     * @param vocabs the vocabs
     * @param multiLevel true if voc is multi level : parent/child
     * @return the vocabulary entry
     * @throws Exception the exception
     */
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, List<String> vocabs, Boolean multiLevel)  {
        VocabularyEntry vocab = callCommand(ctx, vocabs, multiLevel);
        return vocab;
    }
    
    
    

    /**
     * The Class VocabularyLoaderCommand.
     */
    private static class VocabularyLoaderCommand implements INuxeoCommand {


        /** The vocab names. */
        List<String> vocabNames;

        /** The string vocab names. */
        String stringVocabNames = null;
        
        boolean multiLevel = false;

        /**
         * Instantiates a new vocabulary loader command.
         *
         * @param vocabNames the vocab names
         * @param multiLevel true if voc is multi level : parent/child
         */
        public VocabularyLoaderCommand(List<String> vocabNames, boolean multiLevel) {
            super();
            this.vocabNames = vocabNames;
            this.multiLevel = multiLevel;

            stringVocabNames = "";
            for (String vocab : vocabNames) {
                if (stringVocabNames.length() > 0)
                    stringVocabNames += ";";
                stringVocabNames += vocab;
            }
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
                Iterator itr = vocabulariesObj.iterator();
                while (itr.hasNext()) {
                    JSONObject vocabulary = (JSONObject) itr.next();
                    String key = vocabulary.getString("key");
                    String label = vocabulary.getString("value");
                    String parentId = null;
                    if(vocabulary.containsKey("parent")) {
                    	parentId = vocabulary.getString("parent");
                    }
                    
                    if (label.startsWith("label.directories"))
                        label = key;
                    String DecodedLabel = URLDecoder.decode(label, "UTF-8");

                    entry = new VocabularyEntry(key, DecodedLabel);

                    
                    JSONArray children = null;
                    if (vocabulary.has("children")) {
                        children = vocabulary.getJSONArray("children");
                        if (null != children) {
                            parseVocabularies(entry, children);
                        }
                    }

                    if(multiLevel == true && StringUtils.isNotBlank(parentId)) {
                    	VocabularyEntry parentVoc = parent.getChildren().get(parentId);
                    	if(parentVoc != null){// DCH FIXME: temporary fix in case of more than 2 levels vocabularies
                    	    parentVoc.getChildren().put(entry.getId(), entry);
                    	}
                    }
                    else {
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

                if (!"key_root".equals(vocab.getId()))
                    if (id.equals(child.getId()))
                        return true;

                if (isAChild(id, child))
                    return true;
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
                if (isAChild(child.getId(), vocab))
                    removedEntries.add(child.getId());

            }

            for (String key : removedEntries) {
                vocab.getChildren().remove(key);
            }


        }


        /* (non-Javadoc)
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
         */
        public Object execute(Session nuxeoSession) throws Exception {


            Blob blob = (Blob) nuxeoSession.newRequest("Document.GetVocabularies").setHeader(Constants.HEADER_NX_SCHEMAS, "*")
                    .set("vocabularies", stringVocabNames).execute();
            String content = IOUtils.toString(blob.getStream(), "UTF-8");

            JSONObject rootObject = new JSONObject();
            rootObject.element("key", "key_root");
            rootObject.element("value", "value_root");
            rootObject.element("children", JSONArray.fromObject(content));
            JSONArray root = new JSONArray();
            root.element(rootObject);

            VocabularyEntry entries = parseVocabularies(null, root);
            removeDuplicatedChilds(entries);
            return entries;
        }


        /* (non-Javadoc)
         * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
         */
        public String getId() {
            return stringVocabNames;
        }
    }


    /**
     * Get vocabulary array in json format.
     *
     * @param ctx the ctx
     * @param vocabularyName the vocabulary name
     * @param multiLevel true if voc is multi level : parent/child
     * @return json array
     * @throws Exception the exception
     */
    public static JSONArray getJsonVocabulary(NuxeoController ctx, String vocabularyName, boolean multiLevel)  {

        NuxeoController vocabCtx = new NuxeoController(ctx.getRequest(), ctx.getResponse(), ctx.getPortletCtx());

        vocabCtx.setCacheTimeOut(3600 * 1000L);

        vocabCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        vocabCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        JSONArray vocab = (JSONArray) vocabCtx.executeNuxeoCommand(new JsonVocabularyLoaderCommand(vocabularyName));

        return vocab;
    }
    
    /**
     * The Class VocabularyLoaderCommand.
     */
    private static class JsonVocabularyLoaderCommand implements INuxeoCommand {

    	String vocabName;
    	
    	public JsonVocabularyLoaderCommand(String vocabName) {
    		
    		this.vocabName = vocabName;
    	}
    	
		@Override
		public Object execute(Session nuxeoSession) throws Exception {
			
            Blob blob = (Blob) nuxeoSession.newRequest("Document.GetVocabularies").setHeader(Constants.HEADER_NX_SCHEMAS, "*")
                    .set("vocabularies", vocabName).execute();
            String content = IOUtils.toString(blob.getStream(), "UTF-8");

            return JSONArray.fromObject(content);
		}

		@Override
		public String getId() {
			return "JsonVocabularyLoaderCommand/"+vocabName;
		}
    	
    }

}
