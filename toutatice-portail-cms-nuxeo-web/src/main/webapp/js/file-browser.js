// File browser drag & drop and selection functions
// Required JQuery UI with interactions modules

$JQry(function() {

	var previousIndex = -1,
		isChromeAndroid = /Chrome/i.test(navigator.userAgent) && /Mobile/i.test(navigator.userAgent) && /Android/i.test(navigator.userAgent);
	
	
	if (!isChromeAndroid) {
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
				
				displayControls($browser);
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
			cursor: "move",
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
					$browser = $target.closest(".file-browser"),
					$toolbar = $browser.find(".btn-toolbar"),
					$li = $target.closest("li"),
					$data = $li.find(".data"),
					$selectable = $target.closest(".selectable"),
					$selected = $selectable.find(".ui-selected"),
					$elements, writable;
	
				if ($data.hasClass("ui-selected")) {
					$selected.addClass("dragged");
				} else {
					$selected.each(function(index, element) {
						var $element = $JQry(element);
						$element.removeClass("ui-selected bg-primary");
					});
				}
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
			
			displayControls($browser);
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
	} // if (isChromeAndroid)

	
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
		dropZone : ".drop-zone",
		singleFileUploads : false,
		
		add : function(e, data) {
			var $this = $JQry(this),
				$browser = $this.closest(".file-browser"),
				$panel = $browser.find(".file-upload .panel"),
				$list = $browser.find(".file-upload .file-upload-list");
			    $overwritteAlert = $browser.find(".file-upload .alert-warning");
			
			var $cancelGlyph = $JQry(document.createElement("i")).addClass("halflings halflings-ban-circle");
			var $cancelText = $JQry(document.createElement("span")).text($panel.find(".cancel").first().text());

			// Display panel
			$panel.removeClass("hidden");
			
			// Context
			data.context = $JQry(document.createElement("li"))
			data.context.addClass("template-upload list-group-item")
			data.context.appendTo($list);
			
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
			
			
			var nameConflicts = [];
			$JQry.each(data.files, function(index, file) {
				// Item content
				$content = $JQry(document.createElement("p"));
				$content.text(file.name);
				$content.appendTo($listItem);
				
				$browser.find("li").each(function(index, li) {
					if($JQry(this).find("a").text() === file.name){
						$JQry(this).addClass("bg-warning");
						nameConflicts.push(file.name);
					}
				});
				
			});
			if(nameConflicts.length > 0){
				var $nameList = $overwritteAlert.children("ul");
				for (i = 0; i < nameConflicts.length; i++) {
					$nameList.append("<li>"+nameConflicts[i]+"</li>");
				}
				$overwritteAlert.removeClass("hidden");
			}
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
		
		finished : function (e, data) {
			var $this = $JQry(this),
				$browser = $this.closest(".file-browser"),
				$panel = $browser.find(".file-upload .panel"),
				$list = $browser.find(".file-upload .file-upload-list"),
				$overwritteAlert = $browser.find(".file-upload .alert-warning");
			
			$browser.find("li").removeClass("bg-warning");
			$overwritteAlert.addClass("hidden");
			
			var $paragraphs = $list.find("p");
			if($paragraphs.length){
				var alert = false;
				$paragraphs.each(function(index, paragraph) {
					var fileName = $JQry(paragraph).text();
					
					$browser.find("li").each(function(index, li) {
						if($JQry(this).find("a").text() === fileName){
							$JQry(this).addClass("bg-warning");
							alert = true;
						}
					});
				});
				if(alert){
					$overwritteAlert.removeClass("hidden");
				}
			}else{
				$panel.addClass("hidden");
			}
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


function displayControls($browser) {
	var $toolbar = $browser.find(".btn-toolbar"),
		$messageSelection = $toolbar.find(".message-selection"),
		$links = $toolbar.find("a[data-url]"),
		$single = $toolbar.find(".single-selection"),
		$waiter = $toolbar.find(".ajax-waiter"),
		$edit = $toolbar.find(".edit"),
		$copy = $toolbar.find(".copy"),
		$move = $toolbar.find(".move"),
		$delete = $toolbar.find(".delete"),
		$selected = $browser.find(".ui-selected"),
		identifiers = "", paths = "", types = "",
		ajaxPendingCounter;
	
	// Sortable
	$browser.find(".sortable-handle").addClass("hidden");
	
	// Reset links
	$links.each(function(index, element) {
		var $element = $JQry(element);
		
		$element.attr("href", $element.data("url"));
	});
	
	if ($selected.length == 0) {
		// No selection
		$toolbar.hide();
	} else {
		$toolbar.show();
		$waiter.hide();
		
		$edit.addClass("disabled");
		$copy.addClass("disabled");
		$move.addClass("disabled");
		$delete.addClass("disabled");
		
		if ($selected.length == 1) {
			// Single element selected
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
				var path = $selected.data("path");
				
				// Edition of document having draft
				var draftPath = $selected.data("draft-path");
				if(draftPath && $element.hasClass('edit')){
					path = draftPath;
				}
				
				url = url.replace("_PATH_", path);
				
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
		} else {
			// Multiple elements selected
			$single.hide();
			$messageSelection.children(".badge").text($selected.length);
			$messageSelection.children(".text").text($messageSelection.data("message-multiple-selection"));
		}
		
		
		// Update element infos
		$elements = $selected.filter(function(index, element) {
			var $element = $JQry(element);

			return ($element.data("loaded") != true);
		});
		if ($elements.length > 0) {
			$waiter.show();
			ajaxPendingCounter = $elements.length;
			
			$elements.each(function(index, element) {
				var $element = $JQry(element);
				
				jQuery.ajax({
					url : $toolbar.data("infosurl"),
					data : {
						path : $element.data("path")
					},
					success : function(data, status, xhr) {
						$element.data("loaded", true);
						
						if ("success" == status) {
							$element.data("writable", (data["writable"] == true));
							$element.data("copiable", (data["copiable"] == true));
						}
						
						ajaxPendingCounter--;
						
						if (ajaxPendingCounter == 0) {
							$waiter.hide();
							
							updateControlRights($browser);
						}
					}
				});
			});
		} else {
			updateControlRights($browser);
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
}


function updateControlRights($browser) {
	var $toolbar = $browser.find(".btn-toolbar"),
		$edit = $toolbar.find(".edit"),
		$copy = $toolbar.find(".copy"),
		$move = $toolbar.find(".move"),
		$delete = $toolbar.find(".delete"),
		$selected = $browser.find(".ui-selected"),
		writable = true,
		copiable = true;
	
	// Copiable indicator
	$selected.each(function(index, element) {
		var $element = $JQry(element);
		
		if ($element.data("copiable") != true) {
			copiable = false;

			// Break
			return false;
		} 
	});
	
	// Writable indicator
	$selected.each(function(index, element) {
		var $element = $JQry(element);
		
		if ($element.data("writable") != true) {
			writable = false;

			// Break
			return false;
		} 
	});
	
	
	if ($selected.length == 1) {
		// Edit
		if ($selected.data("editable") && writable) {
			$edit.removeClass("disabled");
		}
		// Copy
		if (copiable) {
			$copy.removeClass("disabled");
		}
	}
	
	
	// Move & delete
	if (writable) {
		$move.removeClass("disabled");
		$delete.removeClass("disabled");
	}
}


function deselect(source) {
	var $browser = $JQry(source).closest(".file-browser"),
		$selected = $browser.find(".ui-selected");
	
	$selected.each(function(index, element) {
		$JQry(element).removeClass("ui-selected bg-primary");
	});
	
	displayControls($browser);
}


function gallery(source) {
	var $browser = $JQry(source).closest(".file-browser"),
		$selected = $browser.find(".ui-selected");
	
	$selected.find(".fancybox.thumbnail").first().trigger("click");
}
