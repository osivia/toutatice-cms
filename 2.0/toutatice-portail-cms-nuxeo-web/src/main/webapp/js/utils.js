

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
   
     if (200 == response.status){
    	 var container = $(idToRefresh);
    	 var content = response.responseText;
    	 
    	 // Jboss connexion
    	 if( response.responseText.search( "j_security_check") == -1)
    		 container.update(content);
    	 else
       		 container.update("");
   		 
     }

     
	}
}

function failureActions(response){
}

//Modif-COMMENTS-begin
function closeFancyBox(){
	parent.jQuery.fancybox.close();
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

var $JQry = jQuery.noConflict();
$JQry(document).ready(function() {
		$JQry(".fancybox_comment").fancybox({
	 		'height': 500,	 
	 		'beforeClose'	:	function(){
				var errorElement = document.getElementById('errorAddCom');
				errorElement.innerHTML = "";
				var errorElement = document.getElementById('errorAddChildCom');
				errorElement.innerHTML = ""
			}
		});
	});
//Modif-COMMENTS-end