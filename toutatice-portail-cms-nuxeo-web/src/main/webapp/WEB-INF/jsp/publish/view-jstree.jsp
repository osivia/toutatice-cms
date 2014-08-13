
<%@page import="java.io.IOException"%>
<%@page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>
<%@ page contentType="text/plain; charset=UTF-8"%>


<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>


<%@page import="javax.portlet.WindowState"%>




<%@page import="javax.portlet.ResourceURL"%>
<%@page import="org.nuxeo.ecm.automation.client.model.Document"%>
<%@page
    import="org.nuxeo.ecm.automation.client.model.PropertyList"%>





<%@page
    import="fr.toutatice.portail.cms.nuxeo.portlets.publish.NavigationDisplayItem"%><portlet:defineObjects />




<%!public void displayItem(javax.servlet.jsp.JspWriter out, NavigationDisplayItem itemToDisplay, int level,
            int openLevels) throws IOException {

        String cssClass = "";
        cssClass = "level_" + level;
        
        out.println("<ul class=\"" + cssClass + "\">");
        
        int iChild = 0;
        int size = itemToDisplay.getChildren().size();

        for (NavigationDisplayItem child : itemToDisplay.getChildren()) {
            {
                String target = "";
                cssClass = "";
                
                if (child.isExternal()){
                    target = "target=\"_blank\"";
                    cssClass += " osivia-link-external";
                } 

                
    
                if (child.isSelected()) {
                        cssClass += " current";

                }
                
                if( level == 0){
                    if( iChild == 0)
                        cssClass += " first";
                    if( iChild == size - 1)
                        cssClass += " last";                    
                }
                
                String li = "<li class=\"" + cssClass + "\"";
                li += ">";

                out.println(li);

                out.println("<a " + target + " class=\"" + cssClass + "\"  href=\"" + child.getUrl() + "\">" + child.getTitle() + "</a>");

                if (child.getChildren().size() > 0) {
                    if (child.isSelected() || (level + 1 < openLevels)) {
                        displayItem(out, child, level + 1, openLevels);
                    }
                }

                out.println("</li>");
                
                iChild ++;
            }

        }
        out.println("</ul>");

    }%>


<div class="no-ajax-link">
<input type="text" onkeyup="jstreeSearch('navtree', this.value)" />


<div id="navtreedebug" class="jstree-links">
<ul>
</ul>
</div>

<%
    NavigationDisplayItem itemToDisplay = (NavigationDisplayItem) request.getAttribute("displayItem");
    int openLevels = (Integer) request.getAttribute("openLevels");
%>

<%
    if (itemToDisplay != null) {
%>

<div id="navtree" class="jstree-links">
    <%
        displayItem(out, itemToDisplay, 0, openLevels);
    %>
</div>

<%
    }
%>






</div>


<script>


var $JQry = jQuery.noConflict();

$JQry(document).ready(function() {
    
    // Display links JSTree
    $JQry("#navtreedebug,#navtree").jstree({
        "core" : {
            "animation" : 0,
            "open_parents" : true
        },
        "themes" : {
            "theme" : "default",
            "dots" : false,
            "icons" : true
        },
        "search" : {
            "case_insensitive" : true,
            "show_only_matches" : true
        },
        "plugins" : [ "themes", "html_data", "search" ]
    });

});



</script>



