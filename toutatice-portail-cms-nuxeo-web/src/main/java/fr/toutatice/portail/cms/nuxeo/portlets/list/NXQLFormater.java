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
package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.selectors.DateSelectorPortlet;
import fr.toutatice.portail.cms.nuxeo.portlets.selectors.VocabSelectorPortlet;

/**
 * NXQL formatter.
 */
public class NXQLFormater {

    /** Frontend date pattern. */
    private static final String FRONTEND_DATE_PATTERN = "dd/MM/yyyy";
    /** Backend date pattern. */
    private static final String BACKEND_DATE_PATTERN = "yyyy-MM-dd";


    /**
     * Default constructor.
     */
    public NXQLFormater() {
        super();
    }


    /**
     * Format text search.
     *
     * @param fieldName field name
     * @param searchValues search values
     * @return formatted text search
     */
    public String formatTextSearch(String fieldName, List<String> searchValues) {
        StringBuilder request = new StringBuilder();
        request.append("(");
        
        boolean firstItem = true;
        for (String searchWord : searchValues) {
        	
        	// Remove accents in search query
        	searchWord = Normalizer.normalize(searchWord, Normalizer.Form.NFD);
        	searchWord = searchWord.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        	// Remove special chars
        	searchWord = searchWord.replaceAll("[^A-Za-z0-9 ]", " ");
        	
        	String[] words = searchWord.split(" ");
        	
        	for (String word : words) {
        		
                if (firstItem) {
                    firstItem = false;
                } else {
                    request.append(" AND ");
                }
        		
                request.append(fieldName);
                request.append(" ILIKE '%");
                request.append(word);
                request.append("%'");
        	}


        }

        request.append(")");

        return request.toString();
    }


    /**
     * Format vocabulary search.
     *
     * @param fieldName field name
     * @param selectedVocabsEntries selected vocabulary entries
     * @return formatted vocabulary search
     */
    public String formatVocabularySearch(String fieldName, List<String> selectedVocabsEntries) {
        StringBuilder clause = new StringBuilder();
        clause.append("(");

        boolean firstItem = true;
        for (String selectedVocabsEntry : selectedVocabsEntries) {
            if (!selectedVocabsEntry.contains(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE)) {
                if (firstItem) {
                    firstItem = false;
                } else {
                    clause.append(" OR ");
                }
                clause.append(fieldName);
                clause.append(" STARTSWITH '");
                clause.append(StringUtils.replace(selectedVocabsEntry, "'", "\\'"));
                clause.append("'");
            }
        }

        clause.append(")");

        return clause.toString();
    }


    /**
     * Format other vocabulary entries search.
     *
     * @param portletRequest portlet request
     * @param vocabsNames vocabulary names
     * @param fieldName field name
     * @param selectedVocabsEntries selected vocabulary entries
     * @return formatted other vocabulary entries search
     * @throws Exception
     */
    public String formatOthersVocabularyEntriesSearch(PortletRequest portletRequest, List<?> vocabsNames, String fieldName, List<String> selectedVocabsEntries)
            throws Exception {
        StringBuilder clause = new StringBuilder();

        int nbOtherEntries = 0;
        for (String selectedEntry : selectedVocabsEntries) {
            if (selectedEntry.contains(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE)) {
                if (nbOtherEntries > 0) {
                    clause.append(" OR ");
                }
                nbOtherEntries++;
                StringBuilder clauseBeforeOther = new StringBuilder();
                StringBuilder otherClause = new StringBuilder();

                String selectedValuesBeforeOthers = StringUtils.substringBeforeLast(selectedEntry, "/");
                if (VocabSelectorPortlet.OTHER_ENTRIES_CHOICE.equalsIgnoreCase(selectedValuesBeforeOthers)) {
                    selectedValuesBeforeOthers = "";
                } else {
                    selectedValuesBeforeOthers += "/";
                }

                if (StringUtils.isNotEmpty(selectedValuesBeforeOthers)) {
                    clauseBeforeOther.append(" ( ");
                    clauseBeforeOther.append(fieldName);
                    clauseBeforeOther.append(" STARTSWITH '");
                    clauseBeforeOther.append(StringUtils.replace(selectedValuesBeforeOthers, "'", "\\'"));
                    clauseBeforeOther.append("' ");
                    clauseBeforeOther.append(") AND ");
                }


                // Récupération du niveau du dernier vocabulaire affiché
                int selectedLevel = selectedEntry.split("/").length;

                // Récupération de l'arbre des vocabulaires
                List<String> vocabNames = new ArrayList<String>();
                for (Object vocab : vocabsNames) {
                    vocabNames.add(vocab.toString());
                }


                NuxeoController nuxeoController = (NuxeoController) portletRequest.getAttribute("nuxeoController");
                VocabularyEntry vocabEntry = VocabularyHelper.getVocabularyEntry(nuxeoController, vocabNames);


                // Récupération du dernier vocabulaire sélectionné avant l'entrée "Autres"
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
                if ((vocabsEntries != null) && (vocabsEntries.size() > 0)) {
                    otherClause.append(" ( NOT (");

                    boolean firstItem = true;
                    for (VocabularyEntry displayedEntry : vocabsEntries) {
                        String entry = displayedEntry.getId();

                        if (!firstItem) {
                            otherClause.append(" OR ");
                        }

                        otherClause.append(fieldName);
                        otherClause.append(" STARTSWITH '");
                        otherClause.append(StringUtils.replace(selectedValuesBeforeOthers, "'", "\\'"));
                        otherClause.append(StringUtils.replace(entry, "'", "\\'"));
                        otherClause.append("' ");

                        firstItem = false;
                    }

                    otherClause.append(" ) ) ");
                }

                clause.append(clauseBeforeOther.toString());
                clause.append(otherClause.toString());
            }
        }

        StringBuilder otherClause = new StringBuilder();
        if (nbOtherEntries > 0) {
            otherClause.append("(");

            // Les documents Nuxeo dont le champ fieldName n'est pas renseigné ou existant ne doivent pas être retournés
            // otherClause.append("(");
            // otherClause.append(fieldName);
            // otherClause.append(" LIKE '%%') AND (");

            otherClause.append(clause.toString());
            otherClause.append(")");
        }

        String resultClause = this.formatVocabularySearch(fieldName, selectedVocabsEntries);
        if (resultClause.equals("()")) {
            resultClause = "";
        }

        if (nbOtherEntries > 0) {
            if (resultClause.length() > 0) {
                resultClause = "(" + resultClause + " OR " + otherClause.toString() + ")";
            } else {
                resultClause = otherClause.toString();
            }
        }

        return resultClause;
    }


    /**
     * Format date search.
     *
     * @param fieldName field name
     * @param searchValue search value
     * @return formatted date search
     */
    public String formatDateSearch(String fieldName, List<String> searchValue) {
        StringBuilder request = new StringBuilder();

        if ((searchValue != null) && (searchValue.size() > 0)) {
            request.append("(");

            int index = 0;
            for (String datesInterval : searchValue) {
                String[] interval = datesInterval.split(DateSelectorPortlet.DATES_SEPARATOR);
                try {
                    // Dates
                    String[] frontPattern = new String[]{FRONTEND_DATE_PATTERN};
                    Date beginDate = DateUtils.parseDate(interval[0], frontPattern);
                    Date endDate = DateUtils.parseDate(interval[1], frontPattern);
                    // Add one day to end date
                    endDate = DateUtils.addDays(endDate, 1);

                    // Backend dates format
                    String from = DateFormatUtils.format(beginDate, BACKEND_DATE_PATTERN);
                    String to = DateFormatUtils.format(endDate, BACKEND_DATE_PATTERN);

                    // Request generation
                    if (index > 0) {
                        request.append(" OR ");
                    }
                    request.append("(");
                    request.append(fieldName);
                    request.append(" BETWEEN DATE '");
                    request.append(StringUtils.replace(from, "'", "\\'"));
                    request.append("' AND DATE '");
                    request.append(StringUtils.replace(to, "'", "\\'"));
                    request.append("')");
                } catch (ParseException e) {
                    continue;
                }
                index++;
            }
            request.append(")");
        }

        return request.toString();
    }


    /**
     * Format advanced search.
     *
     * @param searchValues search values
     * @return formatted advanced search
     */
    public String formatAdvancedSearch(List<String> searchValues) {
        StringBuilder builder = new StringBuilder();

        Iterator<String> itSearchValues = searchValues.iterator();
        while (itSearchValues.hasNext()) {
            builder.append(formatAdvancedSearch(itSearchValues.next()));
            // Multi valued selector
            if (itSearchValues.hasNext()) {
                builder.append(" AND ");
            }
        }

        return builder.toString();
    }

    /**
     * Format advanced search.
     * 
     * @param keyWords key words
     * @return formatted advanced search
     */
    public String formatAdvancedSearch(String keyWords) {
        StringBuilder builder = new StringBuilder();

        String[] keyWds = StringUtils.split(keyWords);
        Iterator<String> itKeyWords = Arrays.asList(keyWds).iterator();

        while (itKeyWords.hasNext()) {
            String keyWord = StringUtils.replace(itKeyWords.next(), "'", "\\'");

            builder.append("(ecm:fulltext = '");
            builder.append(keyWord);
            builder.append("' OR dc:title ILIKE '");
            builder.append(keyWord);
            builder.append("%')");

            if (itKeyWords.hasNext()) {
                builder.append(" AND ");
            }
        }

        return builder.toString();
    }

}
