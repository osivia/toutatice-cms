package fr.toutatice.application.portail.ficheApplication;

import fr.toutatice.outils.ldap.entity.Profil;


public class ProfilUrl implements Comparable{
	
	private Profil profil;
	private String url;
	private boolean clicable;
	
	public Profil getProfil() {
		return profil;
	}
	public void setProfil(Profil profil) {
		this.profil = profil;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isClicable() {
		return clicable;
	}
	public void setClicable(boolean clicable) {
		this.clicable = clicable;
	}
	
	public ProfilUrl(Profil profil, String url, boolean clicable) {
		super();
		this.profil = profil;
		this.url = url;
		this.clicable = clicable;
	}

	public int compareTo(Object obj) {
		ProfilUrl p = (ProfilUrl) obj;
		return this.getProfil().getDescription().toLowerCase().compareTo(p.getProfil().getDescription().toLowerCase());
	}
	
}
