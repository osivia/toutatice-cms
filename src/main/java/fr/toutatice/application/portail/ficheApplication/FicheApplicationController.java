package fr.toutatice.application.portail.ficheApplication;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.context.PortletContextAware;

import fr.toutatice.outils.ldap.entity.Application;
import fr.toutatice.outils.ldap.entity.Person;
import fr.toutatice.outils.ldap.entity.Profil;
import fr.toutatice.outils.ldap.entity.RoleApplicatif;
import fr.toutatice.outils.ldap.entity.Structure;
import fr.toutatice.outils.ldap.exception.ToutaticeAnnuaireException;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;




@Controller
@RequestMapping("VIEW")
@SessionAttributes({"ficheAppli","userConnecte","level","etbUserConnecte"})

public class FicheApplicationController implements PortletContextAware{

	protected static final Log logger = LogFactory.getLog("fr.toutatice.services");
	protected static final Log logModifLdap = LogFactory.getLog("fr.toutatice.annuaire.modif");

	private PortletContext portletContext;
	
	@Autowired
	private Application application;
	
	@Autowired
	private Person personne;
	
	@Autowired
	private RoleApplicatif roleApplicatif;
	
	@Autowired 
	private Profil profil;
	
	@Autowired 
	private Structure structure;
	
	@Autowired
	private Habilitation habilitation;
	
	private IPortalUrlFactory portalUrlFactory;

	int firstrow = 0;
	int firstrowManagerAjout = 0;
	int rowcount = 10;

	public Person initUserConnecte(ModelMap model, RenderRequest request) {
		String uid = request.getUserPrincipal().toString();
		Person userConnecte = personne.findUtilisateur(uid);
		model.addAttribute("userConnecte",userConnecte);
		return userConnecte;
	}
	
	public FicheApplication initFicheAppli(ModelMap model, RenderRequest request, PortletSession session, Person userConnecte) throws ToutaticeAnnuaireException {
		
		List<Structure> etbUserConnecte = new ArrayList<Structure>();
		for(String rne : userConnecte.getListeRnes()) {
			etbUserConnecte.add(structure.findStructure(rne)); 
		}
		Structure academie = new Structure();
		academie.setId("");
		academie.setDescription("Toute l'académie");
		etbUserConnecte.add(academie);
		model.addAttribute("etbUserConnecte",etbUserConnecte);
		
		FicheApplication ficheAppli = new FicheApplication();
		ficheAppli.setTypeManager("person");
		ficheAppli.setFiltreRne(etbUserConnecte.get(0).getId());
		String cnApplication = request.getParameter("cnApplication");
		if (cnApplication == null)
			{ cnApplication = " "; }
		
		Application app= new Application();
		try {
			app = application.findApplication(cnApplication);
		} catch (ToutaticeAnnuaireException e) {
			logger.error("Application "+cnApplication+" non trouvée dans l'annuaire");
			throw(e);
		}
		
		ficheAppli.setApplication(app);
				
		model.addAttribute("ficheAppli", ficheAppli);

		return ficheAppli;
		
	}
		
	
	@RequestMapping 	
	public String showFicheAppli(RenderRequest request, ModelMap model, PortletSession session) {
		
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			userConnecte = initUserConnecte(model, request);
		}
		String retour;
		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		try{
			if ( ficheAppli != null) {
				if (request.getParameter("cnApplication") != null) {
					ficheAppli = initFicheAppli(model, request, session, userConnecte);
				} 
			} else {
				ficheAppli = initFicheAppli(model, request, session, userConnecte);
			}
			Habilitation.level level = habilitation.findRoleUser(userConnecte, ficheAppli.getApplication());
			model.addAttribute("level",level);
			
			this.setListeProfilUrlModel(userConnecte, ficheAppli.getApplication(), request, model);
			this.setListeRoleUrlModel(userConnecte,ficheAppli.getApplication(), request, model);
			this.setListeManagersUrlModel(userConnecte,ficheAppli.getApplication(),request,model);
		
			
			if (level.equals(Habilitation.level.NONHABILITE)) {
				retour = "nonAutorise";
			}
			else {
				retour = "ficheApplication";
			}
		}catch(ToutaticeAnnuaireException e){
			logger.error("Erreur lors de l'affichage de la portlet de présentation d'une application");
			logger.error(e);
			retour = "erreur";
		}
			
		return retour;
	}

	
	@RequestMapping(params = "action=nonAutorise")
	public String showNonAutorise() {
		return "nonAutorise";
	}
	
	@RequestMapping(params = "action=erreur")
	public String showErreur() {
		return "erreur";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "action=gestionProfils") 
	public String showGestionProfils(PortletSession session, ModelMap model, RenderRequest request) {
		
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			initUserConnecte(model, request);
		}
		
		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		if ( ficheAppli == null) {
			logger.error("perte du contexte dans la portlet de gestion des applications. User concerné : " + userConnecte.getUid());
			return "erreur";
		} else {
		
			Habilitation.level level = (Habilitation.level) session.getAttribute("level");
			if(level == null) {
				level = habilitation.findRoleUser(userConnecte, ficheAppli.getApplication());
				model.addAttribute("level",level);
			}
			
			this.setListeProfilUrlModel(userConnecte, ficheAppli.getApplication(), request, model);
			
			request.setAttribute("firstrow", firstrow);
			request.setAttribute("rowcount", rowcount);	
			request.setAttribute("dernierePage", firstrow+rowcount < ((FicheApplication)model.get("ficheAppli")).getListeProfilsAjout().size());
	
			ficheAppli.setFiltreRne(((List<Structure>)model.get("etbUserConnecte")).get(0).getId());
			String retour;
			if (level.equals(Habilitation.level.NONHABILITE)) {
				retour = "nonAutorise";
			}
			else {
				if (level.equals(Habilitation.level.ADMINISTRATEUR) || level.equals(Habilitation.level.GESTIONNAIRE)){
					retour = "gestionProfils"; }
				else {
					retour = "ficheApplication";
				}
			}
			return retour;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "action=gestionManagers") 
	public String showGestionManagers(PortletSession session, ModelMap model, RenderRequest request) {
		
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			initUserConnecte(model, request);
		}
		
		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		if ( ficheAppli == null) {
			logger.error("perte du contexte dans la portlet de gestion des applications. User concerné : " + userConnecte.getUid());
			return "erreur";
		}
		else {
			Habilitation.level level = habilitation.findRoleUser(userConnecte, ficheAppli.getApplication());
			this.setListeManagersUrlModel(userConnecte, ficheAppli.getApplication(),request,model);
			this.setListeManagersAjoutUrlModel(userConnecte, ficheAppli.getListeManagersAjout(), request, model);

			request.setAttribute("firstrowManagerAjout", firstrowManagerAjout);
			request.setAttribute("rowcount", rowcount);	
			request.setAttribute("dernierePageManagerAjout", firstrowManagerAjout+rowcount < ((List<ManagerUrl>)model.get("listeManagersUrlAjout")).size());
			
			String retour;
			if (level.equals(Habilitation.level.NONHABILITE)) {
				retour = "nonAutorise";
			}
			else {
				if (level.equals(Habilitation.level.ADMINISTRATEUR) || level.equals(Habilitation.level.GESTIONNAIRE)){
					retour = "gestionManagers"; }
				else {
					retour = "ficheApplication";
				}
			}
			return retour;
		}
	}
	
	@ActionMapping(params="action=ajoutProfil") 
	public void ajoutProfil(@RequestParam String cn, ActionResponse response, ModelMap model, PortletSession session) {	
		
		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		Profil profilAjout = profil.findProfilByCn(cn);
		
		if (ficheAppli==null) {
			response.setRenderParameter("action", "ficheApplication");	
		} else {
			if (ficheAppli.getApplication().getListeProfils().contains(profilAjout.getDn())) {
				model.addAttribute("messageErreur","Ce profil est déjà associé à l'application");
			}
			else {
			
				ficheAppli.getApplication().addProfil(profilAjout.getDn());
				List<Profil> liste = new ArrayList<Profil>();
				for(Profil p : ficheAppli.getListeProfilsAjout()) {
					if(!p.getCn().equals(cn)) {
						liste.add(p);
					}
				}
				ficheAppli.setListeProfilsAjout(liste);
			}
			model.addAttribute("ficheAppli", ficheAppli);
			response.setRenderParameter("action", "gestionProfils");		
		}
	}
	
	@SuppressWarnings("unchecked")
	@ActionMapping(params="action=deleteProfil") 
	public void deleteProfil(@RequestParam String cn, ActionResponse response, ModelMap model, PortletSession session) {

		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		Profil profilDelete = profil.findProfilByCn(cn);
		
		if (ficheAppli==null) {
			response.setRenderParameter("action", "ficheApplication");	
		} else {
			String dnProfilASupprimer = profilDelete.getDn();
			List<String> liste = (List<String>)ficheAppli.getApplication().getListeProfils().clone();
			for(String dn : liste) {
				if (dn.equalsIgnoreCase(dnProfilASupprimer)) {
					ficheAppli.getApplication().removeProfil(dn);
				}
			}
			
			
			model.addAttribute("ficheAppli", ficheAppli);
			response.setRenderParameter("action", "gestionProfils");			
		}
	}
	
	@ActionMapping(params = "action=rechercherProfil") 	
	public void rechercherProfil(@ModelAttribute("ficheAppli") FicheApplication ficheAppli, ActionResponse response, ModelMap model, PortletSession session) {
		if(ficheAppli.getFiltreRne().equals("")&ficheAppli.getFiltreProfil().equals("")){
			model.addAttribute("messageErreur","Vous devez sélectionner un établissement ou saisir un filtre pour effectuer une recherche du profil");
			ficheAppli.setListeProfilsAjout(new ArrayList<Profil>());
		}
		else {
			List<Profil> liste = profil.findProfilByRneNom(ficheAppli.getFiltreRne(),ficheAppli.getFiltreProfil()) ;
			ficheAppli.setListeProfilsAjout(liste);
		}
		model.addAttribute("ficheAppli", ficheAppli);
		if(ficheAppli.getListeProfilsAjout().size()<1){
			model.addAttribute("messageInfo","La recherche n'a donné aucune résultat");
		}
		firstrow = 0;
		response.setRenderParameter("action", "gestionProfils");	
	}
	
	@ActionMapping(params="action=ajoutManager") 
	public void ajoutManager(@RequestParam String dnManager, ActionResponse response, ModelMap model, PortletSession session) {	
		
		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		
		if (ficheAppli==null) {
			response.setRenderParameter("action", "ficheApplication");	
		} else {
			if (ficheAppli.getApplication().getListeManagers().contains(personne.findFullDn(dnManager))
					|| ficheAppli.getApplication().getListeManagers().contains(dnManager)) {
				model.addAttribute("messageErreur","Cette personne est déjà manager de l'application");
			}
			else {
				ficheAppli.getApplication().addManager(dnManager);
				ficheAppli.getListeManagersAjout().remove(dnManager);
			}
			model.addAttribute("ficheAppli", ficheAppli);
			response.setRenderParameter("action", "gestionManagers");		
		}
	}
	
	@SuppressWarnings("unchecked")
	@ActionMapping(params="action=deleteManager") 
	public void deleteManager(@RequestParam String dnManager, ActionResponse response, ModelMap model, PortletSession session) {

		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		
		if (ficheAppli==null) {
			response.setRenderParameter("action", "ficheApplication");	
		} else {
			List<String> liste = (List<String>)ficheAppli.getApplication().getListeManagers().clone();
			for(String dn : liste) {
				if (dn.equalsIgnoreCase(dnManager)) {
					ficheAppli.getApplication().removeManager(dn);
				}
			}
			model.addAttribute("ficheAppli", ficheAppli);
			response.setRenderParameter("action", "gestionManagers");			
		}
	}
	
	@ActionMapping(params = "action=rechercherManager") 	
	public void rechercherManager(@ModelAttribute("ficheAppli") FicheApplication ficheAppli, ActionResponse response, ModelMap model, PortletSession session) {
		if(ficheAppli.getTypeManager().equals("")) {
			model.addAttribute("messageErreur","Vous devez sélectionner un type de manager pour la recherche : personne ou profil");
			ficheAppli.setListeManagersAjout(new ArrayList<String>());
		} else {
			
			if(ficheAppli.getFiltreRne().equals("")&ficheAppli.getFiltreManager().equals("")){
				model.addAttribute("messageErreur","Vous devez sélectionner un établissement ou saisir un filtre pour effectuer une recherche de personne");
				ficheAppli.setListeManagersAjout(new ArrayList<String>());
			}
			else {
				List<String> liste = new ArrayList<String>();
				List<Person> listePerson = personne.getPersonByNomIdRne(ficheAppli.getFiltreManager(),ficheAppli.getFiltreRne()) ;
				for(Person p : listePerson) {
					liste.add(p.findFullDn(p.getUid()));
				}
				List<Profil> listeProfil = profil.findProfilByRneNom(ficheAppli.getFiltreRne(), ficheAppli.getFiltreManager());
				for(Profil p : listeProfil) {
					liste.add(p.getDn());
				}
				ficheAppli.setListeManagersAjout(liste);
			}
		}
		if(ficheAppli.getListeManagersAjout().size()<1){
			model.addAttribute("messageInfo","La recherche n'a donné aucune résultat");
		}
		model.addAttribute("ficheAppli", ficheAppli);
		firstrowManagerAjout = 0;
		response.setRenderParameter("action", "gestionManagers");	
	}
	
	@ActionMapping(params="action=valider") 
	public void valider(ActionResponse response, ModelMap modelMap, PortletSession session) {
			
		FicheApplication ficheAppli = (FicheApplication) session.getAttribute("ficheAppli");
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		
		if(ficheAppli != null) {
		Application app = ficheAppli.getApplication();
			
			// Enregistrement dans l'annuaire
			try {
				app.updateAppli();
				modelMap.addAttribute("ficheAppli", ficheAppli);
				firstrow = 0;
				firstrowManagerAjout=0;
				ficheAppli.setFiltreProfil("");
				ficheAppli.setFiltreManager("");
				ficheAppli.setListeProfilsAjout(new ArrayList<Profil>());
				ficheAppli.setListeManagersAjout(new ArrayList<String>());
				ficheAppli.setTypeManager("person");
				response.setRenderParameter("action", "ficheApplication");	
				modelMap.addAttribute("messageInfo","Modifications effectuées");
				logModifLdap.info("L'utilisateur "+userConnecte.getUid()+" a modifié l'application "+app.getDn());
			} catch (ToutaticeAnnuaireException e) {
				modelMap.addAttribute("ficheAppli", ficheAppli);
				modelMap.addAttribute("messageErreur","Erreur lors de la validation des modifications, veuillez ré-essayer");
				logger.error("Erreur lors de la modification de l'application "+app.getId()+" par "+userConnecte.getUid());
				logger.error(e);
			}
			
			
		} else {
			modelMap.addAttribute("messageErreur","Erreur lors de la validation des modifications, veuillez ré-essayer");
			logger.error("Erreur dans la portlet de modification des applications : perte du contexte. Utilisateur concerné : "+userConnecte.getUid());
		}
	}
	
	
	
	@RequestMapping(params = "action=pageSuivanteAjoutProfils") 	
	public String pageSuivanteProfils(ModelMap model, RenderRequest request, PortletSession session) {
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			initUserConnecte(model, request);
		}
		FicheApplication ficheAppli = (FicheApplication) model.get("ficheAppli");
		if (ficheAppli==null) {
			return "ficheApplication";	
		} else {
			if (ficheAppli.getListeProfilsAjout().size() > firstrow +rowcount) {
				firstrow = firstrow + rowcount;
			}
			request.setAttribute("firstrow", firstrow);
			request.setAttribute("rowcount", rowcount);	
			request.setAttribute("dernierePage", firstrow+rowcount < ((FicheApplication)model.get("ficheAppli")).getListeProfilsAjout().size());
			this.setListeProfilUrlModel(userConnecte, ficheAppli.getApplication(), request, model);
			return "gestionProfils";		
		}
	}
	
	@RequestMapping(params = "action=pagePrecedenteAjoutProfils") 	
	public String pagePrecedenteProfils(RenderRequest request, ModelMap model, PortletSession session) {
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			initUserConnecte(model, request);
		}
		FicheApplication ficheAppli = (FicheApplication) model.get("ficheAppli");
		if (ficheAppli==null) {
			return "ficheApplication";	
		} else {
			if (firstrow - rowcount > 0) {
				firstrow = firstrow - rowcount;
			} else {
				firstrow = 0;
			}
			request.setAttribute("firstrow", firstrow);
			request.setAttribute("rowcount", rowcount);	
			request.setAttribute("dernierePage", firstrow+rowcount < ((FicheApplication)model.get("ficheAppli")).getListeProfilsAjout().size());
			this.setListeProfilUrlModel(userConnecte, ficheAppli.getApplication(), request, model);
			return "gestionProfils";
		}
	}
	
	@RequestMapping(params = "action=pageSuivanteAjoutManagers") 	
	public String pageSuivanteManagers(ModelMap model, RenderRequest request, PortletSession session) {
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			initUserConnecte(model, request);
		}
		FicheApplication ficheAppli = (FicheApplication) model.get("ficheAppli");
		if (ficheAppli==null) {
			return "ficheApplication";	
		} else {
			if (ficheAppli.getListeManagersAjout().size() > firstrowManagerAjout +rowcount) {
				firstrowManagerAjout = firstrowManagerAjout + rowcount;
			}
			request.setAttribute("firstrowManagerAjout", firstrowManagerAjout);
			request.setAttribute("rowcount", rowcount);	
			request.setAttribute("dernierePageManagerAjout", firstrowManagerAjout+rowcount < ((FicheApplication)model.get("ficheAppli")).getListeManagersAjout().size());
			this.setListeManagersAjoutUrlModel(userConnecte,ficheAppli.getListeManagersAjout(), request, model);
			this.setListeManagersUrlModel(userConnecte,ficheAppli.getApplication(),request,model);
			return "gestionManagers";		
		}
	}
	
	@RequestMapping(params = "action=pagePrecedenteAjoutManagers") 	
	public String pagePrecedenteManagers(RenderRequest request, ModelMap model, PortletSession session) {
		Person userConnecte = (Person) session.getAttribute("userConnecte");
		if (userConnecte == null) {
			initUserConnecte(model, request);
		}
		FicheApplication ficheAppli = (FicheApplication) model.get("ficheAppli");
		if (ficheAppli==null) {
			return "ficheApplication";	
		} else {
			if (firstrowManagerAjout - rowcount > 0) {
				firstrowManagerAjout = firstrowManagerAjout - rowcount;
			} else {
				firstrowManagerAjout = 0;
			}
			request.setAttribute("firstrowManagerAjout", firstrowManagerAjout);
			request.setAttribute("rowcount", rowcount);	
			request.setAttribute("dernierePageManagerAjout", firstrowManagerAjout+rowcount < ((FicheApplication)model.get("ficheAppli")).getListeManagersAjout().size());
			this.setListeManagersAjoutUrlModel(userConnecte,ficheAppli.getListeManagersAjout(), request, model);
			this.setListeManagersUrlModel(userConnecte, ficheAppli.getApplication(),request,model);
			return "gestionManagers";
		}
	}


	

	@SuppressWarnings("unchecked")
	private void setListeRoleUrlModel(Person userConnecte, Application app, RenderRequest request, ModelMap model) {
		List<RoleUrl> liste = new ArrayList<RoleUrl>();
		
		for(String dnRole : app.getListeRolesApplicatifs()) {
			RoleApplicatif r;
			try {
				r = roleApplicatif.findRoleByDn(dnRole);
					if (r!= null) {
						try {
							portalUrlFactory = (IPortalUrlFactory) portletContext.getAttribute("PortalUrlFactory");
							Map<String, String> windowProperties = new HashMap<String, String>();
							windowProperties.put("pia.ajaxLink", "1");
							windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
							Map<String, String> params = new HashMap<String, String>();
							params.put("dnRoleApplicatif", r.getDn());
							String url;
							url = portalUrlFactory.getExecutePortletLink(request,"toutatice-application-ficheroleapplicatif-portailPortletInstance",windowProperties, params);	
							boolean a = userConnecte.hasRole(r.getCn());
							boolean b = r.isManagedBy(userConnecte);
							boolean c = habilitation.isAdmin(userConnecte);
							liste.add(new RoleUrl(r,url,a||b||c));
						} catch (Exception e) {
							logger.error("portlet de gestion des applications. Erreur lors de la création du lien pour le role " + dnRole +" - Application : "+app.getId());
							logger.error("problème API portalURLFactory");
							liste.add(new RoleUrl(r,"",false));
						}	
					}
			} catch(ToutaticeAnnuaireException e) {
				logger.error("Le role "+dnRole+" n'a pas été retrouvé dans l'annuaire");
			}
		}
		Collections.sort(liste);
		model.addAttribute("listeRolesUrl", liste);
	}
	
	@SuppressWarnings("unchecked")
	private void setListeProfilUrlModel(Person userConnecte, Application app, RenderRequest request, ModelMap model) {
		List<ProfilUrl> liste = new ArrayList<ProfilUrl>();
	
		for(String dnProfil : app.getListeProfils()) {
			Profil p;
			p = profil.findProfilByDn(dnProfil);
			if(p!=null) {
				try {
					portalUrlFactory = (IPortalUrlFactory) portletContext.getAttribute("PortalUrlFactory");
					Map<String, String> windowProperties = new HashMap<String, String>();
					windowProperties.put("pia.ajaxLink", "1");
					windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
					Map<String, String> params = new HashMap<String, String>();
					params.put("cnProfil", p.getCn());
					String url = portalUrlFactory.getExecutePortletLink(request,"toutatice-identite-ficheprofil-portailPortletInstance",windowProperties, params);	
					boolean a = userConnecte.hasProfil(dnProfil);
					boolean b = p.isManagedBy(userConnecte);
					boolean c = habilitation.isAdmin(userConnecte);
					liste.add(new ProfilUrl(p,url,a||b||c));	
				} catch (Exception e) {
					logger.error("portlet de gestion des applications. Erreur lors de la création du lien pour le profil " + dnProfil +" - Application : "+app.getId());
					logger.error("problème API portalURLFactory");
					liste.add(new ProfilUrl(p,"",false));
				}
			}
		}
		Collections.sort(liste);
		model.addAttribute("listeProfilsUrl",liste);
	}
	
	@SuppressWarnings("unchecked")
	private void setListeManagersUrlModel(Person userConnecte, Application app, RenderRequest request, ModelMap model) {
		List<ManagerUrl> liste = new ArrayList<ManagerUrl>();
	
		for(String dnManager : app. getListeManagers()) {
			
			if(dnManager.substring(0, 4).equals("uid=")) {
				Person p; 
				p = personne.findPersonByDn(dnManager);
				if(p!=null) {
					try {
						portalUrlFactory = (IPortalUrlFactory) portletContext.getAttribute("PortalUrlFactory");
						Map<String, String> windowProperties = new HashMap<String, String>();
						windowProperties.put("pia.ajaxLink", "1");
						windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
						Map<String, String> params = new HashMap<String, String>();
						params.put("uidFichePersonne", p.getUid());
						String url = portalUrlFactory.getExecutePortletLink(request,"toutatice-identite-fichepersonne-portailPortletInstance",windowProperties, params);
						boolean a = userConnecte.getDn().toLowerCase().equals(dnManager.toLowerCase());
						boolean b = p.isManagedBy(userConnecte);
						boolean c = habilitation.isAdmin(userConnecte);
						boolean b1;
						if(habilitation.findRoleUser(userConnecte, app).equals(Habilitation.level.ADMINISTRATEUR)){
							b1=false;
						}else {
							b1=userConnecte.getDn().toLowerCase().equals(p.getDn().toLowerCase());
						}
						liste.add(new ManagerUrl(p.getCn(),p.getUid(), p.getDn(), url,a||b||c,b1));	
					} catch (Exception e) {
						logger.error("portlet de gestion des applications. Erreur lors de la création du lien pour le manager " + dnManager +" - Application : "+app.getId());
						logger.error("problème API portalURLFactory");
						boolean b1;
						if(habilitation.findRoleUser(userConnecte, app).equals(Habilitation.level.ADMINISTRATEUR)){
							b1=false;
						}else {
							b1=userConnecte.getDn().toLowerCase().equals(p.getDn().toLowerCase());
						}
						liste.add(new ManagerUrl(p.getCn(), p.getUid(), p.getDn(), "",false,b1));
					}
				}
			}
			if(dnManager.substring(0, 3).equals("cn=")) {
				Profil p;
				p = profil.findProfilByDn(dnManager);
				if(p!=null) {
					try {
						portalUrlFactory = (IPortalUrlFactory) portletContext.getAttribute("PortalUrlFactory");
						Map<String, String> windowProperties = new HashMap<String, String>();
						windowProperties.put("pia.ajaxLink", "1");
						windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
						Map<String, String> params = new HashMap<String, String>();
						params.put("cnProfil", p.getCn());
						String url = portalUrlFactory.getExecutePortletLink(request,"toutatice-identite-ficheprofil-portailPortletInstance",windowProperties, params);	
						boolean a = userConnecte.hasProfil(dnManager);
						boolean b = p.isManagedBy(userConnecte);
						boolean c = habilitation.isAdmin(userConnecte);
						liste.add(new ManagerUrl(p.getDescription(), p.getCn(), p.getDn(), url,a||b||c,false));	
					} catch (Exception e) {
						logger.error("portlet de gestion des applications. Erreur lors de la création du lien pour le manager " + dnManager +" - Application : "+app.getId());
						logger.error("problème API portalURLFactory");
						liste.add(new ManagerUrl(p.getDescription(), p.getCn(), p.getDn(),"",false,false));
					}
				}
			}
		}
		Collections.sort(liste);
		model.addAttribute("listeManagersUrl",liste);
	}
	
	@SuppressWarnings("unchecked")
	private void setListeManagersAjoutUrlModel(Person userConnecte, List<String> listeDnManagers, RenderRequest request, ModelMap model) {
		List<ManagerUrl> liste = new ArrayList<ManagerUrl>();
	
		for(String dnManager : listeDnManagers) {
			
			if(dnManager.substring(0, 4).equals("uid=")) {
				Person p; 
				p = personne.findPersonByDn(dnManager);
				if(p!=null) {
					try {
						portalUrlFactory = (IPortalUrlFactory) portletContext.getAttribute("PortalUrlFactory");
						Map<String, String> windowProperties = new HashMap<String, String>();
						windowProperties.put("pia.ajaxLink", "1");
						windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
						Map<String, String> params = new HashMap<String, String>();
						params.put("uidFichePersonne", p.getUid());
						String url = portalUrlFactory.getExecutePortletLink(request,"toutatice-identite-fichepersonne-portailPortletInstance",windowProperties, params);	
						boolean a = userConnecte.getDn().toLowerCase().equals(dnManager.toLowerCase());
						boolean b = p.isManagedBy(userConnecte);
						boolean c = habilitation.isAdmin(userConnecte);
						liste.add(new ManagerUrl(p.getCn(),p.getUid(),p.getDn(), url,a||b||c,false));	
					} catch (Exception e) {
						logger.error("portlet de gestion des applications. Erreur lors de la création du lien pour l'ajout du manager " + dnManager);
						logger.error("problème API portalURLFactory");
						liste.add(new ManagerUrl(p.getCn(),p.getUid(),p.getDn(), "",false,false));	
					}
				}
			}
			if(dnManager.substring(0, 3).equals("cn=")) {
				Profil p;
				p = profil.findProfilByDn(dnManager);
				if(p!=null) {
					try {
						portalUrlFactory = (IPortalUrlFactory) portletContext.getAttribute("PortalUrlFactory");
						Map<String, String> windowProperties = new HashMap<String, String>();
						windowProperties.put("pia.ajaxLink", "1");
						windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
						Map<String, String> params = new HashMap<String, String>();
						params.put("cnProfil", p.getCn());
						String url = portalUrlFactory.getExecutePortletLink(request,"toutatice-identite-ficheprofil-portailPortletInstance",windowProperties, params);	
						boolean a = userConnecte.hasProfil(dnManager);
						boolean b = p.isManagedBy(userConnecte);
						boolean c = habilitation.isAdmin(userConnecte);
						liste.add(new ManagerUrl(p.getDescription(), p.getCn(),p.getDn(),url,a||b||c,false));	
					} catch (Exception e) {
						logger.error("portlet de gestion des applications. Erreur lors de la création du lien pour l'ajout du manager " + dnManager);
						logger.error("problème API portalURLFactory");
						liste.add(new ManagerUrl(p.getDescription(), p.getCn(),p.getDn(),"",false,false));	
					}
				}
			}
		}
		Collections.sort(liste);
		model.addAttribute("listeManagersUrlAjout",liste);
	}
	
	public void setPortletContext(PortletContext ctx) {
		portletContext = ctx;			
	}
	
	
}
