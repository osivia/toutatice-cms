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
	
	public String formatVocabularySearch(String fieldName, List<String> selectedVocabsEntries) throws Exception{
		

		
		StringBuffer clause = new StringBuffer();		
		clause.append("(");
		
		boolean firstItem = true;
		
		for (String selectedVocabsEntry : selectedVocabsEntries) {

			if (!selectedVocabsEntry.contains(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE)) {

				if (!firstItem)
					clause.append(" OR ");

				clause.append(fieldName + " STARTSWITH '" + selectedVocabsEntry + "'");

				firstItem = false;
			}
		}
		clause.append(")");

			
		return clause.toString();
	}
	
	public String formatOthersVocabularyEntriesSearch(PortletRequest portletRequest, List vocabsNames,
			String fieldName, List<String> selectedVocabsEntries) throws Exception {
		
		StringBuffer clause = new StringBuffer();
		

		
		int nbOtherEntries = 0;

		for(String selectedEntry : selectedVocabsEntries) {

			
			if (selectedEntry.contains(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE)) {
				
				if( nbOtherEntries > 0)
					clause.append(" OR ");
				
				nbOtherEntries++;
				
				StringBuffer clauseBeforeOther = new StringBuffer();
				StringBuffer otherClause = new StringBuffer();
				
				String selectedValuesBeforeOthers = StringUtils.substringBeforeLast(selectedEntry, "/");
				if (VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(selectedValuesBeforeOthers))
					selectedValuesBeforeOthers = "";
				else
					selectedValuesBeforeOthers += "/";

				if (StringUtils.isNotEmpty(selectedValuesBeforeOthers)){
					clauseBeforeOther.append(" ( ");
					clauseBeforeOther.append(fieldName);
					clauseBeforeOther.append(" STARTSWITH '");
					clauseBeforeOther.append(selectedValuesBeforeOthers);
					clauseBeforeOther.append("' ");
					clauseBeforeOther.append(") AND ");
				}
				

				/* Récupération du niveau du dernier vocabulaire affiché */
				int selectedLevel = selectedEntry.split("/").length;

				/* Récupération de l'arbre des vocabulaires */
				VocabularyEntry vocabEntry = getVocabularyEntry((NuxeoController) portletRequest.getAttribute("ctx"),
						vocabsNames);

				/*
				 * Récupération du dernier vocabulaire sélectionné avant
				 * l'entrée "Autres"
				 */
				int levelIndex = 0;
				VocabularyEntry lastVocab = vocabEntry;

				String[] entries = selectedEntry.split("/");

				while (levelIndex < selectedLevel) {

					String entry = entries[levelIndex];
					if (!VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equals(entry)) {
						lastVocab = lastVocab.getChild(entry);
					}

					levelIndex++;
				}


				Collection<VocabularyEntry> vocabsEntries = lastVocab.getChildren().values();

				if (vocabsEntries != null && vocabsEntries.size() > 0) {

					otherClause.append(" ( NOT (");

					boolean firstItem = true;
					for (VocabularyEntry displayedEntry : vocabsEntries) {

						String entry = displayedEntry.getId();

						if (!firstItem) 
							otherClause.append(" OR ");

						otherClause.append(fieldName);
						otherClause.append(" STARTSWITH '");
						otherClause.append(selectedValuesBeforeOthers);
						otherClause.append(entry);
						otherClause.append("' ");

						firstItem = false;
					}

					otherClause.append(" ) ) ");

				}
				
				clause.append(clauseBeforeOther.toString());
				clause.append(otherClause.toString());


				
			}
		}	
		
		
		StringBuffer otherClause = new StringBuffer();
		
		 if( nbOtherEntries > 0)	{
		
			 otherClause.append("(");
		
		/* Les documents Nuxeo dont le champ fieldName n'est pas renseigné
		 * ou existant ne doivent pas être retournés
		 */
	//		 otherClause.append("(");
	//		 otherClause.append(fieldName);
	//		 otherClause.append(" LIKE '%%') AND (");
		
			 otherClause.append(clause.toString());
			 otherClause.append(")");
		 }
		
		
		
		String resultClause = formatVocabularySearch( fieldName, selectedVocabsEntries);
		
		if( resultClause.equals("()"))	
			resultClause = "";
		
	
		if( nbOtherEntries > 0)	{
			if( resultClause.length() > 0)
				resultClause = "(" + resultClause + " OR " + otherClause.toString() + ")";
			else
				resultClause = otherClause.toString();
		}
		

		return resultClause;
	}
	
	/* TODO: à externaliser dans VocabularyHelper */
	private VocabularyEntry getVocabularyEntry(NuxeoController nuxeoCtrl, List vocabsNames) throws Exception {
		
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
