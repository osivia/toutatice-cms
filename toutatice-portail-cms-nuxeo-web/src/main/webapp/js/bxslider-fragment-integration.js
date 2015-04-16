var $JQry = jQuery.noConflict();

var fgtSliderReady = false;
var fgtSlider;

/* Function done during fgtSlider instanciation */
function setTemporisation(){
	var time = jQuery(".bxfgtSlider").data("timer");
	if((typeof time === 'undefined') || (time == "")){
		time = 6000;
	} else {
		time = time*1000;
	}
	return time;
}

$JQry(document).ready(function() {
	if (!fgtSliderReady) {
		fgtSlider = $JQry(".bxfgtSlider").bxSlider({
			// General
			mode : "horizontal",  // Type of transition between slides
			
			// Controls
			autoControls : true,  // If true, "Start" / "Stop" controls will be added
			
			// Auto
			auto : true,  // Slides will automatically transition
			pause : setTemporisation(),  // The amount of time (in ms) between each auto transition
			autoHover : true,  // Auto show will pause when mouse hovers over fgtSlider
			autoDelay : 3000  // Time (in ms) auto show should wait before starting
		});

		fgtSliderReady = true;
	}
});


function goToSlide(index) {
	fgtSlider.goToSlide(index);
	fgtSlider.stopAuto();
}
