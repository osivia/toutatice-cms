package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Date;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DuplicateCommand implements INuxeoCommand {

	private String sourcePath;
	private String targetPath;
	
	public DuplicateCommand(String sourcePath, String targetPath) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		
	}

	@Override
	public Object execute(Session nuxeoSession) throws Exception {
		
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        // Source
        DocRef source = new PathRef(this.sourcePath);
        // Target
        DocRef target = new PathRef(this.targetPath);

        Document copy = documentService.copy(source, target);
        PropertyMap properties = new PropertyMap();
        properties.set("dc:title", copy.getTitle()+ "*");
        
        long time = new Date().getTime();
        
        properties.set("ttc:webid", Long.toString(time));
        properties.set("ottcweb:segment", Long.toString(time));
        
        copy = documentService.update(copy, properties);
        return copy;
	}

	@Override
	public String getId() {
		return null;
	}

}
