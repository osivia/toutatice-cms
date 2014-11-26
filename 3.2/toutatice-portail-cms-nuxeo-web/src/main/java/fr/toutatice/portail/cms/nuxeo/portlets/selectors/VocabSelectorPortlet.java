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
package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;





/**
 * Portlet de selection de liste par vocabulaire
 */

public class VocabSelectorPortlet extends CMSPortlet {

	private static Log logger = LogFactory.getLog(VocabSelectorPortlet.class);

	public static String DELETE_PREFIX = "delete_";
	public static String OTHER_ENTRIES_CHOICE = "othersVocabEntries";

	public static int NB_NIVEAUX = 3;

	/**
	 * Permet d'exprimer le label d'un composant sur plusieurs niveaux : cle1/cle2/cle3
	 *
	 * @param label
	 * @param id
	 * @param vocab
	 * @return
	 */
	public static String getLabel(String othersLabel, String id, VocabularyEntry vocab, String preselect1) {
        String res = "";

        if (id.contains(OTHER_ENTRIES_CHOICE)
                && StringUtils.isNotEmpty(othersLabel)) {

            res = StringUtils.replace(id, OTHER_ENTRIES_CHOICE, othersLabel);

        } else { 

            String[] tokens = id.split("/", 2);

            if (tokens.length > 0 && preselect1==null) {
                VocabularyEntry child = vocab.getChild(tokens[0]);
                res += child.getLabel();
            }

            if (tokens.length > 1) {
                VocabularyEntry childVocab = vocab.getChild(tokens[0]);
                if (childVocab != null) {
                    if(res.length() > 0)
                        res += "/" ;
                    res += getLabel(res, tokens[1], childVocab, null);
                }
            }
        }

        return res;
    }




	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		PortalWindow window = WindowFactory.getWindow(req);

		if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("modifierPrefs") != null)) {

			if( req.getParameter("selectorId").length() > 0) {
                window.setProperty("osivia.selectorId", req.getParameter("selectorId"));
            } else if (window.getProperty("osivia.selectorId") != null) {
                window.setProperty("osivia.selectorId", null);
            }

			if( req.getParameter("libelle").length() > 0) {
                window.setProperty("osivia.libelle", req.getParameter("libelle"));
            } else if (window.getProperty("osivia.libelle") != null) {
                window.setProperty("osivia.libelle", null);
            }


			for(int niveau = 1; niveau < (NB_NIVEAUX + 1); niveau++){

				if( req.getParameter("vocabName" + String.valueOf(niveau)).length() > 0) {
                    window.setProperty("osivia.vocabName" + String.valueOf(niveau), req.getParameter("vocabName" + String.valueOf(niveau)));
                } else if (window.getProperty("osivia.vocabName" + String.valueOf(niveau)) != null) {
                    window.setProperty("osivia.vocabName" + String.valueOf(niveau), null);
                }

			}
			
            
            if( req.getParameter("preselect1").length() > 0)
                window.setProperty("osivia.preselect1", req.getParameter("preselect1"));
            else if (window.getProperty("osivia.preselect1") != null)
                window.setProperty("osivia.preselect1", null);
            
            
			

			if("1".equals(req.getParameter("selectorMonoValued"))) {
                window.setProperty("osivia.selectorMonoValued", "1");
            } else if (window.getProperty("osivia.selectorMonoValued") != null) {
                window.setProperty("osivia.selectorMonoValued", null);
            }

			if("1".equals(req.getParameter("othersOption"))) {
                window.setProperty("osivia.othersOption", "1");
            } else if (window.getProperty("osivia.othersOption") != null) {
                window.setProperty("osivia.othersOption", null);
            }

			if((req.getParameter("othersLabel") != null) && (req.getParameter("othersLabel").length() > 0)) {
                window.setProperty("osivia.othersLabel", req.getParameter("othersLabel"));
            } else if (window.getProperty("osivia.othersLabel") != null) {
                window.setProperty("osivia.othersLabel", null);
            }


			/* Initialisation du vocabulaire parent suite à éventuel changement de configuration. */
			Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
			if(selectors != null){
				List<String> vocabs = selectors.get(req.getParameter("selectorId"));
				if((vocabs != null) && (vocabs.size() > 0)){
					vocabs.clear();
					if( req.getParameter("selectors")!= null)
	                    res.setRenderParameter("lastSelectors", req.getParameter("selectors"));					    
			
					res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
				}
			}


			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("annuler") != null)) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Pour supporter le mode Ajax, il faut également test le add sans l'extension '.x'
		if ("view".equals(req.getPortletMode().toString())
				&& (((req.getParameter("add.x") != null) || (req.getParameter("add") != null))
						||(req.getParameter("monovaluedSubmit") != null))) {

			// Set public parameter
			String selectorId = window.getProperty("osivia.selectorId");
			if (selectorId != null) {

				Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
				List<String> vocabIds = selectors.get(selectorId);
				if (vocabIds == null) {
					vocabIds = new ArrayList<String>();
					selectors.put(selectorId, vocabIds);
				}

				String[] selectedVocabsEntries = { req.getParameter("vocab1Id"), req.getParameter("vocab2Id"),
						req.getParameter("vocab3Id") };

				String separator = "";
				int index = 0;
				String selectedEntries = "";
				
                String preselect = window.getProperty("osivia.preselect1");
                if( preselect != null)  {
                    
                    // If preselection is set, controls if 2nd item is selected
                    if(StringUtils.isNotEmpty(req.getParameter("vocab2Id")))    {
                        selectedVocabsEntries = new String[] { preselect, req.getParameter("vocab2Id"),
                                req.getParameter("vocab3Id") };
                    }   else
                        // If no item selected, remove selection
                        selectedVocabsEntries = new String[0];

                }
				
				for (String selectedVocabEntry : selectedVocabsEntries) {

					if (index > 0) {
                        separator = "/";
                    }

					if (StringUtils.isNotEmpty(selectedVocabEntry)) {

						selectedEntries += separator + selectedVocabEntry;

					}

					index++;

				}

				if (req.getParameter("monovaluedSubmit") != null) {
					/*
					 * On ne conserve qu'une valeur dans le cas d'un
					 * sélecteur mono-valué.
					 */
					vocabIds.clear();
				}
				if(StringUtils.isNotEmpty(selectedEntries)) {
                    vocabIds.add(selectedEntries);
                }

                String lastSelectors =  req.getParameter("selectors");
                if(lastSelectors != null)
                    res.setRenderParameter("lastSelectors", lastSelectors);                     

				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

				String vocab1Id = req.getParameter("vocab1Id");
				if(StringUtils.isNotEmpty(vocab1Id)) {
                    res.setRenderParameter("vocab1Id", vocab1Id);
                }

				String vocab2Id = req.getParameter("vocab2Id");
				if( vocab2Id != null) {
                    res.setRenderParameter("vocab2Id", vocab2Id);
                }

				String vocab3Id = req.getParameter("vocab3Id");
				if( vocab3Id != null) {
                    res.setRenderParameter("vocab3Id", vocab3Id);
                }


				// Réinitialisation des fenetres en mode NORMAL
				req.setAttribute("osivia.unsetMaxMode", "true");

			}

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Delete
		if ("view".equals(req.getPortletMode().toString()) && "delete".equals(req.getParameter("action"))) {
			int occ = new Integer(req.getParameter("occ"));

			Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
			String selectorId = window.getProperty("osivia.selectorId");

			List<String> vocabIds = selectors.get(selectorId);
			if ((vocabIds != null) && (vocabIds.size() > occ)) {

				vocabIds.remove(occ);
				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
				
                if( req.getParameter("selectors")!= null)
                    res.setRenderParameter("lastSelectors", req.getParameter("selectors"));                     
				

                // Réinitialisation des fenetres en mode NORMAL
                req.setAttribute("osivia.unsetMaxMode", "true");
			}

			String vocab1Id = req.getParameter("vocab1Id");
			if( vocab1Id != null) {
                res.setRenderParameter("vocab1Id", vocab1Id);
            }

			String vocab2Id = req.getParameter("vocab2Id");
			if( vocab2Id != null) {
                res.setRenderParameter("vocab2Id", vocab2Id);
            }

			String vocab3Id = req.getParameter("vocab3Id");
			if( vocab3Id != null) {
                res.setRenderParameter("vocab3Id", vocab3Id);
            }

		}

	}


	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);

		String libelle = window.getProperty("osivia.libelle");
		if (libelle == null) {
            libelle = "";
        }
		req.setAttribute("libelle", libelle);


		String selectorId = window.getProperty("osivia.selectorId");
		if (selectorId == null) {
            selectorId = "";
        }
		req.setAttribute("selectorId", selectorId);


		for(int niveau = 1; niveau < (NB_NIVEAUX + 1); niveau++){

			String vocabName = window.getProperty("osivia.vocabName" + String.valueOf(niveau));
			if (vocabName == null) {
                vocabName = "";
            }
			req.setAttribute("vocabName" + String.valueOf(niveau), vocabName);

		}
		
	      String preselect = window.getProperty("osivia.preselect1");
	        if (preselect == null)
	            preselect = "";
	        req.setAttribute("preselect1" , preselect);
	        


		String selectorMonoValued = window.getProperty("osivia.selectorMonoValued");
		if(selectorMonoValued == null) {
            selectorMonoValued = "0";
        }
		req.setAttribute("selectorMonoValued", selectorMonoValued);

		String othersOption = window.getProperty("osivia.othersOption");
		if(othersOption == null) {
            othersOption = "0";
        }
		req.setAttribute("othersOption", othersOption);

		String othersLabel = window.getProperty("osivia.othersLabel");
		if (othersLabel == null) {
            othersLabel = "";
        }
		req.setAttribute("othersLabel", othersLabel);

		rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/vocab/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);
			request.setAttribute("window", window);

			String libelle = window.getProperty("osivia.libelle");
			request.setAttribute("libelle", libelle);

			String selectorId = window.getProperty("osivia.selectorId");
			if (selectorId == null) {
				response.getWriter().print("<h2>Identifiant non défini</h2>");
				response.getWriter().close();
				return;

			}

			List<String> vocabs = new ArrayList<String>();

			String selectorMonoValued = window.getProperty("osivia.selectorMonoValued");
			request.setAttribute("selectorMonoValued", selectorMonoValued);

			String vocabName1 = window.getProperty("osivia.vocabName1");
			if (vocabName1 == null) {
				response.getWriter().print("<h2>Vocabulaire non défini</h2>");
				response.getWriter().close();
				return;

			}

			vocabs.add(vocabName1);


			String vocabName2 = window.getProperty("osivia.vocabName2");
			if( vocabName2 != null){
			    vocabs.add(vocabName2);
			}

			String vocabName3 = window.getProperty("osivia.vocabName3");
			if( vocabName3 != null){
			    vocabs.add(vocabName3);
			}



			String vocab1Id = request.getParameter("vocab1Id");
			
            String preselect1 = window.getProperty("osivia.preselect1");
            if( StringUtils.isNotEmpty(preselect1))
                vocab1Id= preselect1;
			
			String vocab2Id = request.getParameter("vocab2Id");
			String vocab3Id = request.getParameter("vocab3Id");



			// Get public parameter
			Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
			List<String> curSelect = selectors.get(selectorId) ;
			if (curSelect != null) {
                request.setAttribute("vocabsId", curSelect);
                if( "1".equals(selectorMonoValued))  {
                    
                    
                    if( curSelect.size() > 0)   {
                        String[] tokens = curSelect.get( 0).split("/");
                        if( tokens.length > 0)  {
                            vocab1Id = tokens[0];
                        }
                        if( tokens.length > 1)  {
                            vocab2Id = tokens[1];
                        }
                        if( tokens.length > 2)  {
                            vocab3Id = tokens[2];
                        }
                                                
                    }             
                }
            } else {
                if( "1".equals(selectorMonoValued))  {
                    if( preselect1 == null)
                        vocab1Id= null;
                    vocab2Id=null;
                    vocab3Id=null;
                }
                request.setAttribute("vocabsId", new ArrayList<String>());
            }


			
			
			

			request.setAttribute("vocab1Id", vocab1Id);
			request.setAttribute("vocab2Id", vocab2Id);
			request.setAttribute("vocab3Id", vocab3Id);
			
            request.setAttribute("preselect1", preselect1);     			

			request.setAttribute("vocabName2", vocabName2);
			request.setAttribute("vocabName3", vocabName3);


			NuxeoController ctx = new NuxeoController(request, response, this.getPortletContext());




			VocabularyEntry vocab = VocabularyHelper.getVocabularyEntry(ctx, vocabs);
			request.setAttribute("vocab1", vocab);

			this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/vocab/view.jsp")
					.include(request, response);

		} catch (NuxeoException e) {
			PortletErrorHandler.handleGenericErrors(response, e);
		}

		catch (Exception e) {
			if (!(e instanceof PortletException)) {
                throw new PortletException(e);
            }
		}

		logger.debug("doView end");
	}

}
