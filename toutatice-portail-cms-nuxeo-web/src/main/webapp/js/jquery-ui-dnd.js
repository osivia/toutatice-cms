var $JQry = jQuery.noConflict();

// File browser and menu drag'n'drop
// Required JQuery UI with interactions modules
$JQry(function($) {
	// File browser draggable items
	$(".file-browser .draggable").draggable({
		addClasses: false,
		cancel: ".draggable-cancel",
		cursor: "crosshair",
		helper: "clone",
		opacity: 0.8,
		revert: "invalid",
		revertDuration : 200,
		scope: "file-browser-item"
	});
	
	// Menu droppable items
	$(".nuxeo-publish-navigation .droppable").droppable({
		activeClass : "droppable-active",
		addClasses : false,
		drop: menuDrop,
		greedy : true,
		hoverClass : "droppable-hover",
		scope : "file-browser-item",
		tolerance: "pointer"
	});
	
});


function menuDrop(event, ui) {
	// Source
	var $source = $JQry(ui.draggable.context);
	var sourceId = $source.data("id");
	
	// Target
	var $target = $JQry(event.target);
	var targetId = $target.data("id");
	
	// Action URL
	var $this = $JQry(this);
	var $root = $this.closest(".nuxeo-publish-navigation");
	var url = $root.data("dropurl");

	var options = {
		requestHeaders : [ "ajax", "true", "bilto" ],
		method : "post",
		postBody : "sourceId=" + sourceId + "&targetId=" + targetId,
		onSuccess : function(t) {
			onAjaxSuccess(t, null);
		},
		on404 : function(t) {
			alert("Error 404: location " + t.statusText + " was not found.");
		},
		onFailure : function(t) {
			alert("Error " + t.status + " -- " + t.statusText);
		},
		onLoading : function(t) {}
	};
	new Ajax.Request(url, options);
}
