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
 */
package fr.toutatice.portail.cms.nuxeo.api.services.dao;

import org.osivia.portal.api.context.PortalControllerContext;

/**
 * Data access object service interface.
 *
 * @author Cédric Krommenhoek
 * @param <Type> object
 * @param <DTO> data transfert object
 */
public interface IDAO<Type, DTO> {

    /**
     * Convert object to data transfert object.
     *
     * @param object object
     * @return data transfert object
     * @deprecated
     */
    DTO toDTO(Type object);


    /**
     * Convert object to data transfert object.
     *
     * @param portalControllerContext
     * @param object object
     * @return data transfert object
     */
    DTO toDTO(PortalControllerContext portalControllerContext, Type object);

}
