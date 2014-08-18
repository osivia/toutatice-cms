<%@ page import="org.osivia.portal.api.Constants"%>
<%@ page import="org.osivia.portal.api.menubar.MenubarItem"%>
<%@ page import="java.util.List"%>


<%
String permaLinkURL = (String) request.getAttribute("permaLinkURL");
String rssLinkURL = (String) request.getAttribute("rssLinkURL");


// Menu items can be customized by jsp duplication
@SuppressWarnings("unchecked")
List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

if (permaLinkURL != null) {
    MenubarItem item = new MenubarItem("PERMLINK", "Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC, permaLinkURL, null,
            "portlet-menuitem-permalink", null);
    item.setGlyphicon("link");
    item.setAjaxDisabled(true);
    menuBar.add(item);
}

if (rssLinkURL != null) {
    MenubarItem item = new MenubarItem("RSS", "RSS", MenubarItem.ORDER_PORTLET_SPECIFIC + 2, rssLinkURL, null, "portlet-menuitem-rss", null);
    item.setGlyphicon("social rss");
    item.setAjaxDisabled(true);
    menuBar.add(item);
}
%>
