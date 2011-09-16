package fr.toutatice.portail.cms.nuxeo.api;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PageSelectors {
	
	public static String encodeProperties( Map <String, List<String>> props)	{
		
		try	{
		String url = "";
		
		for( String name : props.keySet())	{
			
			List<String> values = props.get(name);
			
			if( values != null && values.size() > 0) {

					if (url.length() > 0)
						url += "&";

					url += encodeValue(name);

					// Encode values
					String encodedValues = "";
					for (String value : values) {
						if (encodedValues.length() > 0)
							encodedValues += ",";
						encodedValues += encodeValue(value);
					}

					url += "=" + encodedValues;
			}
		}
		
		return URLEncoder.encode(url , "UTF-8");
		
		} catch( Exception e)	{
			throw new RuntimeException( e);
		}
	}
	
	public static Map<String,List<String>> decodeProperties( String urlParams)	{
		try	{
			
		Map<String, List<String>> params = new HashMap<String, List<String>>();		
		
		if( urlParams == null || urlParams.length() == 0)
				return params;
			
		
		String decodedParam = URLDecoder.decode(urlParams , "UTF-8");
		
		
		String[] tabParams = decodedParam.split("&");	
		
		for(int i=0; i< tabParams.length; i++){
			String[] valParams = tabParams[i].split("=");
			
			if( valParams.length != 2)
				throw new IllegalArgumentException("Bad parameter format");
			
			String[] values = valParams[1].split(",");
			List<String> decodedValues = new ArrayList<String>();
			for( int j=0; j< values.length; j++)
				decodedValues.add( decodeValue( values[ j]));
			params.put(valParams[0], decodedValues);
		}
		
		return params;
		
		} catch( Exception e)	{
			throw new RuntimeException( e);
		}

	}

	private static String ESC_EQUALS = "##EQUALS##";
	private static String ESC_AMP = "##AMP##";	
	private static String ESC_COMMA = "##COMMA##";
	
	private static String encodeValue( String origValue)	{
		
		String res = origValue.replaceAll("=", ESC_EQUALS);
		res = res.replaceAll("&", ESC_AMP);
		res = res.replaceAll(",",  ESC_COMMA);		
		
		return res;
				
		
	}
	
	private static String decodeValue( String origValue)	{
		
		String res = origValue.replaceAll( ESC_EQUALS, "=");
		res = res.replaceAll(ESC_AMP, "&");
		res = res.replaceAll(ESC_COMMA, ",");		
		
		return res;
				
		
	}


}
