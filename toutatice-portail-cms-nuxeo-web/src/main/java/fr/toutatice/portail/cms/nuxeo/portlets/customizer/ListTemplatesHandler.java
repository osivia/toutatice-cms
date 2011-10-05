package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.List;

import fr.toutatice.portail.core.nuxeo.ListTemplate;

/**
 * Ce customizer permet de définir de nouveaux templates.
 * 
 * Le template d'affichage est WEB-INF/jsp/liste/view-[nom-du-template].jsp
 * 
 * @author jeanseb
 *
 */
public class ListTemplatesHandler extends DefaultListTemplatesHandler {
	
	public List<ListTemplate> getListTemplates()	{
		return super.getListTemplates();
	}

}
