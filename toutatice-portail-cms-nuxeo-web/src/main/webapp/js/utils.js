var $JQry = jQuery.noConflict();


// Publication menu
$JQry(document).ready(function($) {
	$(".nuxeo-publish-navigation .hidden-script").addClass("hidden").removeClass("hidden-script");
	
	$(".nuxeo-publish-navigation li button").click(function() {
		$this = $(this);
		
		// Toggle button glyphicon
		$glyph = $this.find(".glyphicons");
		$glyph.toggleClass("chevron-down chevron-right text-muted");
		
		// Toggle item children display
		$children = $this.closest("li").children("ul");
		$children.toggleClass("hidden");
	});
});






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

/* 2.1 Gestion des workspaces */




function getFileActions(url, idToRefresh) {
	var el1 = document.getElementById(idToRefresh);

	if (el1.style.visibility == "visible") {
		el1.style.visibility = "hidden";
	} else {
		el1.style.visibility = "visible";

		new Ajax.Request(url, {
			method : 'get',
			onSuccess : successActions(idToRefresh),
			onFailure : failureActions
		});
	}
}


function successActions(idToRefresh){
	return function(response){
	
	 var ok = false;
	 
	 var container = $(idToRefresh);
   
     if (200 == response.status){
    	 
    	 var content = response.responseText;
    	 
    	 // Exclude Jboss connexion
    	 if( response.responseText.search( "j_security_check") == -1)	{
    		 container.update(content);
    		 ok = true;
    	 }
  		 
     }
     
     // Default message if response incorrect
     if (ok == false){
     	container.update("Non disponible");
     }
     

     
	}
}

function failureActions(response){
}

function isEmptyField(inputId, errorElementId, msg){
	var isEmpty = true;
	var field = document.getElementById(inputId);
	if(field != null){
		var value = field.value;
		isEmpty = value == null || value == "";
		if(isEmpty){
			var errorElement = document.getElementById(errorElementId);
			errorElement.innerHTML = msg;
		}
	}
	return isEmpty;
}

/* Affichage des actions répondre/supprimer au survol */
function toggleactions(obj, show){ 
	var delete_elem = obj.getElementsByClassName('delete_comment')[0];
	if(show){       
		obj.getElementsByClassName('child_comment')[0].style.display="block"; 
		if(typeof(delete_elem) != "undefined"){
			delete_elem.style.display="block";  
		}
	}else{      
		obj.getElementsByClassName('child_comment')[0].style.display="none";       
		if(typeof(delete_elem) != "undefined"){
			delete_elem.style.display="none";  
		}
	}  
}



function clearText(div) {
    var formulaire = $JQry(div).parents("form");
    var inputs = formulaire.find("input[type='text']");
    for (var i = 0; i < inputs.length; i++) {
    	inputs[i].value = "";
    }
}

