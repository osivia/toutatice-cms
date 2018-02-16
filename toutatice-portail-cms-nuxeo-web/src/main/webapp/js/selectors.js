$JQry(function() {
	$JQry(".keywords-selector.auto-submit").each(function(index, element) {
		var $element = $JQry(element);
		
		if (!$element.data("loaded")) {
			var enterKey = 13,
				timer;
			
			$element.find("input[name=keyword]").on("input", function(event) {
				// Clear timer
				clearTimeout(timer);
				
				if (event.which != enterKey) {	
					timer = setTimeout(function() {
						var $target = $JQry(event.target),
							$formGroup = $target.closest(".form-group"),
							$submit = $formGroup.find("button[type=submit]");
						
						$submit.click();
					}, 200);
				}
			});
			
			$element.data("loaded", true);
		}
	});
});


/**
 * Refresh on vocabulary change.
 * 
 * @param selectText select text
 * @param url URL
 */
function refreshOnVocabularyChange(selectText, url) {
    url = url.replace("SELECTED_VALUE", encodeURIComponent(selectText.options[selectText.selectedIndex].value));
    updatePortletContent(selectText, url);
}
