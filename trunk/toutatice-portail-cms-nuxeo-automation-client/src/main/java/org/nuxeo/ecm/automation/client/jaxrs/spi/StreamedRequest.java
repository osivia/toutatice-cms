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
package org.nuxeo.ecm.automation.client.jaxrs.spi;

import java.io.InputStream;

import org.nuxeo.ecm.automation.client.model.StreamBlob;


/**
 * Requete de streaming
 * 
 * @author Jean-Sébastien Steux
 *
 */
public class StreamedRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StreamedRequest(int method, String url) {
        super(method, url);

    }

    public Object handleResult(int status, String ctype, String disp,
            InputStream stream) throws Exception {
        if (status == 204) { // no content
            return null;
        } else if (status >= 400) {
            handleException(status, ctype, stream);
        }
        return new StreamBlob(stream, getFileName(disp), ctype);
    }
    
}
