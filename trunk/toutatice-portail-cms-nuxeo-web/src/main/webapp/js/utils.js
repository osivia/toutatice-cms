

function refreshOnVocabularyChange( selectText, url) {
	
	// Encodage encodeURIComponent pour les '+' dans libelles
    
    url = url.replace( "SELECTED_VALUE", encodeURIComponent(selectText.options[selectText.selectedIndex].value));
    
    
    updatePortletContent( selectText, url);
     
    return false;

}
