package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.List;

import fr.toutatice.portail.core.nuxeo.ListTemplate;

/**
 * Ce customizer peut être redéfini dans la webapp surchargée
 * 
 * @author jeanseb
 *
 */
public class ListTemplatesHandler extends DefaultListTemplatesHandler {
	
	public List<ListTemplate> getListTemplates()	{
		return super.getListTemplates();
	}

}
