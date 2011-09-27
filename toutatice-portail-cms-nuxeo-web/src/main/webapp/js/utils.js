

function refreshOnVocabularyChange( selectText, url) {
    
    url = url.replace( "SELECTED_VALUE", selectText.options[selectText.selectedIndex].value);
    
    updatePortletContent( selectText, url);
     
    return false;

}
