<%@page import="javax.portlet.WindowState"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<%@ page import="fr.toutatice.outils.ldap.entity.Application"%>

<portlet:defineObjects />

<portlet:actionURL var="valider"><portlet:param name="action" value="valider" /></portlet:actionURL>
<portlet:actionURL var="rechercherProfil"> <portlet:param name="action" value="rechercherProfil" /> </portlet:actionURL>

<%int cpt = 0; %>

<h1>Application ${ficheAppli.application.nom}</h1>

<div class="menu1"> 
  <a class="onglet" href="<portlet:renderURL><portlet:param name="action" value="ficheApplication" /></portlet:renderURL>">Détail de l'application</a> 
  <span class="onglet-actif">Gérer les profils</span> 	
  <c:if test="${level == 'ADMINISTRATEUR' || level == 'GESTIONNAIRE'}">
  	<a class="onglet" href="<portlet:renderURL><portlet:param name="action" value="gestionManagers" /></portlet:renderURL>">Gérer les managers</a> 
  </c:if>
</div>


<h2>Gestion des profils de l'application</h2>


<form:form id="formAppli" name="formAppli" method="post" modelAttribute="ficheAppli"  action="${rechercherProfil}">  	

<div class="ligne">
	<div class= "nomAttr">Profils associés :</div>
	<div class= "contenu">
		<TABLE>
				    
			<c:forEach items="${listeProfilsUrl}" var="profilUrl">
				<%cpt++; %>
				<tr class="item<%=(cpt % 2)%>">
					<TD>
							<div class="no-ajax-link">
								<a href="${profilUrl.url}">
									<c:out value="${profilUrl.profil.description}"/> (<c:out value="${profilUrl.profil.cn}"/>)
								</a>
							</div>
							<a href="<portlet:actionURL>
										<portlet:param name="action" value="deleteProfil"/>
		         						<portlet:param name="cn" value="${profilUrl.profil.cn}"/>
         							</portlet:actionURL>">
         						<img src="<%=request.getContextPath()%>/images/delete.gif" border="0"/></a>
			        </TD>
		        </tr>
		    </c:forEach>
		</TABLE>
	</div>
</div>

<h3>Ajouter des Profils :</h3>

	<div class="ligne">
		Périmètre de la recherche :
		<c:forEach items="${etbUserConnecte}" var="str">
			<form:radiobutton path="filtreRne" value="${str.id}"/>${str.description} 
		</c:forEach>
		Filtre : 
		<input type="text" name="filtreProfil" id="filtreProfil" size="15" value="<c:out value="${ficheAppli.filtreProfil}"/>" />	
		<input type="submit" value="Rechercher"/>
	</div>
	
	
	<c:if test="${not empty ficheAppli.listeProfilsAjout}">
		<div class="ligne">
		<div class= "nomAttr">${ fn:length(ficheAppli.listeProfilsAjout) } résultats</div>
			<div class= "contenu">
				<TABLE>
					<c:forEach items="${ficheAppli.listeProfilsAjout}" var="profil" begin="${firstrow}" end="${firstrow + rowcount - 1}">
						<%cpt++; %>
						<tr class="item<%=(cpt % 2)%>">
							<TD>
									<c:out value="${profil.description}"/> (<c:out value="${profil.cn}"/>)
							</TD>
							<TD>
									<a href="<portlet:actionURL>
												<portlet:param name="action" value="ajoutProfil"/>
				         						<portlet:param name="cn" value="${profil.cn}"/>
		         							</portlet:actionURL>">
		         						<img src="<%=request.getContextPath()%>/images/add.gif" border="0"/></a>	
					        </TD>
				        </tr>
				    </c:forEach>
				</TABLE>
				
				<c:if test="${firstrow > 0}">
					<a href="<portlet:renderURL><portlet:param name="action" value="pagePrecedenteAjoutProfils"/></portlet:renderURL>">Page précédente</a>	
				</c:if>
				
				<c:out value="${firstrow}"/> - <c:out value="${firstrow+rowcount}"/>  
				
				<c:if test="${dernierePage}">
					<a href="<portlet:renderURL><portlet:param name="action" value="pageSuivanteAjoutProfils"/></portlet:renderURL>">Page suivante</a>
				</c:if>
				
			</div>
		</div>
	</c:if>


<div class="ligne">
		<font style="color: red;"><c:out value="${messageErreur}"/></font>
</div> 
<div class="ligne">
		<font style="color: blue;"><c:out value="${messageInfo}"/></font>
</div>  

<div class="ligne">
		<div class="bouton">
			<input type="button" value="Valider" onclick="updatePortletContent(this,'${valider}');" />
		</div>
</div>


</form:form>




          

