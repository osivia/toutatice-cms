package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.util.Comparator;
import java.util.Map;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

/**
 * Comparateur de documents Nuxeo en fnoction d'un ordre passé en paramètre
 *
 */
public class DocumentComparator implements Comparator<Document> {

	/** ordre défini par l'utilisateur */
	private Map<String, Integer> documentOrder;
	
	public DocumentComparator(Map<String, Integer> documentOrder) {
		this.documentOrder = documentOrder;
	}
	
	public int compare(Document docA, Document docB) {
		Integer a = documentOrder.get(docA.getPath());
		Integer b = documentOrder.get(docB.getPath());
		
		return a.compareTo(b);
	}
}
