package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.osivia.portal.api.Constants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;

/**
 * Add menubar item tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class AddMenubarItemTag extends SimpleTagSupport {

    /** Bundle factory. */
    private static final IBundleFactory BUNDLE_FACTORY = getBundleFactory();

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


    /**
     * Default constructor.
     */
    public AddMenubarItemTag() {
        super();
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
        // Bundle
        Bundle bundle = BUNDLE_FACTORY.getBundle(request.getLocale());
        // Menubar
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

        if (menubar != null) {
            // Title
            String title = bundle.getString(this.labelKey);

            // Order int value
            int orderInt;
            if (this.order == null) {
                orderInt = 0;
            } else {
                orderInt = this.order.intValue();
            }

            // Menubar item
            MenubarItem item = new MenubarItem(this.id, title, orderInt, this.url, this.onclick, this.htmlClass, this.target);
            item.setGlyphicon(this.glyphicon);

            menubar.add(item);
        }
    }


    /**
     * Get bundle factory.
     *
     * @return bundle factory
     */
    private static IBundleFactory getBundleFactory() {
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        return internationalizationService.getBundleFactory(GetFileSizeTag.class.getClassLoader());
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

}
