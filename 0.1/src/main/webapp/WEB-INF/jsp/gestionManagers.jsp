<%@page import="javax.portlet.WindowState"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>


<portlet:defineObjects />

<portlet:actionURL var="valider"><portlet:param name="action" value="valider" /></portlet:actionURL>
<portlet:actionURL var="rechercherManager"> <portlet:param name="action" value="rechercherManager" /> </portlet:actionURL>

<%int cpt = 0; %>

<h1>Application ${ficheAppli.application.nom}</h1>

<div class="menu1"> 
  <a class="onglet" href="<portlet:renderURL><portlet:param name="action" value="ficheApplication" /></portlet:renderURL>">Détail de l'application</a> 
  <c:if test="${level == 'ADMINISTRATEUR'}">
  	<a class="onglet" href="<portlet:renderURL><portlet:param name="action" value="gestionProfils" /></portlet:renderURL>">Gérer les profils</a> 
  </c:if>
  <span class="onglet-actif">Gérer les managers</span> 	
</div>


<h2>Gestion des managers de l'application</h2>


<form:form method="post" modelAttribute="ficheAppli"  action="${rechercherManager}">  	

<div class="ligne">
	<div class= "nomAttr">Managers :</div>
	<div class= "contenu">
		<TABLE>
				    
			<c:forEach items="${listeManagersUrl}" var="managerUrl">
				<%cpt++; %>
				<tr class="item<%=(cpt % 2)%>">
					<TD>
							<div class="no-ajax-link">
								<a href="${managerUrl.url}">
									<c:out value="${managerUrl.description}"/> (<c:out value="${managerUrl.id}"/>)
								</a>
							</div>
							<c:if test="${not managerUrl.self}">
								<a href="<portlet:actionURL>
											<portlet:param name="action" value="deleteManager"/>
			         						<portlet:param name="dnManager" value="${managerUrl.dn}"/>
	         							</portlet:actionURL>">
	         						<img src="<%=request.getContextPath()%>/images/delete.gif" border="0"/></a>
         					</c:if>
			        </TD>
		        </tr>
		    </c:forEach>
		</TABLE>
	</div>
</div>

<h3>Ajouter des Managers :</h3>
	<div class="ligne">
	Ajouter un manager de type :
			<form:radiobutton path="typeManager" value="person"/>Personne 
			<form:radiobutton path="typeManager" value="profil"/>Profil 
	</div>
	<div class="ligne">
		Périmètre de la recherche :
		<c:forEach items="${etbUserConnecte}" var="str">
			<form:radiobutton path="filtreRne" value="${str.id}"/>${str.description} 
		</c:forEach>
		Filtre : 
		<input type="text" name="filtreManager" id="filtreManager" size="15" value="<c:out value="${ficheAppli.filtreManager}"/>" />	
		<input type="submit" value="Rechercher"/>
	</div>
	
		<div class="ligne">
		<div class= "nomAttr">${ fn:length(listeManagersUrlAjout) } résultats</div>
			<div class= "contenu">
				<TABLE>
					<c:forEach items="${listeManagersUrlAjout}" var="manager" begin="${firstrowManagerAjout}" end="${firstrowManagerAjout + rowcount - 1}">
						<%cpt++; %>
						<tr class="item<%=(cpt % 2)%>">
							<TD>
									<c:out value="${manager.description}"/> (<c:out value="${manager.id}"/>)
							</TD>
							<TD>
									<a href="<portlet:actionURL>
												<portlet:param name="action" value="ajoutManager"/>
				         						<portlet:param name="dnManager" value="${manager.dn}"/>
		         							</portlet:actionURL>">
		         						<img src="<%=request.getContextPath()%>/images/add.gif" border="0"/></a>	
					        </TD>
				        </tr>
				    </c:forEach>
				</TABLE>
				
				<c:if test="${firstrowManagerAjout > 0}">
					<a href="<portlet:renderURL><portlet:param name="action" value="pagePrecedenteAjoutManagers"/></portlet:renderURL>">Page précédente</a>	
				</c:if>
				
				<c:if test="${not empty listeManagersUrlAjout}">
					<c:out value="${firstrowManagerAjout}"/> - <c:out value="${firstrowManagerAjout+rowcount}"/> 
				</c:if> 
				
				<c:if test="${dernierePageManagerAjout}">
					<a href="<portlet:renderURL><portlet:param name="action" value="pageSuivanteAjoutManagers"/></portlet:renderURL>">Page suivante</a>
				</c:if>
				
			</div>
		</div>


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




          

