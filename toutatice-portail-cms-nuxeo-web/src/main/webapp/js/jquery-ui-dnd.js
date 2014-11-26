var $JQry = jQuery.noConflict();


// File browser and menu drag'n'drop
// Required JQuery UI with interactions modules

$JQry(function($) {
	// File browser draggable items
	$(".file-browser .draggable").draggable({
		addClasses: false,
		cancel: ".draggable-cancel",
		cursor: "crosshair",
		drag: fileBrowserDrag,
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
		over: menuOver,
		scope : "file-browser-item",
		tolerance: "pointer"
	});
	
});

function fileBrowserDrag(event, ui) {
	
}

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

	// Non-AJAX call
	window.location.href = url + "&sourceId=" + sourceId + "&targetId=" + targetId;
	
//	// AJAX call
//	var options = {
//		requestHeaders : [ "ajax", "true", "bilto" ],
//		method : "post",
//		postBody : "sourceId=" + sourceId + "&targetId=" + targetId,
//		onSuccess : function(t) {
//			onAjaxSuccess(t, null);
//		},
//		on404 : function(t) {
//			alert("Error 404: location " + t.statusText + " was not found.");
//		},
//		onFailure : function(t) {
//			alert("Error " + t.status + " -- " + t.statusText);
//		},
//		onLoading : function(t) {}
//	};
//	new Ajax.Request(url, options);
}

function menuOver(event, ui) {
	// Target
	var $target = $JQry(event.target);
	
	// Button glyphicon
	var $button = $target.children(".btn");
	var $glyph = $button.find(".glyphicons");
	$glyph.removeClass("chevron-right text-muted");
	$glyph.addClass("chevron-down");
	
	// Children
	var $children = $target.closest("li").children("ul");
	$children.removeClass("hidden");
}
