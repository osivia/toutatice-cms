package fr.toutatice.portail.cms.nuxeo.api;


import java.util.List;
import java.util.Map;

import org.osivia.portal.api.page.PageParametersEncoder;





public class PageSelectors {
	
	public static String encodeProperties( Map <String, List<String>> props)	{
		
		return PageParametersEncoder.encodeProperties(props);
	}
	
	public static Map<String,List<String>> decodeProperties( String urlParams)	{
		return PageParametersEncoder.decodeProperties(urlParams);
		

	}

	
}
