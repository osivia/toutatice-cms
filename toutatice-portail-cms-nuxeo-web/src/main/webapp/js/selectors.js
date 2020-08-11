$JQry(function() {
	$JQry(".keywords-selector.auto-submit").each(function(index, element) {
		var $element = $JQry(element);
		
		if (!$element.data("loaded")) {
			var enterKey = 13,
				timer;
			
			$element.find("input[name=keyword]").keyup(function(event) {
				// Clear timer
				clearTimeout(timer);
				
				if (event.which != enterKey) {	
					timer = setTimeout(function() {
						var $target = $JQry(event.target);
						var $formGroup = $target.closest(".form-group");
						var $submit = $formGroup.find("button[type=submit]");
						
						$submit.click();
					}, 200);
				}
			});

			$element.find("button[data-action=reset]").click(function(event) {
				var $target = $JQry(event.target);
				var $formGroup = $target.closest(".form-group");

				$formGroup.find("input[name=keyword]").val("");
				$formGroup.find("button[type=submit]").click();
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
