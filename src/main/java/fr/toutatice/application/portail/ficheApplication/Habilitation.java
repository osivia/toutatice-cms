package fr.toutatice.application.portail.ficheApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.toutatice.outils.ldap.entity.Application;
import fr.toutatice.outils.ldap.entity.Person;
import fr.toutatice.outils.ldap.exception.ToutaticeAnnuaireException;

public class Habilitation {

	protected static final Log logger = LogFactory
			.getLog("fr.toutatice.services");

	public enum level {
		LECTEUR, GESTIONNAIRE, ADMINISTRATEUR, NONHABILITE;
		public String getString() {
			return this.name();
		}
	}

	private String roleAdministrateur;
	private String roleGestionnaire;
	private String roleLecteur;

	private level role;

	public String getRoleAdministrateur() {
		return roleAdministrateur;
	}

	public void setRoleAdministrateur(String roleAdministrateur) {
		this.roleAdministrateur = roleAdministrateur;
	}

	public String getRoleGestionnaire() {
		return roleGestionnaire;
	}

	public void setRoleGestionnaire(String roleGestionnaire) {
		this.roleGestionnaire = roleGestionnaire;
	}

	public String getRoleLecteur() {
		return roleLecteur;
	}

	public void setRoleLecteur(String roleLecteur) {
		this.roleLecteur = roleLecteur;
	}

	public level getRole() {
		return role;
	}

	public void setRole(level role) {
		this.role = role;
	}
	
	public boolean isAdmin(Person user){
		if (user.hasRole(roleAdministrateur)) {
			return true;
		} else {
			return false;
		}
	}

	public level findRoleUser(Person user, Application app) {

		level role = level.NONHABILITE;

		if (user.hasRole(roleAdministrateur)) {
			role = level.ADMINISTRATEUR;
		} 
		else {
			if (app.isManagedBy(user)) {
				role = level.GESTIONNAIRE;
			} else {
				
				if (app.autorise(user)) {
					role = level.LECTEUR;
				}
				else {
					role = level.NONHABILITE;
				}
			}
		}

		return role;
	}

}
