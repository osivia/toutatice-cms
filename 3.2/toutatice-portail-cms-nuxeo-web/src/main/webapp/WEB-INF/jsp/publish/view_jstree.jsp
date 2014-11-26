
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
        int size = itemToDisplay.getChildrens().size();
        

        for (NavigationDisplayItem child : itemToDisplay.getChildrens()) {
            {
                String target = "";
                cssClass = "";
                
                if (child.isExternal()){
                    target = "target=\"_blank\"";
                    cssClass += " osivia-link-external";
                } 

                String id= null;
    
                if (child.isSelected()) {
                        cssClass += " current";

                        boolean lastItemSelected = true;
                        for (NavigationDisplayItem subchild : child.getChildrens()) {
                            if( subchild.isSelected())
                                lastItemSelected = false;
                            
                        }
                        
                        if( lastItemSelected){
                            id= "selected";
                        }
                        
                }
                
                if( level == 0){
                    if( iChild == 0)
                        cssClass += " first";
                    if( iChild == size - 1)
                        cssClass += " last";                    
                }
                
                String li = "<li class=\"" + cssClass + "\"";
                
                if( id != null)
                    li += " id=\""+id+"\"";
                li += ">";

                out.println(li);

                out.println("<a " + target + " class=\"" + cssClass + "\"  href=\"" + child.getUrl() + "\">" + child.getTitle() + "</a>");

                if (child.getChildrens().size() > 0) {
//                    if (child.isSelected() || (level + 1 < openLevels)) {
                        displayItem(out, child, level + 1, openLevels);
//                    }
                }

                out.println("</li>");
                
                iChild ++;
            }

        }
        out.println("</ul>");

    }%>
    

<div class="no-ajax-link">
<input type="text" onkeyup="jstreeSearch('<portlet:namespace/>navtree', this.value)" />


<%
    NavigationDisplayItem itemToDisplay = (NavigationDisplayItem) renderRequest.getAttribute("itemToDisplay");
    NuxeoController ctx = (NuxeoController) renderRequest.getAttribute("ctx");
    int openLevels = (Integer) request.getAttribute("openLevels");
%>

<%
    if (itemToDisplay != null) {
%>

<div id="<portlet:namespace/>navtree" class="jstree-nav">
    <%
        displayItem(out, itemToDisplay, 0, openLevels);
    %>
</div>

<%
    }
%>



</div>





