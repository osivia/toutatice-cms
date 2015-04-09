// File browser and menu drag'n'drop
// Required JQuery UI with interactions modules

$JQry(function() {
	// File browser draggable items
	$JQry(".file-browser .draggable").draggable({
		addClasses : false,
		connectToFancytree : true,
		cursorAt : {top : 0, left : 0},
		helper : function(event) {
			var $target = $JQry(event.target);
			
			var $draggable = $target.closest(".draggable");
			var sourceId = $draggable.data("id");
			var sourceType = $draggable.data("type");
			
			var $draggableChildren = $draggable.children();
			
			var $iconCell = $draggableChildren.first();
			var $icon = $JQry(document.createElement("div"));
			$icon.addClass("text-gray-dark");
			$icon.append($iconCell.find("i").clone());
			
			var $textCell = $iconCell.next();
			var $textCellContent = $textCell.find(".content");
			var $text = $JQry(document.createElement("div")).text($textCellContent.text());
			
			// Helper
			var $helper = $JQry(document.createElement("div"));
			$helper.addClass("draggable-helper bg-primary clearfix");
			$helper.data("id", sourceId);
			$helper.data("type", sourceType);
			$helper.append($icon);
			$helper.append($text);
			return $helper;
		},
		opacity : 0.8,
		revert : "invalid",
		revertDuration : 200
	});

	
	// File browser droppable items
	$JQry(".file-browser .droppable").droppable({
		accept : function(draggable) {
			// Source
			var $source = $JQry(draggable);
			var sourceType = $source.data("type");

			// Target
			var $target = $JQry(this);
			var targetAcceptedTypes = $target.data("acceptedtypes").split(",");
			
			var acceptedType = false;
			jQuery.each(targetAcceptedTypes, function(index, type) {
				if (sourceType === type) {
					acceptedType = true;
				}
			});
			
			return acceptedType;
		},
		addClasses : false,
		drop : function(event, ui) {
			// Source
			var $source = $JQry(ui.helper.context);
			var sourceId = $source.data("id");

			// Target
			var $target = $JQry(event.target);
			var targetId = $target.data("id");

			// Action URL
			var $root = $JQry(this).closest(".file-browser");
			var url = $root.data("dropurl");

			// AJAX call
			var container = null;
			var options = {
				requestHeaders : [ "ajax", "true", "bilto" ],
				method : "post",
				postBody : "sourceId=" + sourceId + "&targetId=" + targetId,
				onSuccess : function(t) {
					onAjaxSuccess(t, null);
				}
			};
			var eventToStop = null;
			var callerId = null;
			directAjaxCall(container, options, url, eventToStop, callerId);
		},
		hoverClass : "droppable-hover bg-info",
		tolerance : "pointer"
	});
	
	
	// Fancytree lazy, with drag & drop
	$JQry(".fancytree.fancytree-lazy").fancytree({
		activeVisible : true,
		extensions : ["dnd", "glyph"],
		tabbable : false,
		titlesTabbable : true,
		toggleEffect : false,

		dnd : {
			autoExpandMS : 500,
			preventRecursiveMoves : true,
			preventVoidMoves : true,
			
			draggable : {
				scroll : false
			},
			
			dragEnter : function(targetNode, data) {
				// Only drop on folders
				if (!targetNode.folder) {
					return false;
				}
				
				// Prevent drop on active node
				if (targetNode.data.current) {
					return false;
				}

				// Target node must accept at least one sub-type
				if (targetNode.data.acceptedtypes == undefined) {
					return false;
				}

				
				// Source
				var $source = $JQry(data.draggable.helper.context);
				var sourceType = $source.data("type");

				// Target
				var targetAcceptedTypes = targetNode.data.acceptedtypes.split(",");
				
				var acceptedType = false;
				jQuery.each(targetAcceptedTypes, function(index, type) {
					if (sourceType === type) {
						acceptedType = "over";
					}
				});
				
				return acceptedType;
			},
			
			dragDrop : function(targetNode, data) {
				// Source
				var $source = $JQry(data.draggable.helper.context);
				var sourceId = $source.data("id");

				// Target
				var targetId = targetNode.data.id;

				// Action URL
				var $root = targetNode.tree.$div.closest(".menu");
				var url = $root.data("dropurl");
				
				// AJAX call
				var container = null;
				var options = {
					requestHeaders : [ "ajax", "true", "bilto" ],
					method : "post",
					postBody : "sourceId=" + sourceId + "&targetId=" + targetId,
					onSuccess : function(t) {
						onAjaxSuccess(t, null);
					}
				};
				var eventToStop = null;
				var callerId = null;
				directAjaxCall(container, options, url, eventToStop, callerId);
			}
		},
		
		glyph : {
			map : {
				doc : "glyphicons glyphicons-file",
				docOpen: "glyphicons glyphicons-file",
				checkbox: "halflings halflings-unchecked",
				checkboxSelected: "halflings halflings-check",
				checkboxUnknown: "halflings halflings-share",
				error: "halflings halflings-exclamation-sign",
				expanderClosed: "glyphicons glyphicons-collapse text-primary-hover",
				expanderLazy: "glyphicons glyphicons-collapse text-primary-hover",
				expanderOpen: "glyphicons glyphicons-expand text-primary-hover",
				folder: "glyphicons glyphicons-folder-closed",
				folderOpen: "glyphicons glyphicons-folder-open",
				loading: "halflings halflings-hourglass text-info"
			}
		},
		
		activate : function(event, data) {
			var node = data.node;
			if (node.data.href) {
				if (node.data.target) {
					window.open(node.data.href, node.data.target);
				} else {
					window.location.href = node.data.href;
				}
			}
		},
		
		lazyLoad : function(event, data) {
			var node = data.node;
			
			// Lazy loading URL
			var $root = node.tree.$div.closest(".menu");
			var url = $root.data("lazyloadingurl");
			
			data.result = {
				url : url,
				data : {
					"action" : "lazyLoading",
					"path" : node.data.path
				},
				cache : false
			};
		}
	});
	
	
	// File Upload
	$JQry(".file-browser .file-upload").fileupload({
		autoUpload : false,
		dataType : "json",
		dropZone : ".drop-zone",
		
		add : function(e, data) {
			var $this = $JQry(this);
			var $root = $this.closest(".file-browser");
			var $panel = $root.find(".file-upload .panel");
			var $list = $root.find(".file-upload .file-upload-list");
			
			var $cancelGlyph = $JQry(document.createElement("i")).addClass("glyphicons halflings ban-circle");
			var $cancelText = $JQry(document.createElement("span")).text($panel.find(".cancel").first().text());
			

			// Display panel
			$panel.removeClass("hidden");
			
			// Context
			data.context = $JQry(document.createElement("li"))
			data.context.addClass("template-upload list-group-item")
			data.context.appendTo($list);
			
			$JQry.each(data.files, function(index, file) {
				// List item
				var $listItem = $JQry(document.createElement("div"))
				$listItem.addClass("clearfix");
				$listItem.appendTo(data.context);
				
				// Upload button
				var $uploadButton =  $JQry(document.createElement("button"))
				$uploadButton.addClass("start hidden");
				$uploadButton.click(function() {
					data.submit();
				});
				$uploadButton.appendTo($listItem);
				
				// Cancel button
				var $cancelButton = $JQry(document.createElement("button"));
				$cancelButton.addClass("cancel btn btn-default pull-right");
				$cancelButton.append($cancelGlyph);
				$cancelButton.append($cancelText);
				$cancelButton.appendTo($listItem);
				
				// Item content
				var $content = $JQry(document.createElement("p"));
				$content.text(file.name);
				$content.appendTo($listItem);
			});
		},

		stop : function(e, data) {
			var $this = $JQry(this);
			var $root = $this.closest(".file-browser");
			
			// Hide panel
			var $panel = $root.find(".file-upload .panel");
			$panel.addClass("hidden");
			
			// Refresh
			var url = $root.data("refreshurl");
			updatePortletContent(this, url);
		},

		progressall : function(e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10) + "%";
			$JQry(".file-browser .progress-bar").css("width", progress);
		}
	});

	$JQry(document).bind("dragover", function(e) {
		e.preventDefault();
		
		var $dropZone = $JQry(".drop-zone");
		var $foundDropZone;
		var found = false;
		var node = e.target;
		var $node;
		var timeout = window.dropZoneTimeout;
		
		if (!timeout) {
			$dropZone.addClass("in");
		} else {
			clearTimeout(timeout);
		}

		do {
			$node = $JQry(node);
			if ($node.hasClass("drop-zone")) {
				found = true;
				$foundDropZone = $node;
				break;
			}
			node = node.parentNode;
		} while (node != null);
		
		$dropZone.removeClass("hover bg-info");
		$dropZone.find(".inbox").removeClass("inbox_in");
		
		if (found) {
			$foundDropZone.addClass("hover bg-info");
			$foundDropZone.find(".inbox").addClass("inbox_in");
		}
		
		window.dropZoneTimeout = setTimeout(function() {
			window.dropZoneTimeout = null;
			$dropZone.removeClass("in hover bg-info");
		}, 1000);
	});

	$JQry(document).bind("drop", function(e) {
		e.preventDefault();
		
		var $dropZone = $JQry(".drop-zone");
		$dropZone.removeClass("in hover bg-info");
	});
	
});
