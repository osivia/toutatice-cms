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
					$selectable = $selecting.closest(".selectable"),
					$selectee = $selectable.find(".data"),
					currentIndex = $selectee.index(ui.selecting);
				
				$selectable.addClass("remove-hover");
				
				if (event.shiftKey && previousIndex > -1) {
					$selectee.slice(Math.min(previousIndex, currentIndex), Math.max(previousIndex, currentIndex) + 1).addClass("ui-selected bg-primary");
				} else {
					$selecting.addClass("bg-info");
					previousIndex = currentIndex;
				}
			},
			
			stop: function(event, ui) {
				var $target = $JQry(event.target),
					$selectable = $target.closest(".selectable"),
					$browser = $selectable.closest(".file-browser");
				
				$selectable.removeClass("remove-hover");
				
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
			appendTo: "body",
			connectToFancytree: true,
			cursor: "move",
			distance: 10,
	
			helper: function(event) {
				var $target = $JQry(event.target),
					$draggable = $target.closest(".draggable"),
					$li = $target.closest("li"),
					$data = $li.find(".data"),
					$selectable = $data.closest(".selectable"),
					offset = $draggable.offset(),
					click = {
						top: event.pageY - offset.top,
						left: event.pageX - offset.left
					},
					identifiers = "", types = "", text = "",
					$selected, $helper, $content, $icon, $title;

				// Selected items
				if ($data.hasClass("ui-selected")) {
					$selected = $selectable.find(".ui-selected");
				} else {
					$selected = $data;
				}

				// Identifiers, types & text
				$selected.each(function(index, element) {
					var $element = $JQry(element),
						$draggable = $element.find(".draggable");
					
					if (index > 0) {
						identifiers += ",";
						types += ",";
						text += ", ";
					}
					
					identifiers += $element.data("id");
					types += $element.data("type");
					text += jQuery.trim($draggable.text());
				});
				
				// Helper
				$helper = $JQry(document.createElement("div"));
				$helper.addClass("file-browser-helper");
				$helper.data({
					identifiers: identifiers,
					types: types
				});
				$helper.css({
					height: 0,
					width: 0
				});
				
				// Helper content
				$content = $JQry(document.createElement("div"));
				$content.addClass("bg-primary");
				$content.appendTo($helper);
				
				// Helper content animation
				$content.css({
					width: $data.width()
				});
				$content.animate({
					top: click.top + 1,
					left: click.left + 1,
					width: 300
				}, 300);
				
				// Icon
				$icon = $JQry(document.createElement("div"));
				$icon.addClass("document-icon");
				$icon.appendTo($content);
				if ($selected.length == 1) {
					$selected.find(".document-icon").children().clone().appendTo($icon);
				} else {
					$iconInner = $JQry(document.createElement("div"));
					$iconInner.appendTo($icon);
					
					$strong = $JQry(document.createElement("strong"));
					$strong.text($selected.length);
					$strong.appendTo($iconInner);
				}
				
				// Title
				$title = $JQry(document.createElement("div"));
				$title.addClass("text-overflow");
				$title.text(text);
				$title.appendTo($content);
				
				return $helper;
			},
			
			revert: "invalid",
			revertDuration: 200,
			
			start: function(event, ui) {
				var $target = $JQry(event.target),
					$li = $target.closest("li"),
					$data = $li.find(".data"),
					$selectable = $li.closest(".selectable"),
					$selected = $selectable.find(".ui-selected");
	
				$selectable.addClass("remove-hover");
				
				if ($data.hasClass("ui-selected")) {
					$selected.addClass("dragged");
				} else {
					deselect($selectable);
					$data.addClass("dragged");
				}
			},
			
			stop: function(event, ui) {
				var $target = $JQry(event.target),
					$selectable = $target.closest(".selectable"),
					$dragged = $selectable.find(".dragged");
			
				$selectable.removeClass("remove-hover");
				
				$dragged.removeClass("dragged");
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
				var $droppable = $JQry(this),
					$selectable = $droppable.closest(".selectable"),
					$selected = $selectable.find(".ui-selected"),
					targetAcceptedTypes = $droppable.data("accepted-types").split(","),
					accepted = true;
					
				if ($draggable.hasClass("ui-sortable-helper") || $droppable.closest(".data").hasClass("dragged")) {
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
	var $toolbar = $browser.find(".table .table-header .contextual-toolbar"),
		$messageSelection = $toolbar.find(".message-selection"),
		$links = $toolbar.find("a[data-url]"),
		$single = $toolbar.find(".single-selection"),
		$bulkDownload = $toolbar.find(".bulk-download"),
		$waiter = $toolbar.find(".ajax-waiter"),
		$edit = $toolbar.find(".edit"),
		$driveEdit = $toolbar.find(".drive-edit"),
		$onlyofficeRead = $toolbar.find(".onlyoffice-read"),		
		$onlyofficeEditLock = $toolbar.find(".onlyoffice-edit-lock"),
		$onlyofficeEditCollab = $toolbar.find(".onlyoffice-edit-collab"),
		$allEdit = $toolbar.find(".all-edit"),
		$liDrive = $allEdit.find(".li-drive-edit"),
		$singleEdit = $toolbar.find(".single-edit"),
		$download = $toolbar.find(".download"),
		$copy = $toolbar.find(".copy"),
		$gallery = $toolbar.find(".gallery"),
		$move = $toolbar.find(".move"),
		$delete = $toolbar.find(".delete"),
		$selected = $browser.find(".ui-selected"),
		identifiers = "", paths = "", types = "",
		downloadUrl,
		ajaxPendingCounter;
	
	// Sortable
	$browser.find(".sortable-handle").addClass("hidden");
	
	// Reset links
	$links.each(function(index, element) {
		var $element = $JQry(element);
		
		
		if($element.data("load-url")){
			// for modals
			$element.data("load-url", $element.data("url"));
		}else{
			// for links
			$element.attr("href", $element.data("url"));
		}
	});
	
	if ($selected.length) {
		$toolbar.addClass("in");
		$waiter.hide();
		
		$edit.addClass("disabled");
		$driveEdit.addClass("hidden disabled");
		$onlyofficeRead.addClass("hidden disabled");
		$onlyofficeEditLock.addClass("hidden disabled");
		$onlyofficeEditCollab.addClass("hidden disabled");		
		$allEdit.addClass("hidden disabled");
		$liDrive.addClass("hidden disabled");
		$singleEdit.addClass("hidden disabled");
		$copy.addClass("disabled");
		$move.addClass("disabled");
		$delete.addClass("disabled");
		
		if ($selected.length == 1) {
			// Single element selected
			$single.show();
			$bulkDownload.hide();
			$messageSelection.children().text("1 " + $messageSelection.data("message-single-selection"));
			
			
			// Sortable
			$selected.find(".sortable-handle").removeClass("hidden");
			
			
			// Drive edition replacement for file types
			if ($selected.data("file")) {
				$edit.addClass("hidden");
				$driveEdit.find("i").attr("class", $selected.data("icon"));
				
				if ($toolbar.data("drive-enabled")) {
					$driveEdit.removeClass("hidden");
				}
			} else {
				$edit.removeClass("hidden");
			}
			
			
			// Download
			downloadUrl = $selected.data("download-url");
			if (downloadUrl) {
				$download.attr("href", downloadUrl);
				$download.removeClass("hidden");
			} else {
				$download.addClass("hidden");
			}
			
			
			// Gallery
			if ($selected.find(".fancybox.thumbnail").length) {
				$gallery.removeClass("hidden");
			} else {
				$gallery.addClass("hidden");
			}
			
			
			// Update links with single-selected properties
			$links.each(function(index, element) {
				var $element = $JQry(element),
					url = $element.attr("href");
				
				// Update path
				var path = $selected.data("path");
				
				if($element.data("load-url")){
					// for modals
					var loadUrl = $element.data("load-url");
					loadUrl = loadUrl.replace("_PATH_", path);
					$element.data("load-url", loadUrl);
				}else{
					// for links
					
					// Edition of document having draft
					var draftPath = $selected.data("draft-path");
					if(draftPath && $element.hasClass('edit')){
						path = draftPath;
					}
					
					url = url.replace("_PATH_", path);
					$element.attr("href", url);
				}
			});
			
		} else {
			// Multiple elements selected
			
			// for bulkDownload, all documents must be files
			var allFiles = true;
			// size shouldn't exceed 100000000 bytes
			var sizeSum = 0;
			$selected.each(function(){
				var $element = $JQry(this);
				if(!$element.data("file")){
					allFiles = false;
				}
				sizeSum += $element.data("size");
			});
			var $bulkDownloadLink = $bulkDownload.children();
			var title;
			if(!allFiles){
				$bulkDownloadLink.addClass("disabled");
				title = $bulkDownloadLink.data("message-not-file");
			}else if(sizeSum>100000000){
				$bulkDownloadLink.addClass("disabled");
				title = $bulkDownloadLink.data("message-too-large");
			}else{
				$bulkDownloadLink.removeClass("disabled");
				title = $bulkDownloadLink.data("message-ok");
			}
			$bulkDownloadLink.attr("title", title).tooltip('fixTitle')
			$bulkDownloadLink.children("span").text(title);
			
			$single.hide();
			$bulkDownload.show();
			$messageSelection.children().text($selected.length + " " + $messageSelection.data("message-multiple-selection"));
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
					url: $toolbar.data("infos-url"),
					data: {
						path: $element.data("path"),
						file: $element.data("file"),
					},
					success : function(data, status, xhr) {
						$element.data("loaded", true);
						
						if ("success" == status) {
							$element.data("writable", (data["writable"] == true));
							$element.data("copiable", (data["copiable"] == true));
							$element.data("drive-edit-url", data["driveEditUrl"]);
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
	} else {
		// No selection
		$toolbar.removeClass("in");
	}
}


function updateControlRights($browser) {
	var $toolbar = $browser.find(".table .table-header .contextual-toolbar"),
		$edit = $toolbar.find(".edit"),
		$driveEdit = $toolbar.find(".drive-edit"),
		$onlyofficeRead = $toolbar.find(".onlyoffice-read"),		
		$onlyofficeEditLock = $toolbar.find(".onlyoffice-edit-lock"),
		$onlyofficeEditCollab = $toolbar.find(".onlyoffice-edit-collab"),
		$allEdit = $toolbar.find(".all-edit"),
		$singleEdit = $toolbar.find(".single-edit"),
		$liDrive = $allEdit.find(".li-drive-edit"),
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
		
		// Drive edit
		driveEditUrl = $selected.data("drive-edit-url");
		driveEnabled = $toolbar.data("drive-enabled");
		onlyofficeEditLockUrl = $selected.data("onlyoffice-edit-lock-url");
		onlyofficeEditCollabUrl = $selected.data("onlyoffice-edit-collab-url");
		
		if (writable && driveEditUrl && onlyofficeEditCollabUrl){
			$driveEdit.attr("href", driveEditUrl);
			$onlyofficeEditLock.attr("href", onlyofficeEditLockUrl);
			$onlyofficeEditCollab.attr("href", onlyofficeEditCollabUrl);
			$driveEdit.removeClass("hidden disabled");
			$onlyofficeEditLock.removeClass("hidden disabled");
			$onlyofficeEditCollab.removeClass("hidden disabled");
			$allEdit.removeClass("hidden disabled");
			$liDrive.removeClass("hidden disabled");
		}else if (driveEditUrl) {
			$driveEdit.attr("href", driveEditUrl);
			$driveEdit.removeClass("hidden disabled");
			$singleEdit.removeClass("hidden disabled");
		}else if(writable && onlyofficeEditCollabUrl){
			$onlyofficeEditLock.attr("href", onlyofficeEditLockUrl);
			$onlyofficeEditCollab.attr("href", onlyofficeEditCollabUrl);
			$onlyofficeEditLock.removeClass("hidden disabled");
			$onlyofficeEditCollab.removeClass("hidden disabled");
			if(driveEnabled){
				$allEdit.removeClass("hidden disabled");
				$liDrive.removeClass("hidden");
			}else{
				$singleEdit.removeClass("hidden disabled");
			}
		}else if(onlyofficeEditCollabUrl){
			$onlyofficeRead.attr("href", onlyofficeEditCollabUrl);
			$onlyofficeRead.removeClass("hidden disabled");
		}else if (driveEnabled){
			$singleEdit.removeClass("hidden disabled");
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
