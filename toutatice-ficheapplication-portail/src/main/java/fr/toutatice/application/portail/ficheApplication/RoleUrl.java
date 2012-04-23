package fr.toutatice.application.portail.ficheApplication;

import fr.toutatice.outils.ldap.entity.RoleApplicatif;

public class RoleUrl implements Comparable{

	private RoleApplicatif roleApplicatif;
	private String url;
	private boolean clicable;

	
	
	public RoleUrl(RoleApplicatif role, String url, boolean clicable) {
		super();
		this.roleApplicatif = role;
		this.url = url;
		this.clicable = clicable;
	}

	public RoleApplicatif getRoleApplicatif() {
		return roleApplicatif;
	}

	public String getUrl() {
		return url;
	}
	
	

	public boolean isClicable() {
		return clicable;
	}

	public int compareTo(Object obj) {
		RoleUrl p = (RoleUrl) obj;
		return this.getRoleApplicatif().getDescription().toLowerCase().compareTo(p.getRoleApplicatif().getDescription().toLowerCase());
	}
}
