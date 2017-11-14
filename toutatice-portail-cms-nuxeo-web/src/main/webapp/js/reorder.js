// Reorder documents functions

$JQry(function() {
	
	$JQry(".reorder-sortable").sortable({
		axis: "y",
		cursor: "move",
		forcePlaceholderSize: true,
		placeholder: "list-sortable-placeholder bg-info",
		tolerance: "pointer",
		
		update: function(event, ui) {
			var $form = $JQry(this).closest("form"),
				$input = $form.find("input[name=order]");
				$documents = $form.find(".reorder-sortable"),
				order = "";
			
			$documents.find("li").each(function(index, element) {
				if (index > 0) {
					order += "|";
				}
				order += $JQry(element).data("id");
			});
			
			// Update input value
			$input.val(order);

			// Update fancybox
			parent.$JQry.fancybox.update();
		}
	});
	
	$JQry(".reorder-sortable").disableSelection();
	
});
