function customPreviewContent(preview){
	var $body = $JQry(preview.contentWindow.document.body); 						console.log("$body: " + $body);
	$body.attr("style", "background-color: #f5f5f5");
	var $pages = $JQry(preview.contentWindow.document.body).find("div").first();	
	var $styleValue = $pages.attr("style");
	$pages.attr("style", $styleValue + " margin: 0 auto;");
}

function adaptPreviewToContent (preview){
	var $firstPage = $JQry(preview.contentWindow.document.body).find("div").first();
	var pageH = $firstPage.height();          console.log("pageH: " + pageH);       
	$JQry(preview).height(pageH);
}

function resizePreview(){
	var $preview = $JQry(".file iframe.embed-preview"); 
	var preview = $preview.get(0); 
	var prevMarginBottom = 25;
	
	var winH = window.innerHeight;                 
	var prevTop = $preview.offset().top;     		
	var scrollTop = $JQry(window).scrollTop();           
	
	var $firstPage = $JQry(preview.contentWindow.document.body).find("div").first();
	var firstPageH = $firstPage.height();
	
	var prevTopPosition = prevTop - scrollTop;    console.log("prevTopPosition: " + prevTopPosition);
	var firstPageBottom = prevTopPosition + firstPageH; 
	var prevVisiblePart = winH;      
	if(prevTop > 0){
		prevVisiblePart = winH - prevTopPosition;
	}       console.log("prevVisiblePart: " + prevVisiblePart);   
	
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
window.onscroll = resizePreview;
