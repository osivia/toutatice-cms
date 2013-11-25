package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;



/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class WebConfiguratinQueryCommand implements INuxeoCommand {



    /** Le domaine du site courant. */
    private String domainPath;

    /** Le type de configuration cherché. */
    private WebConfigurationType type;

    /** Types de configuration possible. */
    public enum WebConfigurationType {
        CMSNavigationAdapter, CMSPlayer, CMSToWebPathAdapter, extraRequestFilter;
    }
    
    /** Schémas. */
    public final static String basicNavigationSchemas = "dublincore,common,toutatice,webconfiguration";
	
    /**
     * 
     * @param domainPath
     * @param type
     */
    public WebConfiguratinQueryCommand(String domainPath, WebConfigurationType type) {
		super();

        this.domainPath = domainPath;
        this.type = type;
	}
	
	public Object execute(Session session) throws Exception {
		
		OperationRequest request;

		request = session.newRequest("Document.Query");

        String nuxeoRequest = "( ecm:path STARTSWITH '" + domainPath + "'  " + "AND  (wconf:type = '" + type.toString() + "') AND (wconf:enabled=1) )";
		

        request.set("query", "SELECT * FROM Document WHERE " + nuxeoRequest + " ORDER BY wconf:order");
		

		String navigationSchemas = basicNavigationSchemas;

		request.setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas);


        Documents configurations = (Documents) request.execute();
        return configurations;
		

	}

	public String getId() {

        return "WebConfiguratinQueryCommand/" + domainPath + "/" + type.toString();
	};

}
