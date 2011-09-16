package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.util.List;

public class NXQLFormater {
	
	public String formatTextSearch( String fieldName, List<String> searchValue){
		
		StringBuffer request = new StringBuffer();
		request.append("(");
		
		boolean firstItem = true;
		
		for( String searchWord : searchValue)	{
			
			if( !firstItem)
				request.append(" OR ");

			request.append( fieldName+" ILIKE \"%" + searchWord + "%\"");
			
			firstItem = false;
		}
		request.append(")");
		
		return request.toString();
	}
	
	public String formatVocabularySearch( String fieldName, List<String> searchValue){
		
		StringBuffer request = new StringBuffer();
		request.append("(");
		
		boolean firstItem = true;
		
		for( String searchWord : searchValue)	{
			
			if( !firstItem)
				request.append(" OR ");

			request.append( fieldName+" STARTSWITH '" + searchWord + "'");
			
			firstItem = false;
		}
		request.append(")");
		
		return request.toString();
	}	

}
