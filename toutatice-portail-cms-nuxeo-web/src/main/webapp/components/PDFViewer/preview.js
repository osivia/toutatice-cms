function downloadPreview(){
	var $iframeWindow = $JQry(".pdf-preview-iframe");
	var $previewErr = $JQry(".file-preview-unavailable");
	
	var $progress = $JQry(".document-file .progress");
	
	var iframeContext = $iframeWindow[0].contentWindow;
	
	if(iframeContext){
		var previewUrl = $JQry($iframeWindow[0]).data("preview-url");
		if(previewUrl){
			
			iframeContext.webViewerLoad();
			
			var xhr = new XMLHttpRequest();
			xhr.onreadystatechange = function() {
				if (this.readyState == 4) {
					if(this.status == 200){
						$progress.parent().remove();
						iframeContext.PDFViewerApplication.open(new Uint8Array(xhr.response));
						var header = this.getResponseHeader('Content-Disposition');
						var fileName = header.match(/filename="(.+)"/)[1];
						iframeContext.PDFViewerApplication.setTitleUsingUrl(fileName);
						$iframeWindow.removeClass("d-none");
					}else{
						$progress.parent().remove();
						$iframeWindow.remove();
						$previewErr.removeClass("d-none");
					}
				}
			};
			xhr.onprogress = function (event) {
				var loaded = (event.loaded / event.total) * 100 ;
				$progress.children(".progress-bar").width(loaded+"%");
				$progress.children(".progress-bar").attr("aria-valuenow", loaded);
			};
			try {
				xhr.open('GET', previewUrl);
				xhr.responseType = 'arraybuffer';
				xhr.send();
			} catch (e) {
				$progress.parent().remove();
				$iframeWindow.remove();
				$previewErr.removeClass("d-none");
			}
		}
	}else{
		$progress.parent().remove();
		$previewErr.removeClass("d-none");
	}
};

