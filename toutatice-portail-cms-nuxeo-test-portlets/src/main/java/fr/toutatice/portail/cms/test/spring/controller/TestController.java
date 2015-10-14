package fr.toutatice.portail.cms.test.spring.controller;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import fr.toutatice.portail.cms.test.common.TestPortlet;
import fr.toutatice.portail.cms.test.common.model.Tab;

/**
 * Test SpringFramework portlet.
 *
 * @author Cédric Krommenhoek
 * @see TestPortlet
 */
@Controller
@RequestMapping(value = "VIEW")
public class TestController extends TestPortlet {

    /**
     * Constructor.
     */
    public TestController() {
        super();
    }


    /**
     * View render mapping.
     *
     * @param request render request
     * @param response render response
     * @param tab current tab request parameter, may be null
     * @return view path
     */
    @RenderMapping
    public String view(RenderRequest request, RenderResponse response, @RequestParam(value = "tab", required = false) String tab) {
        // Current tab
        Tab currentTab = Tab.fromId(request.getParameter("tab"));

        return currentTab.getId();
    }


    /**
     * Get tabs model attribute.
     *
     * @param request portlet request
     * @param response portlet response
     * @return tabs
     */
    @ModelAttribute(value = "tabs")
    public Tab[] getTabs(PortletRequest request, PortletResponse response) {
        return Tab.values();
    }

}
