package fr.toutatice.portail.cms.nuxeo.portlets.bridge;

import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyIdentifier;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyLoaderCommand;

public class VocabularyHelper {

	public static String getVocabularyLabel(NuxeoController ctx, String vocabularyName, String key) throws Exception {

		NuxeoController vocabCtx = new NuxeoController(ctx.getRequest(), ctx.getResponse(), ctx.getPortletCtx());

		
		// v2.0.21 : le contexte n'était pas le bon !!!
		//ctx.setCacheTimeOut(3600 * 1000L);

		vocabCtx.setCacheTimeOut(3600 * 1000L);
		//ctx.setAsynchronousUpdates(true);
		
		vocabCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
		vocabCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);


		VocabularyIdentifier vocabIdentifier = new VocabularyIdentifier(vocabularyName, vocabularyName);

		VocabularyEntry vocab = (VocabularyEntry) vocabCtx.executeNuxeoCommand(new VocabularyLoaderCommand(
				vocabIdentifier));
		if (vocab != null) {
			VocabularyEntry child = vocab.getChild(key);
			if (child != null)
				return vocab.getChild(key).getLabel();
		}

		return null;

	}

}
