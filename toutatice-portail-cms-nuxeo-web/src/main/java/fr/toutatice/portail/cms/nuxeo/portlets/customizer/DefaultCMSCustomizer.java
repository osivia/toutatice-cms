package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.ArrayList;
import java.util.List;



public class DefaultCMSCustomizer {
	
	public static final String STYLE_MINI = "mini";
	public static final String STYLE_NORMAL = "normal";
	public static final String STYLE_DETAILED = "detailed";
	public static final String STYLE_EDITORIAL = "editorial";
	
	public static final String DEFAULT_SCHEMAS =  "dublincore,common, toutatice";	

	
	public static List<ListTemplate> getListTemplates()	{
		
		List<ListTemplate> templates = new ArrayList<ListTemplate>();
		templates.add( new ListTemplate(STYLE_MINI, "Minimal [titre]", DEFAULT_SCHEMAS));
		templates.add( new ListTemplate(STYLE_NORMAL, "Normal [titre, date]", DEFAULT_SCHEMAS));
		templates.add( new ListTemplate(STYLE_DETAILED, "Détaillé [titre, description, date, ...]", DEFAULT_SCHEMAS));
		templates.add( new ListTemplate(STYLE_EDITORIAL, "Editorial [vignette, titre, description]", DEFAULT_SCHEMAS));
		
		return templates;
	}
	
	
	public static String getSearchSchema()	{
		
		return "dublincore,common";
		
	}
	
	
	
	
}
