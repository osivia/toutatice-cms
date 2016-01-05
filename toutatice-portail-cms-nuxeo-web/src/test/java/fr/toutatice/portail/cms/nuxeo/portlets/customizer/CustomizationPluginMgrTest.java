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
import java.util.ArrayList;


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
        
        // Test non default values
        DocumentType nonDefault = new DocumentType("NonDefautType", false, false, false, false, false, false, new ArrayList<String>(0), null, "glyphicons glyphicons-picture", true, false, true);
        DocumentType clone = CustomizationPluginMgr.cloneDefaultType(nonDefault);
        Assert.assertTrue("Incorrect cloning for non default values", EqualsBuilder.reflectionEquals(nonDefault, clone));
 
        
        // Test true values
        DocumentType trueValues = new DocumentType("TrueType", true, true, true, true, true, true, new ArrayList<String>(0), null, "true true-picture", true, true, true);
        DocumentType trueClone = CustomizationPluginMgr.cloneDefaultType(trueValues);
        Assert.assertTrue( "Incorrect cloning for true values", EqualsBuilder.reflectionEquals(trueValues, trueClone));

        // Test false values
        DocumentType falseValues = new DocumentType("FalseType", false, false, false, false, false, false, new ArrayList<String>(0), null, "false", false, false, false);
        DocumentType falseClone = CustomizationPluginMgr.cloneDefaultType(falseValues);
        Assert.assertTrue( "Incorrect cloning for false values", EqualsBuilder.reflectionEquals(falseValues, falseClone));       
        
    }
}
