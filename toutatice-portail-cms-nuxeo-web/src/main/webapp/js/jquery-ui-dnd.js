// File browser and menu drag'n'drop
// Required JQuery UI with interactions modules

$JQry(function() {
	// File browser draggable items
	$JQry(".file-browser .draggable").draggable({
		addClasses : false,
		connectToDynatree: true,
		cursor : "crosshair",
		cursorAt : {top : -2, left : -2},
		helper : "clone",
		opacity : 0.8,
		revert : function(dropped) {
			// Return `true` to let the helper slide back.
			if (typeof dropped === "boolean") {
				// dropped == true, when dropped over a simple, valid droppable target.
				// false, when dropped outside a drop target.
				return !dropped;
			}
			
			// Drop comes from another tree. Default behavior is to assume a valid drop, since we are over a drop-target.
			// Therefore we have to make an extra check, if the target node was rejected by a Dynatree callback.
			var helper = $JQry.ui.ddmanager && $JQry.ui.ddmanager.current && $JQry.ui.ddmanager.current.helper;
			var isRejected = helper && helper.hasClass("dynatree-drop-reject");
			return isRejected;
		},
		revertDuration : 200
	});

	
	// File browser droppable items
	$JQry(".file-browser .droppable").droppable({
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
			var options = {
				requestHeaders : [ "ajax", "true", "bilto" ],
				method : "post",
				postBody : "sourceId=" + sourceId + "&targetId=" + targetId,
				onSuccess : function(t) {
					onAjaxSuccess(t, null);
				}
			};
			new Ajax.Request(url, options);
		},
		hoverClass : "droppable-hover",
		tolerance : "pointer"
	});
	
	
	// Menu Dynatree
	$JQry(".nuxeo-publish-navigation .dynatree").dynatree({
		activeVisible : true,
		clickFolderMode : 1,

		dnd : {
			autoExpandMS : 500,
			preventVoidMoves : true,

			onDragEnter : function(node, sourceNode) {
				if (!node.data.isFolder) {
					return false;
				}
				
				return "over";
			},

			onDrop : function(node, sourceNode, hitMode, ui, draggable) {
				// Source
				var $source = $JQry(draggable.helper.context);
				var sourceId = $source.data("id");

				// Target
				var targetId = node.data.id;

				// Action URL
				var $root = $JQry(node.li).closest(".nuxeo-publish-navigation");
				var url = $root.data("dropurl");
				
				// AJAX call
				var options = {
					requestHeaders : [ "ajax", "true", "bilto" ],
					method : "post",
					postBody : "sourceId=" + sourceId + "&targetId=" + targetId,
					onSuccess : function(t) {
						onAjaxSuccess(t, null);
					}
				};
				new Ajax.Request(url, options);
			}
		},

		onActivate : function(node) {
			if (node.data.target) {
				window.open(node.data.href, node.data.target);
			} else {
				window.location.href = node.data.href;
			}
		},

		onLazyRead : function(node) {
			// Lazy loading URL
			var $root = $JQry(node.li).closest(".nuxeo-publish-navigation");
			var url = $root.data("lazyloadingurl");
			
			node.appendAjax({
				url : url,
				data : {
					"action" : "lazyLoading",
					"path" : node.data.path
				},
			})
		},

		debugLevel : 0
	});
	
	
	// File Upload
	$JQry(".file-browser .file-upload").fileupload({
		autoUpload : false,
		dataType : "json",
		
		add : function(e, data) {
			var $this = $JQry(this);
			var $root = $this.closest(".file-browser");
			var $list = $root.find(".file-upload-list");

			data.context = $JQry("<li />").addClass("list-group-item").appendTo($list);
			
			$JQry.each(data.files, function(index, file) {
				var $listItem = $JQry("<div />").addClass("clearfix");
				
				var $button =  $JQry("<button />").addClass("btn btn-primary pull-right").text("Upload");
				$button.click(function() {
					data.context = $JQry("<p />").text("Uploading...").replaceAll($JQry(this));
					data.submit();
				});
				$button.appendTo($listItem);
				
				$JQry("<p />").text(file.name).appendTo($listItem);
				
				$listItem.appendTo(data.context);
			});
		},
		
		submit : function(e, data) {
			console.log("submit");
			
			return true;
		},
		
		send : function(e, data) {
			console.log("send");
			
			return true;
		},
		
		done : function(e, data) {
			console.log("done");
			
			data.context.text = "Upload finished.";
		},
		
		progressall : function(e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10) + "%";
			$JQry(".file-browser .progress-bar").css("width", progress);
		}
	});

});
