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
 *
*/

package fr.toutatice.portail.cms.nuxeo.portlets.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.cms.CMSBinaryContent;




public class StreamingServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 9089991326380332372L;


    public static void streamBigFile(InputStream inputStream, OutputStream outputStream, int bufSize) throws IOException {


        try {
            byte[] b = new byte[bufSize];
            int bytesread = 0;
            int bytesBuffered = 0;
            while ((bytesread = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, bytesread);
                bytesBuffered += bytesread;
                if (bytesBuffered > 1024 * 1024) { // flush after 1MB
                    bytesBuffered = 0;
                }
            }
            outputStream.flush();
        } finally {
            inputStream.close();

        }
    }


    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws IOException, ServletException {

        OutputStream output = theResponse.getOutputStream();
        
        CMSBinaryContent content = null;
        try {


            String idLargeFile = theRequest.getParameter("idLargeFile");
            if (idLargeFile != null) {

                content = CMSBinaryContent.largeFile.get(idLargeFile);
  
                if (content != null) {

                    theResponse.setContentType(content.getMimeType());
                    theResponse.setHeader("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");
                    theResponse.setBufferSize(8192);

                    streamBigFile(content.getStream(), output, 8192);
                    
                    CMSBinaryContent.largeFile.remove(idLargeFile);
                }
            }


        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            output.close();
            
            // Close Nuxeo Session
            ((Session) content.getLongLiveSession()).close();
        }

    }

}