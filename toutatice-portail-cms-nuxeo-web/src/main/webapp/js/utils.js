

function refreshOnVocabularyChange( selectText, url) {
	
	// Encodage encodeURIComponent pour les '+' dans libelles
    
    url = url.replace( "SELECTED_VALUE", encodeURIComponent(selectText.options[selectText.selectedIndex].value));
    
    
    updatePortletContent( selectText, url);
     
    return false;

}



function toggleDiv(id1, id2) {

	var el1 = document.getElementById(id1);
	var el2 = document.getElementById(id2);
	
	el1.style.visibility = "hidden";
	el2.style.visibility = "visible";

} 


function showDiv(id1) {

	var el1 = document.getElementById(id1);

	

	el1.style.visibility = "visible";

} 
