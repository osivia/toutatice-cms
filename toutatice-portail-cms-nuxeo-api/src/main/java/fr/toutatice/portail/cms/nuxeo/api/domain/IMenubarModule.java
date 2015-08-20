/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.List;

import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * Module for customize the menubar
 * @author lbillon
 *
 */
public interface IMenubarModule {

	/**
	 * Adapt the menubar
	 * @param ctx the context
	 * @param menuBar the menubars
	 * @param publicationInfos publications infos of the doc
	 * @param extendedDocumentInfos extended infos (not cached) of the doc
	 */
	public void adaptContentMenuBar(CMSServiceCtx C, List<MenubarItem> menuBar, CMSPublicationInfos publicationInfos, CMSExtendedDocumentInfos extendedDocumentInfos) throws CMSException;
	
}
