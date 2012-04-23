package fr.toutatice.application.portail.ficheApplication;

import java.util.ArrayList;
import java.util.List;

import fr.toutatice.outils.ldap.entity.Application;
import fr.toutatice.outils.ldap.entity.Person;
import fr.toutatice.outils.ldap.entity.Profil;
import fr.toutatice.outils.ldap.entity.RoleApplicatif;


public class FicheApplication {
	

	private Application application;
	private String filtreProfil;
	private String filtreManager;
	private List<Profil> listeProfilsAjout = new ArrayList<Profil>();
	private List<String> listeManagersAjout = new ArrayList<String>();
	private String filtreRne="";
	private String typeManager="";
	
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application application) {
		this.application = application;
	}

	public String getFiltreProfil() {
		return filtreProfil;
	}
	public void setFiltreProfil(String filtreProfil) {
		this.filtreProfil = filtreProfil;
	}
	public List<Profil> getListeProfilsAjout() {
		return listeProfilsAjout;
	}
	public void setListeProfilsAjout(List<Profil> listeProfilsAjout) {
		this.listeProfilsAjout = listeProfilsAjout;
	}
	public String getFiltreRne() {
		return filtreRne;
	}
	public void setFiltreRne(String filtreRne) {
		this.filtreRne = filtreRne;
	}
	public List<String> getListeManagersAjout() {
		return listeManagersAjout;
	}
	public void setListeManagersAjout(List<String> listeManagersAjout) {
		this.listeManagersAjout = listeManagersAjout;
	}
	public String getFiltreManager() {
		return filtreManager;
	}
	public void setFiltreManager(String filtreManager) {
		this.filtreManager = filtreManager;
	}
	public String getTypeManager() {
		return typeManager;
	}
	public void setTypeManager(String typeManager) {
		this.typeManager = typeManager;
	}
	
	
	
}
