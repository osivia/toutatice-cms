function downloadPreview(){
	var $iframeWindow = $JQry(".pdf-preview-iframe");
	var $previewErr = $JQry(".file-preview-unavailable");
	
	var $progress = $JQry(".file .progress");
	var $loadBar = $JQry(".file .loadBar");
	var $downloadBar = $JQry(".file .downloadBar");
	
	var iframeContext = $iframeWindow[0].contentWindow;
	
	if(iframeContext){
		var previewUrl = $JQry($iframeWindow[0]).data("preview-url");
		if(previewUrl){
			
			iframeContext.webViewerLoad();
			
			var xhr = new XMLHttpRequest();
			xhr.onreadystatechange = function() {
				if (this.readyState == 4) {
					if(this.status == 200){
						$progress.remove();
						iframeContext.PDFViewerApplication.open(new Uint8Array(xhr.response));
						var header = this.getResponseHeader('Content-Disposition');
						var fileName = header.match(/filename="(.+)"/)[1];
						iframeContext.PDFViewerApplication.setTitleUsingUrl(fileName);
						$iframeWindow.removeClass("hidden");
					}else{
						$progress.remove();
						$iframeWindow.remove();
						$previewErr.removeClass("hidden");
					}
				}
			};
			xhr.onprogress = function (event) {
				var loaded = (event.loaded / event.total) * 100 ;
				$downloadBar.width(loaded+"%");
				$downloadBar.attr("aria-valuenow", loaded);
				$downloadBar.find(".sr-only").first().text(loaded+"%");
				
			};
			try {
				xhr.open('GET', previewUrl);
				xhr.responseType = 'arraybuffer';
				xhr.send();
			} catch (e) {
				$progress.remove();
				$iframeWindow.remove();
				$previewErr.removeClass("hidden");
			}
		}
	}else{
		$progress.remove();
		$previewErr.removeClass("hidden");
	}
};

