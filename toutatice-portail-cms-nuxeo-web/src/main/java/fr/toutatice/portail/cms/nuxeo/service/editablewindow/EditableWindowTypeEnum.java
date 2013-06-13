package fr.toutatice.portail.cms.nuxeo.service.editablewindow;


/**
 * 
 * Liste des types de fragments possibles
 *
 */
public enum EditableWindowTypeEnum {

	/** Contenu riche */
	html("fgt.html", "toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_",HtmlEWService.getInstance()),
	
	/** Liste de notes */
	liste("fgt.liste", "toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_",ListEWService.getInstance()),

	/** Portlet metier */
	portlet("fgt.portlet", null, "portlet_Frag_", PortletEWService.getInstance());	
	
    /** Identifiant type de fgt nuxeo */
	private String pmFragmentType;
	
    /** Identifiant JBoss de l'instance de portlet */
	private String porletInstance;
	
    /** préfix de la window */
	private String prefixeIdFrag;

    /** Instance du service associé au type de fragment */
	private EditableWindowService service;
	
	private EditableWindowTypeEnum(String pmFragmentType, String porletInstance, String prefixeIdFrag, EditableWindowService service) {
		this.pmFragmentType = pmFragmentType;
		this.porletInstance = porletInstance;
		this.prefixeIdFrag = prefixeIdFrag;
		
		service.setType(this);
		this.service = service;
	}

	/**
	 * @return the pmFragmentType
	 */
	public String getPmFragmentType() {
		return pmFragmentType;
	}

	/**
	 * param portletInstance the porletInstance
	 */
	public void setPorletInstance(String portletInstance) {
		if(porletInstance == null) {
			this.porletInstance = portletInstance;
		}
	}
	
	/**
	 * @return the porletInstance
	 */
	public String getPorletInstance() {
		return porletInstance;
	}
	
	
	/**
	 * @return the prefixeIdFrag
	 */
	public String getPrefixeIdFrag() {
		return prefixeIdFrag;
	}

	/**
	 * @return the service
	 */
	public EditableWindowService getService() {
		return service;
	}

	/**
	 * Valider un type de fragment possible
	 * @param fragmentType
	 * @return true si fragment existe
	 */
	public static EditableWindowTypeEnum findByName(String propType) {
		for(EditableWindowTypeEnum ew : EditableWindowTypeEnum.values()) {
			if(propType.equals(ew.getPmFragmentType())){
				return ew;
			}
		}
		
		return null; // Par défaut
	}
}
	

