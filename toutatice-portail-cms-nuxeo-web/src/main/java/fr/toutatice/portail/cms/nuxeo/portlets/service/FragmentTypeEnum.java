package fr.toutatice.portail.cms.nuxeo.portlets.service;


/**
 * 
 * Liste des types de fragments possibles
 *
 */
public enum FragmentTypeEnum {

    // /** Contenu riche */
    // html("fgt.html", "toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_"),
    //
    // /** Liste de notes */
    // liste("fgt.liste", "toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_");
    //
    // private String pmFragmentType;
    //
    // private String porletInstance;
    //
    // private String prefixeIdFrag;
    //
    // private FragmentTypeEnum(String pmFragmentType, String porletInstance, String prefixeIdFrag) {
    // this.pmFragmentType = pmFragmentType;
    // this.porletInstance = porletInstance;
    // this.prefixeIdFrag = prefixeIdFrag;
    // }
    //
    // /**
    // * @return the pmFragmentType
    // */
    // public String getPmFragmentType() {
    // return pmFragmentType;
    // }
    //
    // /**
    // * @return the porletInstance
    // */
    // public String getPorletInstance() {
    // return porletInstance;
    // }
    //
    // /**
    // * @return the prefixeIdFrag
    // */
    // public String getPrefixeIdFrag() {
    // return prefixeIdFrag;
    // }
    //
    // /**
    // * Valider un type de fragment possible
    // * @param fragmentType
    // * @return true si fragment existe
    // */
    // public static FragmentTypeEnum findByName(String propType) {
    // for(FragmentTypeEnum ft : FragmentTypeEnum.values()) {
    // if(propType.equals(ft.getPmFragmentType())){
    // return ft;
    // }
    // }
    //
    // return null; // Par défaut
    // }
    //
    // /**
    // * Retourner un nouveau porlet en fonction du type de fragment souhaité
    // * @param id identifiant de la portlet dans la page
    // * @param fp le type de portlet
    // * @param portletProps ses propriétés
    // * @return la fenetre éditable avec le portlet instancié
    // */
    // public CMSEditableWindow createNewFragmentWindow(int id, Map<String, String> portletProps) {
    // return new CMSEditableWindow(prefixeIdFrag.concat(Integer.toString(id)), porletInstance, portletProps);
    // }
}
