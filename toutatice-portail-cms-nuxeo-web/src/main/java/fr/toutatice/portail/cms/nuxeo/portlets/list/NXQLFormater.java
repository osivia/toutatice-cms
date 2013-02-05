package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.selectors.VocabSelectorPortlet;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyIdentifier;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyLoaderCommand;

public class NXQLFormater {
	
	public String formatTextSearch( String fieldName, List<String> searchValue){
		
		StringBuffer request = new StringBuffer();
		request.append("(");
		
		boolean firstItem = true;
		
		for( String searchWord : searchValue)	{
			
			if( !firstItem)
				request.append(" OR ");

			request.append( fieldName+" ILIKE \"%" + searchWord + "%\"");
			
			firstItem = false;
		}
		request.append(")");
		
		return request.toString();
	}
	
	public String formatVocabularySearch(PortletRequest portletRequest, List vocabsNames, String fieldName, List<String> selectedVocabsEntries) throws Exception{
		
		StringBuffer request = new StringBuffer();
		request.append("(");
		
		boolean firstItem = true;
		
		for( String selectedVocabsEbtry : selectedVocabsEntries)	{
			
			if( !firstItem)
				request.append(" OR ");

			if(selectedVocabsEbtry.contains(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE))
				request.append(formatOthersVocabularyEntriesSearch(portletRequest, vocabsNames, fieldName, selectedVocabsEbtry));
			else
				request.append( fieldName+" STARTSWITH '" + selectedVocabsEbtry + "'");
			
			firstItem = false;
		}
		request.append(")");
		
		return request.toString();
	}
	
	public String formatOthersVocabularyEntriesSearch(PortletRequest portletRequest, List vocabsNames, String fieldName,
			String selectedEntry) throws Exception {

		/* Récupération du niveau de vocabulaire */
		int selectedLevel = selectedEntry.split("/").length;

		VocabularyEntry vocabEntry = getVocabularyEntry((NuxeoController) portletRequest.getAttribute("ctx"), vocabsNames);

		int levelIndex = 0;
		VocabularyEntry lastVocab = vocabEntry;
		/*
		 * On prends la dernière liste d'entrées soumises par le formulaire du
		 * selector
		 */
		String[] entries = selectedEntry.split("/");
		
		/* Récupération du dernier vocabulaire "sélectionné */
		while (levelIndex < selectedLevel) {

			String entry = entries[levelIndex];
			if (!VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equals(entry))
				lastVocab = lastVocab.getChild(entry);

			levelIndex++;
		}
		
		Collection<VocabularyEntry> vocabsEntries = lastVocab.getChildren().values();
		StringBuffer clause = new StringBuffer();
		
		if(vocabsEntries != null && vocabsEntries.size() > 0){

			clause.append("( NOT (");
	
			String selectedValuesBeforeOthers = StringUtils.substringBeforeLast(selectedEntry, "/");
			if(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(selectedValuesBeforeOthers))
				selectedValuesBeforeOthers = "";
			else
				selectedValuesBeforeOthers += "/";
			
			boolean firstItem = true;
			for (VocabularyEntry displayedEntry : vocabsEntries) {
				
				String entry = displayedEntry.getId();
	
				if (!firstItem)
					clause.append(" OR ");
	
				clause.append(fieldName);
				clause.append(" STARTSWITH '");
				clause.append(selectedValuesBeforeOthers);
				clause.append(entry);
				clause.append("' ");
	
				firstItem = false;
			}
	
			clause.append(") )");
		}

		return clause.toString();
	}

	private VocabularyEntry getVocabularyEntry(NuxeoController nuxeoCtrl, List vocabsNames) throws Exception {
		/* TODO: à externaliser, mutualiser */
		
		nuxeoCtrl.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
		nuxeoCtrl.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
		nuxeoCtrl.setCacheTimeOut(3600 * 1000L);
		//ctx.setAsynchronousUpdates(true);

		/* identifierVocabsNames doit être sous la forme voca1;voca2;voca3 */
		StringBuffer identifierVocabsNames = new StringBuffer();
		for(Object vocabName : vocabsNames){
			identifierVocabsNames.append(vocabName);
			identifierVocabsNames.append(";");
		}
		
		VocabularyIdentifier vocabIdentifier = new VocabularyIdentifier(identifierVocabsNames.toString(), identifierVocabsNames.toString());

		VocabularyEntry vocabEntry = (VocabularyEntry) nuxeoCtrl.executeNuxeoCommand(new VocabularyLoaderCommand(
				vocabIdentifier));
		return vocabEntry;
	}
	
	public String formatDateSearch( String fieldName, List<String> searchValue)
	{
		String delimFrontend = "/";
		String delimBackend = "-";
		String jj = "";
		String mm = "";
		String aaaa = "";
		
		StringTokenizer st = new StringTokenizer(searchValue.get(0), delimFrontend);
		jj = st.nextToken();
		mm = st.nextToken();
		aaaa = st.nextToken();
		String paramFrom = aaaa + delimBackend + mm + delimBackend + jj;
		
		st = new StringTokenizer(searchValue.get(1), delimFrontend);
		jj = st.nextToken();
		mm = st.nextToken();
		aaaa = st.nextToken();
		String paramTo = aaaa + delimBackend + mm + delimBackend + jj;

		StringBuffer request = new StringBuffer();
		request.append("(");
		request.append( fieldName + " BETWEEN DATE '" + paramFrom + "' AND DATE '"+ paramTo +"' ");
		request.append(")");
		
		return request.toString();
	}

}
