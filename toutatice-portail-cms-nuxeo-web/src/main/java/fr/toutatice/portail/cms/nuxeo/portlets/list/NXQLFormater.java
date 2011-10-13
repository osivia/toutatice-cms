package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.util.List;
import java.util.StringTokenizer;

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
	
	public String formatDateSearch( String fieldName, List<String> searchValue)
	{
		String delimFrontend = "/";
		String delimBackend = "-";
		String jj = "";
		String mm = "";
		String aaaa = "";
		
		StringTokenizer st = new StringTokenizer(searchValue.get(0), delimFrontend);
		jj = st.nextToken();
		mm = st.nextToken();
		aaaa = st.nextToken();
		String paramFrom = aaaa + delimBackend + mm + delimBackend + jj;
		
		st = new StringTokenizer(searchValue.get(1), delimFrontend);
		jj = st.nextToken();
		mm = st.nextToken();
		aaaa = st.nextToken();
		String paramTo = aaaa + delimBackend + mm + delimBackend + jj;

		StringBuffer request = new StringBuffer();
		request.append("(");
		request.append( fieldName + " BETWEEN DATE '" + paramFrom + "' AND DATE '"+ paramTo +"' ");
		request.append(")");
		
		return request.toString();
	}

}
