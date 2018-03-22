function onSubmitSearch(form) {
	var $form = $JQry(form),
		searchURL = $form.data("url");
	
	searchURL = searchURL.replace("__REPLACE_KEYWORDS__", form.keywords.value);
	form.action = searchURL;
}
