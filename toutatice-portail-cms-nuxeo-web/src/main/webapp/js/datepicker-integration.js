// Datepicker integration
// Required JQuery UI with widgets modules

$JQry(function() {
	$JQry("input[type='date']").datepicker({
		changeMonth : true,
		dateFormat : "dd/mm/yy",
		numberOfMonths : 1,

		beforeShow : function(input, inst) {
			setTimeout(function() {
				// z-index
				inst.dpDiv.css("z-index", 10);
				
				// Form
				var $title = inst.dpDiv.find(".ui-datepicker-title");
				$title.addClass("form-inline");
				$title.children("select").addClass("form-control");
				$title.children("span").addClass("form-control-static");
			}, 0);
		},
		
		onSelect : function(dateText, inst) {
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
