// File browser drag & drop and selection functions
// Required JQuery UI with interactions modules

$JQry(function() {

	var previousIndex = -1;
	
	// Selectable
	$JQry(".file-browser .selectable").selectable({
		cancel: ".sortable-handle, .draggable",
		filter: ".data",
		
		selected: function(event, ui) {
			$JQry(ui.selected).addClass("bg-primary").removeClass("bg-info");
		},
		
		selecting: function(event, ui) {
			var $selecting = $JQry(ui.selecting),
				$seletable = $selecting.closest(".selectable"),
				$selectee = $seletable.find(".data"),
				currentIndex = $selectee.index(ui.selecting);
			
			if (event.shiftKey && previousIndex > -1) {
				$selectee.slice(Math.min(previousIndex, currentIndex), Math.max(previousIndex, currentIndex) + 1).addClass("ui-selected bg-primary");
			} else {
				$selecting.addClass("bg-info");
				previousIndex = currentIndex;
			}
		},
		
		stop: function(event, ui) {
			var $target = $JQry(event.target),
				$browser = $target.closest(".file-browser");
			
			updateSelectableControls($browser);
		},
		
		unselected: function(event, ui) {
			if (!event.shiftKey) {
				$JQry(ui.unselected).removeClass("bg-primary");
			}
		},
		
		unselecting: function(event, ui) {
			$JQry(ui.unselecting).removeClass("bg-primary bg-info");
		}
	});
	
	
	// Affix
	$JQry(".file-browser .file-browser-affix").each(function(index, element) {
		var $element = $JQry(element),
			$affixContainer = $element.closest(".file-browser-affix-container");
			$browser = $affixContainer.closest(".file-browser"),
			$body = $JQry("body");
	
		$element.affix({
			offset: {
				top: function() {
					return Math.round($affixContainer.offset().top) - 55;
				},
				bottom: function() {
					var top = Math.round($affixContainer.offset().top) - 55,
						scrollTop = $JQry(window).scrollTop(),
						bottom = 0;
					
					if (scrollTop > (top + 34)) {
						bottom = Math.round($body.height() - $browser.offset().top - $browser.outerHeight(true)) - 34;
					}
					
					return bottom;
				}
			}
		});
	});
	
	
	// Draggable
	$JQry(".file-browser .draggable").draggable({
		addClasses: false,
		connectToFancytree: true,
		distance: 10,
		
		helper: function(event) {
			var $target = $JQry(event.target),
				$data = $target.closest(".data"),
				direct = ($data.length > 0),
				$selected = (direct ? $data : $target.closest(".selectable").find(".ui-selected")),
				offset = $target.offset(),
				click = {
					top: event.pageY - offset.top,
					left: event.pageX - offset.left
				},
				identifiers = "", types = "", text = "",
				$helper;
			
			
			// Identifiers & types
			$selected.each(function(index, element) {
				if (index > 0) {
					identifiers += ",";
					types += ",";
				}
				identifiers += $JQry(element).data("id");
				types += $JQry(element).data("type");
			});
			
			
			// Helper
			$helper = $JQry(document.createElement("div"));
			$helper.addClass("helper");
			$helper.data({
				identifiers: identifiers,
				types: types
			});
			$helper.css({
				height: 0,
				width: 0
			});
			
			// Panel
			$panel = $JQry(document.createElement("div"));
			$panel.addClass("panel panel-primary");
			if (direct) {
				$panel.css({
					width: $data.siblings(".draggable-shadowbox").width()
				});
			} else {
				$panel.css({
					width: $target.width()
				});
			}
			$panel.animate({
				top: click.top + 1,
				left: click.left + 1,
				width: 300
			}, 300);
			$panel.appendTo($helper);
			
			// Panel body
			$panelBody = $JQry(document.createElement("div"));
			$panelBody.addClass("panel-body bg-primary");
			$panelBody.appendTo($panel);
			
			// Icon
			$icon = $JQry(document.createElement("div"));
			$icon.addClass("document-icon");
			$icon.appendTo($panelBody);
			$iconInner = $JQry(document.createElement("div"));
			$iconInner.appendTo($icon);
			if ($selected.length == 1) {
				$selected.find(".document-icon").find("i").clone().appendTo($iconInner);
			} else {
				$strong = $JQry(document.createElement("strong"));
				$strong.text($selected.length);
				$strong.appendTo($iconInner);
			}
			
			// Text
			$selected.find(".document-icon").siblings().each(function(index, element) {
				if (index > 0) {
					text += ", ";
				}
				text += $JQry(element).text();
			});
			$textContainer = $JQry(document.createElement("div"));
			$textContainer.addClass("text-overflow");
			$textContainer.text(text);
			$textContainer.appendTo($panelBody);

			return $helper;
		},
		
		revert: "invalid",
		revertDuration: 200,
		
		start: function(event, ui) {
			var $target = $JQry(event.target),
				$selectable = $target.closest(".selectable"),
				$selected = $selectable.find(".ui-selected");
			
			$selected.addClass("dragged");
		},
		
		stop: function(event, ui) {
			var $target = $JQry(event.target),
				$selectable = $target.closest(".selectable"),
				$selected = $selectable.find(".ui-selected");
		
			$selected.removeClass("dragged");
		}
	});
	
	
	// Double click
	$JQry(".file-browser .file-browser-lines li").dblclick(function(event) {
		var $target = $JQry(event.target),
			$li = $target.closest("li"),
		    $link = $li.find("a"),
		    url = $link.attr("href");
		
		if (url === undefined) {
			console.log("Double click event failed: URL is undefined.");
		} else {
			window.location.href = $link.attr("href");
		}
	});
	
	
	// Click on draggable
	$JQry(".file-browser .draggable").click(function(event) {
		var $row = $JQry(event.target).closest("li"),
			$selected = $row.find(".ui-selected"),
			$browser = $row.closest(".file-browser");
		
		if (event.ctrlKey) {
			$selected.removeClass("ui-selected bg-primary");
		} else {
			$browser.find(".ui-selected").each(function(index, element) {
				var $element = $JQry(element);
				
				if (!$element.is($selected)) {
					$element.removeClass("ui-selected bg-primary");
				}
			});
		}
		
		updateSelectableControls($browser);
	});
	
	
	// Droppable
	$JQry(".file-browser .droppable").droppable({
		accept: function($draggable) {
			var $droppable = $JQry(this);
				$selectable = $droppable.closest(".selectable"),
				$selected = $selectable.find(".ui-selected"),
				targetAcceptedTypes = $droppable.data("acceptedtypes").split(","),
				accepted = true;
			
			if ($draggable.hasClass("ui-sortable-helper") || $droppable.closest(".ui-selected").hasClass("ui-selected")) {
				// Prevent drop on sortable or selected element
				accepted = false;
			} else {
				$selected.each(function(index, element) {
					var sourceType = $JQry(element).data("type"),
						match = false;
					
					jQuery.each(targetAcceptedTypes, function(index, targetType) {
						if (sourceType === targetType) {
							match = true;
							return false;
						}
					});
					
					if (!match) {
						accepted = false;
						return false;
					}
				});
			}
			
			return accepted;
		},
		
		addClasses: false,
		hoverClass: "droppable-hover bg-info border-info",
		tolerance: "pointer",
		
		drop: function(event, ui) {
			var $browser = $JQry(this).closest(".file-browser"),
				
				// Source
				$source = $JQry(ui.helper.context),
				sourceIdentifiers = $source.data("identifiers"),
				
				// Target
				$target = $JQry(event.target),
				targetId = $target.closest(".data").data("id"),
				
				// AJAX parameters
				container = null,
				options = {
					requestHeaders : [ "ajax", "true", "bilto" ],
					method : "post",
					postBody : "sourceIds=" + sourceIdentifiers + "&targetId=" + targetId,
					onSuccess : function(t) {
						onAjaxSuccess(t, null);
					}
				},
				url = $browser.data("dropurl"),
				eventToStop = null,
				callerId = null;
			
			directAjaxCall(container, options, url, eventToStop, callerId);
		}
	});

	
	// Sortable
	$JQry(".file-browser .sortable").each(function(index, element) {
		var $element = $JQry(element),
			ordered = $element.data("ordered"),
			axis = $element.data("axis"),
			placeholderClasses = $element.data("placeholderclasses");
		
		if (ordered) {
			$element.sortable({
				axis: (axis !== undefined ? axis : false),
				cursor: "move",
				distance: 10,
				forcePlaceholderSize: true,
				handle: ".sortable-handle",
				placeholder: "bg-info" + (placeholderClasses !== undefined ? " " + placeholderClasses : ""),
				tolerance: "pointer",
				
				start: function(event, ui) {
					var $item = $JQry(ui.item),
						$placeholder = $JQry(ui.placeholder);
					
					$placeholder.height($item.height());
				},
				
				update: function(event, ui) {
					var $browser = $JQry(this).closest(".file-browser"),
						$sortable = $browser.find(".sortable");
					
					// Source
					$source = $JQry(ui.item);
					sourceId = $source.find(".data").data("id");
					
					// Target
					if ($sortable.data("alternative")) {
						$target = $source.prev();
					} else {
						$target = $source.next();
					}
					targetId = $target.find(".data").data("id");
						
					// AJAX parameters
					container = null,
					options = {
						requestHeaders : [ "ajax", "true", "bilto" ],
						method : "post",
						postBody : "sourceId=" + sourceId + (targetId !== undefined ? "&targetId=" + targetId : ""),
						onSuccess : function(t) {
							onAjaxSuccess(t, null);
						}
					},
					url = $browser.data("sorturl"),
					eventToStop = null,
					callerId = null;
					
					directAjaxCall(container, options, url, eventToStop, callerId);
				}
			});
		}
	});
	
	
	// File Upload
	$JQry(".file-browser .file-upload").fileupload({
		autoUpload : false,
		dataType : "json",
		dropZone : ".drop-zone",
		
		add : function(e, data) {
			var $this = $JQry(this),
				$browser = $this.closest(".file-browser"),
				$panel = $browser.find(".file-upload .panel"),
				$list = $browser.find(".file-upload .file-upload-list");
			
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
				$listItem = $JQry(document.createElement("div"));
				$listItem.addClass("clearfix");
				$listItem.appendTo(data.context);
				
				// Upload button
				$uploadButton =  $JQry(document.createElement("button"));
				$uploadButton.addClass("start hidden");
				$uploadButton.click(function() {
					data.submit();
				});
				$uploadButton.appendTo($listItem);
				
				// Cancel button
				$cancelButton = $JQry(document.createElement("button"));
				$cancelButton.addClass("cancel btn btn-default pull-right");
				$cancelButton.append($cancelGlyph);
				$cancelButton.append($cancelText);
				$cancelButton.appendTo($listItem);
				
				// Item content
				$content = $JQry(document.createElement("p"));
				$content.text(file.name);
				$content.appendTo($listItem);
			});
		},

		stop : function(e, data) {
			var $this = $JQry(this),
				$browser = $this.closest(".file-browser"),
				$panel = $browser.find(".file-upload .panel"),
				url = $browser.data("refreshurl");
			
			// Hide panel
			$panel.addClass("hidden");
			
			// Refresh
			updatePortletContent(this, url);
		},

		progressall : function(e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10) + "%";
			$JQry(".file-browser .progress-bar").css("width", progress);
		}
	});

	$JQry(document).bind("dragover", function(e) {
		e.preventDefault();
		
		var $dropZone = $JQry(".drop-zone"),
			$foundDropZone,
			found = false,
			node = e.target,
			$node,
			timeout = window.dropZoneTimeout;
		
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


function updateSelectableControls($browser) {
	var $selected = $browser.find(".ui-selected"),
		$toolbar = $browser.find(".btn-toolbar"),
		$single = $toolbar.find(".single-selection"),
		$messageSelection = $toolbar.find(".message-selection"),
		$links = $toolbar.find("a[data-url]"),
		identifiers = "", paths = "", types = "";

	
	// Sortable
	$browser.find(".sortable-handle").addClass("hidden");
	
	
	// Reset links
	$links.each(function(index, element) {
		var $element = $JQry(element),
			url = $element.data("url");
		
		$element.attr("href", url);
	});
	
	if ($selected.length == 0) {
		// No selection
		$toolbar.hide();
	} else if ($selected.length == 1) {
		// Single element selected
		$toolbar.show();
		$single.show();
		$messageSelection.children(".badge").text("1");
		$messageSelection.children(".text").text($messageSelection.data("message-single-selection"));

		
		// Sortable
		$selected.find(".sortable-handle").removeClass("hidden");
		
		
		// Update links with single-selected properties
		$links.each(function(index, element) {
			var $element = $JQry(element),
				url = $element.attr("href");
			
			// Update path
			url = url.replace("_PATH_", $selected.data("path"));
			
			$element.attr("href", url);
		});

		
		// Gallery
		$gallery = $toolbar.find(".gallery");
		$fancybox = $selected.find(".fancybox.thumbnail");
		if ($fancybox.length) {
			$gallery.removeClass("hidden");
		} else {
			$gallery.addClass("hidden");
		}
		
		
		// Download
		$download = $toolbar.find(".download");
		downloadURL = $selected.data("downloadurl");
		if (downloadURL) {
			$download.attr("href", downloadURL);
			$download.removeClass("hidden");
		} else {
			$download.addClass("hidden");
		}
		
		
		// Edit
		$edit = $toolbar.find(".edit");
		if ($selected.data("editable")) {
			$edit.removeClass("disabled");
		} else {
			$edit.addClass("disabled");
		}
	} else {
		// Multiple elements selected
		$toolbar.show();
		$single.hide();
		$messageSelection.children(".badge").text($selected.length);
		$messageSelection.children(".text").text($messageSelection.data("message-multiple-selection"));
	}
	
	// Identifiers, paths and types
	$selected.each(function(index, element) {
		var $element = $JQry(element);
		
		if (index > 0) {
			identifiers += ",";
			paths += ",";
			types += ",";
		}
		identifiers += $element.data("id");
		paths += $element.data("path");
		types += $element.data("type");
	});
	$browser.find("input[name=identifiers]").val(identifiers);

	// Update links with multiple-selected properties
	$links.each(function(index, element) {
		var $element = $JQry(element),
			url = $element.attr("href");
		
		// Update tokens
		url = url.replace("_IDS_", identifiers);
		url = url.replace("_PATHS_", paths)
		url = url.replace("_TYPES_", types);
		
		$element.attr("href", url);
	});
}


function deselect(source) {
	var $browser = $JQry(source).closest(".file-browser"),
		$selected = $browser.find(".ui-selected");
	
	$selected.each(function(index, element) {
		$JQry(element).removeClass("ui-selected bg-primary");
	});
	
	updateSelectableControls($browser);
}


function gallery(source) {
	var $browser = $JQry(source).closest(".file-browser"),
		$selected = $browser.find(".ui-selected");
	
	$selected.find(".fancybox.thumbnail").first().trigger("click");
}
