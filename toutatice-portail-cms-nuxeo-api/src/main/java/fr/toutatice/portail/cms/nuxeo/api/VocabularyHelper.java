package fr.toutatice.portail.cms.nuxeo.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

public class VocabularyHelper {

    private static VocabularyEntry callCommand(NuxeoController ctx, List<String> vocabularyNames) throws Exception {

        NuxeoController vocabCtx = new NuxeoController(ctx.getRequest(), ctx.getResponse(), ctx.getPortletCtx());

        vocabCtx.setCacheTimeOut(3600 * 1000L);

        vocabCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        vocabCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        VocabularyEntry vocab = (VocabularyEntry) vocabCtx.executeNuxeoCommand(new VocabularyLoaderCommand(vocabularyNames));

        return vocab;
    }


    public static String getVocabularyLabel(NuxeoController ctx, String vocabularyName, String key) throws Exception {

        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyLabel(ctx, vocabs, key);


    }


    public static String getVocabularyLabel(NuxeoController ctx, List<String> vocabs, String key) throws Exception {

        VocabularyEntry vocab = callCommand(ctx, vocabs);

        if (vocab != null) {
            VocabularyEntry child = vocab.getChild(key);
            if (child != null)
                return vocab.getChild(key).getLabel();
        }

        return null;

    }


    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, String vocabularyName, String key) throws Exception {
        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyEntry(ctx, vocabs, key);
    }


    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, List<String> vocabs, String key) throws Exception {
        VocabularyEntry vocab = callCommand(ctx, vocabs);
        return vocab;
    }
    
    
    
    
    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, String vocabularyName) throws Exception {
        List<String> vocabs = new ArrayList<String>();
        vocabs.add(vocabularyName);
        return getVocabularyEntry(ctx, vocabs);
    }


    public static VocabularyEntry getVocabularyEntry(NuxeoController ctx, List<String> vocabs) throws Exception {
        VocabularyEntry vocab = callCommand(ctx, vocabs);
        return vocab;
    }
    
    
    

    private static class VocabularyLoaderCommand implements INuxeoCommand {

        // private static Log logger = LogFactory.getLog(VocabularyLoader.class);

        List<String> vocabNames;

        String stringVocabNames = null;

        public VocabularyLoaderCommand(List<String> vocabNames) {
            super();
            this.vocabNames = vocabNames;

            stringVocabNames = "";
            for (String vocab : vocabNames) {
                if (stringVocabNames.length() > 0)
                    stringVocabNames += ";";
                stringVocabNames += vocab;
            }
        }


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

                    parent.getChildren().put(entry.getId(), entry);
                }
            }

            return entry;
        }


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
         * @param id
         * @param vocab
         * @return
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


        public Object execute(Session nuxeoSession) throws Exception {


            Blob blob = (Blob) nuxeoSession.newRequest("Document.GetVocabularies").setHeader(Constants.HEADER_NX_SCHEMAS, "*")
                    .set("vocabularies", stringVocabNames).set("locale", Locale.FRANCE.toString()).execute();
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


        public String getId() {
            return stringVocabNames;
        }
    }


}
