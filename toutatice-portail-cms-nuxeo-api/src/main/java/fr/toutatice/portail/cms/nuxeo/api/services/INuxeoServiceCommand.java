package fr.toutatice.portail.cms.nuxeo.api.services;

import org.nuxeo.ecm.automation.client.Session;

public interface INuxeoServiceCommand {
	
	public Object execute( Session nuxeoSession) throws Exception;
	
	// Permet d'identifier de manière unique la commande
	// notamment pour la gestion de cache
	// doit inclure les paramètres spécifiques à la commande
	
	public String getId();
}
