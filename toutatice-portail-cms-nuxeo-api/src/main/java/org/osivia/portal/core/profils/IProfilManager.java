/*
 * (C) Copyright 2014 OSIVIA (http://www.osivia.com) 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.osivia.portal.core.profils;

import java.util.List;



public interface IProfilManager {
	
	/* Pour lecture et mise à jour depuis l'admin */
    /*
	public  List<ProfilBean> getListeProfils( Portal portal);
	public void setListeProfils( Portal portal, List<ProfilBean> profils)  ;
	*/
	/* Pour utilisation */
	//public List<Role> getFilteredRoles( ) ;
	public ProfilBean getProfilPrincipalUtilisateur();
	public ProfilBean getProfil(String name);
	public boolean verifierProfilUtilisateur(String name);
	public  List<ProfilBean> getListeProfils( );

}
