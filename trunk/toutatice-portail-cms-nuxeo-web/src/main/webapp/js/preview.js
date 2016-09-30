/* Constants
 * // FIXME: calculate prevMarginBottom
 */
var prevMarginBottom = 20;
var previewMinHPercent = 0.77;

function customPreviewContent(preview){
	var $body = $JQry(preview.contentWindow.document.body); 						
	$body.attr("style", "background-color: #f5f5f5");
	var $pages = $JQry(preview.contentWindow.document.body).find("div");	
	$pages.each(function(){
		var $styleValue = $JQry(this).attr("style");
		$JQry(this).attr("style", $styleValue + " margin: 0 auto;");
	});
}

function adaptPreviewToContent (preview){
	var $firstPage = $JQry(preview.contentWindow.document.body).find("div").first();
	var pageH = $firstPage.height();                
	$JQry(preview).height(pageH);
}

function resizePreview(){
	var $preview = $JQry(".file iframe.embed-preview"); 
	var preview = $preview.get(0); 
	
	var winH = window.innerHeight;                 
	var prevTop = $preview.offset().top;     		
	var scrollTop = $JQry(window).scrollTop();           
	
	var $firstPage = $JQry(preview.contentWindow.document.body).find("div").first();
	var firstPageH = $firstPage.height();
	
	var prevTopPosition = prevTop - scrollTop;    
	var firstPageBottom = prevTopPosition + firstPageH; 
	var previewMinH = 700;
	var prevVisiblePart = previewMinHPercent*winH;     
	
	if(scrollTop < firstPageBottom){  	 
		$preview.height(prevVisiblePart - prevMarginBottom);
	}
	
}

function adaptPreview(){
	var $preview = $JQry(".file iframe.embed-preview"); 			
	var preview = $preview.get(0);  						
	
	customPreviewContent(preview)
	adaptPreviewToContent(preview);
	resizePreview()
}

window.onload = adaptPreview;
window.onresize = resizePreview;
