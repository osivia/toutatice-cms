package fr.toutatice.portail.cms.nuxeo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cette Valve permet d'introduire un délai dans les requetes Nuxeo afin de
 * valider le comportement du portail
 * 
 * A déclarer dans /opt/nuxeo/templates/default/conf/server.xml
 *      <Valve className="fr.toutatice.portail.cms.nuxeo.DelayValve" filePath="/opt/nuxeo/delay.cfg"/>
 * Le délai peut alors être modifié dans le fichier /opt/nuxeo/delay.cfg
 *      Par exemple, "3" pour 3 secondes de délai
 * 
 */
public class DelayValve extends ValveBase {

	protected String filePath;

	private static Log logger = LogFactory.getLog(DelayValve.class);

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void invoke(Request request, Response response) throws IOException, ServletException, IllegalStateException {

		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(getFilePath());
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();
			// Print the content on the console

			long delai = Long.parseLong(strLine);
			
			String requestString = "";
			
			while( request.getParameterNames().hasMoreElements())	{
				String name = (String) request.getParameterNames().nextElement();
				
				requestString += "["+name+"," +request.getParameter(name)+"]";
			}

//			if( request.getRequestURI().startsWith("/nuxeo/site/automation/Document.PageProvider"))	{
			logger.info("--> " + request.getRequestURI());
//			logger.info("Waiting " + delai + " seconds");
//			Thread.sleep(delai * 1000);
//			}

			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			// No file
		} 

		this.getNext().invoke(request, response);
	}

}
