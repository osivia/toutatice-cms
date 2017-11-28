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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.lang.reflect.Constructor;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.osivia.portal.api.cms.DocumentType;



public class CustomizationPluginMgrTest  {

    @Test
    public final void testCloneDocumentType()  {
        
        int LAST_CONSTRUCTOR_PARAMS = 13;
        
        Constructor<?>[] constructors = DocumentType.class.getConstructors();
        
        for(int i=0; i< constructors.length; i++)    {
            Assert.assertTrue("Must test the greater Constructor",constructors[ i].getParameterTypes().length <= LAST_CONSTRUCTOR_PARAMS);
        }
        
        
        // Root document type
        DocumentType root = DocumentType.createRoot("Root");
        DocumentType rootClone = root.clone();
        Assert.assertTrue(EqualsBuilder.reflectionEquals(root, rootClone));
        
    }
}
