// Datepicker integration
// Required JQuery UI with widgets modules

$JQry(function() {
	$JQry(".dates-selector input").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat : "dd/mm/yy",
		numberOfMonths : 1,
		selectOtherMonths : true,

		beforeShow : function(input, inst) {
			customizeRendering(inst);
		},
		
		onChangeMonthYear : function(year, month, inst) {
			customizeRendering(inst);
		},
		
		onClose : function(dateText, inst) {
			var fromSuffix = "-date-from";
			var toSuffix = "-date-to";
			
			if (this.id.indexOf(fromSuffix, this.id.length - fromSuffix.length) !== -1) {
				// From
				var prefix = this.id.substring(0, this.id.length - fromSuffix.length);
				var $to = $JQry("#" + prefix + toSuffix);
				$to.datepicker("option", "minDate", dateText);
			} else if (this.id.indexOf(toSuffix, this.id.length - toSuffix.length) !== -1) {
				// To
				var prefix = this.id.substring(0, this.id.length - toSuffix.length);
				var $from = $JQry("#" + prefix + fromSuffix);
				$from.datepicker("option", "maxDate", dateText);
			}
		}
	});
});


function customizeRendering(inst) {
	setTimeout(function() {
		// z-index
		inst.dpDiv.css("z-index", 10);
		
		// Header
		var $header = inst.dpDiv.find(".ui-datepicker-header");
		
		// Previous button
		var $previous = $header.find(".ui-datepicker-prev");
		$previous.addClass("btn btn-default pull-left");
		$previous.find("span").remove();
		$previous.append($JQry(document.createElement("i")).addClass("halflings halflings-circle-arrow-left"));
		
		// Next button
		var $next = $header.find(".ui-datepicker-next");
		$next.addClass("btn btn-default pull-right");
		$next.find("span").remove();
		$next.append($JQry(document.createElement("i")).addClass("halflings halflings-circle-arrow-right"));
		
		// Form
		var $title = $header.find(".ui-datepicker-title");
		$title.addClass("form-inline text-overflow");
		$title.children("select").addClass("form-control");
		$title.children("span").addClass("form-control-static");
	}, 0);
} 
