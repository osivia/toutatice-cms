var $JQry = jQuery.noConflict();

var sliderReady = false;
var slider;

function setTemporisation(time){
	if(typeof time === 'undefined'){
		time = 6000;
	}
	return time;
}

$JQry(document).ready(function() {
	if (!sliderReady) {
		slider = $JQry(".bxslider").bxSlider({
			// General
			mode : "horizontal",  // Type of transition between slides
			
			// Controls
			autoControls : true,  // If true, "Start" / "Stop" controls will be added
			
			// Auto
			auto : true,  // Slides will automatically transition
			pause : setTemporisation(temporisation),  // The amount of time (in ms) between each auto transition
			autoHover : true,  // Auto show will pause when mouse hovers over slider
			autoDelay : 3000  // Time (in ms) auto show should wait before starting
		});

		sliderReady = true;
	}
});


function goToSlide(index) {
	slider.goToSlide(index);
	slider.stopAuto();
}
