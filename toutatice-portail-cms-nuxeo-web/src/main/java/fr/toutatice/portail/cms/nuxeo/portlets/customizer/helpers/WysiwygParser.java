package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.io.File;
import java.io.InputStream;

import javax.xml.transform.Templates;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class WysiwygParser {

	public static java.net.URL xslfilepath = WysiwygParser.class.getClassLoader().getResource("html.xsl");
	private static Log log = LogFactory.getLog(WysiwygParser.class);


	private Templates template;


	public WysiwygParser(Templates template) {
		super();
		this.template = template;
	}
	

	public Templates getTemplate() {
		return template;
	}


	public XMLReader getParser() throws Exception  {

			//TODO : gérer un pool
		XMLReader parser = XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
		
		return parser;
		
	}



	static private synchronized WysiwygParser createInstance() {

		try {
			TransformerFactory fabriqueT = TransformerFactory.newInstance();
			
			InputStream is = WysiwygParser.class.getResourceAsStream("/html.xsl");

			// Use the factory to create a template containing the xsl file
			StreamSource stylesource = new StreamSource(is);

			Templates template = fabriqueT.newTemplates(stylesource);
			
			instance = new WysiwygParser( template);
			
			return instance;
		} catch (Exception tce) {
			log.error(tce);
		}
		
		return null;

	}

	private static WysiwygParser instance;

	static public WysiwygParser getInstance() {
		if (null == instance) {
			instance = createInstance();
		}
		return instance;
	}

}
