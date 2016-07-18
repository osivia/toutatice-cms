package fr.toutatice.portail.cms.nuxeo.api.forms;

import org.apache.commons.lang.StringUtils;

/**
 * FormFilterInstance
 * 
 * @author dorian
 */
public class FormFilterInstance implements Comparable<FormFilterInstance> {

    /** formFilter */
    private FormFilter formFilter;

    /** path */
    private String path;

    /** name */
    private String name;

    /**
     * @param formFilter
     * @param path
     * @param name
     */
    public FormFilterInstance(FormFilter formFilter, String path, String name) {
        setFormFilter(formFilter);
        setPath(path);
        setName(name);
    }

    @Override
    public int compareTo(FormFilterInstance comparedFormFilterInstance) {

        String indexS = StringUtils.substringAfterLast(getPath(), ",");
        int index = Integer.parseInt(indexS);
        String compIndexS = StringUtils.substringAfterLast(comparedFormFilterInstance.getPath(), ",");
        int compIndex = Integer.parseInt(compIndexS);

        return Integer.compare(index, compIndex);
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the form filter
     */
    public FormFilter getFormFilter() {
        return formFilter;
    }

    /**
     * @param formFilter
     */
    public void setFormFilter(FormFilter formFilter) {
        this.formFilter = formFilter;
    }
}
