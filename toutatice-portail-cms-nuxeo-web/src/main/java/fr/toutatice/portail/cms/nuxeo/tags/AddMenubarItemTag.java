package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.BooleanUtils;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.IMenubarService;
import org.osivia.portal.api.menubar.MenubarContainer;
import org.osivia.portal.api.menubar.MenubarGroup;
import org.osivia.portal.api.menubar.MenubarItem;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Add menubar item tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class AddMenubarItemTag extends SimpleTagSupport {

    /** Menubar item identifier. */
    private String id;
    /** Menubar item label internationationalization property key. */
    private String labelKey;
    /** Menubar item order value. */
    private Integer order;
    /** Menubar item URL. */
    private String url;
    /** Menubar item onclick event. */
    private String onclick;
    /** Menubar item HTML class. */
    private String htmlClass;
    /** Menubar item URL target. */
    private String target;
    /** Menubar item glyphicon. */
    private String glyphicon;
    /** Menubar item AJAX indicator. */
    private Boolean ajax;
    /** Menubar item dropdown parent. */
    private String dropdown;
    /** Menubar item data attributes. */
    private Map<String, String> data;

    /** Menubar service. */
    private final IMenubarService menubarService;
    /** Bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Default constructor.
     */
    public AddMenubarItemTag() {
        super();

        // Menubar service
        this.menubarService = Locator.findMBean(IMenubarService.class, IMenubarService.MBEAN_NAME);

        // Bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doTag() throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();
        // Nuxeo controller
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute("nuxeoController");
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());
        // Menubar
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

        if (menubar != null) {
            // Title
            String title = bundle.getString(this.labelKey);

            // Parent
            MenubarContainer parent = null;
            if ((this.dropdown != null) && (nuxeoController != null)) {
                PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
                parent = this.menubarService.getDropdown(portalControllerContext, this.dropdown);
            }
            if (parent == null) {
                parent = MenubarGroup.SPECIFIC;
            }

            // Order int value
            int orderInt;
            if (this.order == null) {
                orderInt = 0;
            } else {
                orderInt = this.order.intValue();
            }

            // Menubar item
            MenubarItem item = new MenubarItem(this.id, title, this.glyphicon, parent, orderInt, this.url, this.target, this.onclick,
                    this.htmlClass);
            item.setAjaxDisabled(BooleanUtils.isFalse(this.ajax));
            if (this.data != null) {
                item.getData().putAll(this.data);
            }

            menubar.add(item);
        }
    }


    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter for id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for labelKey.
     *
     * @return the labelKey
     */
    public String getLabelKey() {
        return this.labelKey;
    }

    /**
     * Setter for labelKey.
     *
     * @param labelKey the labelKey to set
     */
    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    /**
     * Getter for order.
     *
     * @return the order
     */
    public Integer getOrder() {
        return this.order;
    }

    /**
     * Setter for order.
     *
     * @param order the order to set
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Getter for url.
     *
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Setter for url.
     *
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter for onclick.
     *
     * @return the onclick
     */
    public String getOnclick() {
        return this.onclick;
    }

    /**
     * Setter for onclick.
     *
     * @param onclick the onclick to set
     */
    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    /**
     * Getter for htmlClass.
     *
     * @return the htmlClass
     */
    public String getHtmlClass() {
        return this.htmlClass;
    }

    /**
     * Setter for htmlClass.
     *
     * @param htmlClass the htmlClass to set
     */
    public void setHtmlClass(String htmlClass) {
        this.htmlClass = htmlClass;
    }

    /**
     * Getter for target.
     *
     * @return the target
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Setter for target.
     *
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Getter for glyphicon.
     *
     * @return the glyphicon
     */
    public String getGlyphicon() {
        return this.glyphicon;
    }

    /**
     * Setter for glyphicon.
     *
     * @param glyphicon the glyphicon to set
     */
    public void setGlyphicon(String glyphicon) {
        this.glyphicon = glyphicon;
    }

    /**
     * Getter for ajax.
     *
     * @return the ajax
     */
    public Boolean getAjax() {
        return this.ajax;
    }

    /**
     * Setter for ajax.
     *
     * @param ajax the ajax to set
     */
    public void setAjax(Boolean ajax) {
        this.ajax = ajax;
    }

    /**
     * Getter for dropdown.
     *
     * @return the dropdown
     */
    public String getDropdown() {
        return this.dropdown;
    }

    /**
     * Setter for dropdown.
     *
     * @param dropdown the dropdown to set
     */
    public void setDropdown(String dropdown) {
        this.dropdown = dropdown;
    }

    /**
     * Getter for data.
     *
     * @return the data
     */
    public Map<String, String> getData() {
        return this.data;
    }

    /**
     * Setter for data.
     *
     * @param data the data to set
     */
    public void setData(Map<String, String> data) {
        this.data = data;
    }

}
