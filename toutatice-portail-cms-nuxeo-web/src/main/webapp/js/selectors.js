/**
 * Refresh on vocabulary change.
 * 
 * @param selectText select text
 * @param url URL
 */
function refreshOnVocabularyChange(selectText, url) {
    url = url.replace("SELECTED_VALUE", encodeURIComponent(selectText.options[selectText.selectedIndex].value));
    updatePortletContent(selectText, url);
}
