
$JQry(function() {
    $JQry(".fragment-edition").each(function(index, element) {
        var $element = $JQry(element);

        if (!$element.data("loaded")) {

            $selects = $element.find('select.fragment-type');
            $selects.on('change', function()
            {
                $this = $JQry(this);
                $link = $this.siblings( ".selectLink" );
                href =  $link.attr('href');
                href = href + "&fragmentTypeId="+ this.value;
                $link.attr('href', href);
                $link.click();
            });
        


            $element.data("loaded", true);
        }
    });
});


