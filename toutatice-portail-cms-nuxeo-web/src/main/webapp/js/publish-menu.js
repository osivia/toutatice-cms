// Publish menu drag & drop + Fancytree functions
// Required JQuery UI with interactions modules

$JQry(function() {
	
	// Fancytree lazy, with drag & drop
	$JQry(".fancytree.fancytree-lazy").each(function(index, element) {
		var $element = $JQry(element);
		
		if (!$element.data("loaded")) {

	
			$element.fancytree({
				activeVisible: true,
				extensions: ["dnd", "glyph"],
				tabbable: false,
				titlesTabbable: true,
				toggleEffect: false,
		
				dnd: {
					autoExpandMS: 500,
					preventRecursiveMoves: true,
					preventVoidMoves: true,
					
					draggable: {
						scroll: false
					},
					
					dragEnter: function(targetNode, data) {
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
		
						
						var $source = $JQry(data.draggable.helper.context),
							sourceTypes = $source.data("types").split(","), 
							targetAcceptedTypes = targetNode.data.acceptedtypes.split(","),
							accepted = "over";
						
						
						jQuery.each(sourceTypes, function(index, sourceType) {
							var match = false;
							
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
		
						return accepted;
					},
					
					dragOver: function(targetNode, data) {
						$JQry(targetNode.span).addClass("bg-info text-white").removeClass("text-muted");
					},
					
					dragDrop: function(targetNode, data) {
						var $menu = targetNode.tree.$div.closest(".menu"),
							
							// Source
							$source = $JQry(data.draggable.helper.context),
							sourceIds = $source.data("identifiers"),
							
							// Target
							targetId = targetNode.data.id;
		
							// AJAX shadowbox
							$ajaxShadowbox = $JQry(".file-browser .file-browser-ajax-shadowbox");
						
							// AJAX parameters
							container = null,
							options = {
								method : "post",
								postBody : "sourceIds=" + sourceIds + "&targetId=" + targetId
							},
							url = $menu.data("dropurl"),
							eventToStop = null,
							callerId = null;
		
						$ajaxShadowbox.addClass("in");
							
						directAjaxCall(container, options, url, eventToStop, callerId);
					},
					
					dragLeave: function(targetNode, data) {
						$JQry(targetNode.span).removeClass("bg-info text-white").addClass("text-muted");
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
							// (ajax call) eq. footer
						    var options = new Object();
								// We have a get
						    options.method = "get"
						    // We don't block
						    options.asynchronous = false;
							directAjaxCall(null,options, node.data.href, null);
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
			
			$element.data("loaded", true);
		}
	})	
});


function publishMenuState( element, action, state)	{

	var $element = $JQry(element);
	

	if( action == 'restore')	{
		// restore expanded
		$element.find(".fancytree.fancytree-lazy").fancytree("getTree").visit(function(node) {
            expanded = state.get(node.data.id);	
            if( expanded != null)    {	
           		node.setExpanded(expanded);	
            }
		});
	}

	if( action == 'save')	{
		// save expanded
		const map = new Map();
		$element.find(".fancytree.fancytree-lazy").fancytree("getTree").visit(function(node) {
			map.set(node.data.id, node.isExpanded());

		});
		return map;
	}
	
}

