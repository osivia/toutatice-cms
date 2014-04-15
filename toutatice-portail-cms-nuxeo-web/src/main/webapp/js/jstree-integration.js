
var $JQry = jQuery.noConflict();

function jstreeSearch(id, request) {
	$JQry("#" + id).jstree("search", request);
}


