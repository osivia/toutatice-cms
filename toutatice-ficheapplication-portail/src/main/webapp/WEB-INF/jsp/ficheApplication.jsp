<%@page import="javax.portlet.WindowState"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<%@ page import="fr.toutatice.outils.ldap.entity.Application"%>

<portlet:renderURL var="gestionProfils"><portlet:param name="action" value="gestionProfils" /></portlet:renderURL>

<%int cpt = 0; %>

<h1>Application ${ficheAppli.application.nom}</h1>

<div class="menu1"> 
  <span class="onglet-actif">Détail de l'application</span> 	
  <c:if test="${level == 'ADMINISTRATEUR'}">
  	<a class="onglet" href="<portlet:renderURL><portlet:param name="action" value="gestionProfils" /></portlet:renderURL>">Gérer les profils</a> 
  </c:if>
  <c:if test="${level == 'ADMINISTRATEUR' || level == 'GESTIONNAIRE'}">
  	<a class="onglet" href="<portlet:renderURL><portlet:param name="action" value="gestionManagers" /></portlet:renderURL>">Gérer les managers</a> 
  </c:if>
</div>

<div class="ligne">
	<div class= "nomAttr">Nom de l'application :</div>
	<div class= "contenu"><c:out value="${ficheAppli.application.nom}"/></div>
</div>

<div class="ligne">
	<div class= "nomAttr">Identifiant de l'application :</div>
	<div class= "contenu"><c:out value="${ficheAppli.application.id}"/></div>
</div>

<div class="ligne">
	<div class= "nomAttr">Description :</div>
	<div class= "contenu"><c:out value="${ficheAppli.application.description}"/></div>
</div>

	<div class="ligne">
		<div class= "nomAttr">Profils associés :</div>
		<div class= "contenu">
			
				<TABLE>
					<c:forEach items="${listeProfilsUrl}" var="profil">
						<%cpt++; %>
						<TR class="item<%=(cpt % 2)%>">
							<TD>
								<c:if test="${profil.clicable}">
									<div class="no-ajax-link">
										<a href="${profil.url}">${profil.profil.description} ( ${profil.profil.cn} )</a>
										<a href="${profil.url}"> - Cliquez ici</a>
									</div>
								</c:if>
								<c:if test="${not profil.clicable}">
									<div class="no-ajax-link">
										${profil.profil.description} ( ${profil.profil.cn} )
									</div>
								</c:if>
					        </TD>
				        </TR>
				    </c:forEach>
				</TABLE>
		</div>
	</div>

	<div class="ligne">
		<div class= "nomAttr">Roles applicatifs associés :</div>
		<div class= "contenu">
			
				<TABLE>
					<c:forEach items="${listeRolesUrl}" var="role">
						<%cpt++; %>
						<TR class="item<%=(cpt % 2)%>">
							<TD>
								<c:if test="${role.clicable}">
									<div class="no-ajax-link">
										<a href="${role.url}">${role.roleApplicatif.description} ( ${role.roleApplicatif.cn} )</a>
										<a href="${role.url}"> - Cliquez ici</a>
						        	</div>
					        	</c:if>
					        	<c:if test="${not role.clicable}">
					        		${role.roleApplicatif.description} ( ${role.roleApplicatif.cn} )
					        	</c:if>
					        </TD>
				        </TR>
				    </c:forEach>
				</TABLE>
			</div>
		</div>


<c:if test="${level != 'LECTEUR'}">	
	<div class="ligne">
		<div class= "nomAttr">Managers :</div>
		<div class= "contenu">
			
				<TABLE>
					<c:forEach items="${listeManagersUrl}" var="manager">
						<%cpt++; %>
						<TR class="item<%=(cpt % 2)%>">
							<TD>
								<c:if test="${manager.clicable}">
									<div class="no-ajax-link">
										<a href="${manager.url}">${manager.description} ( ${manager.id} )</a>
										<a href="${manager.url}"> - Cliquez ici</a>
						        	</div>
						        </c:if>
						        <c:if test="${not manager.clicable}">
						        	${manager.description} ( ${manager.id} )</a>
						        </c:if>
					        </TD>
				        </TR>
				    </c:forEach>
				</TABLE>
			</div>
		</div>
</c:if> 
     
<div class="ligne">
		<font style="color: blue;"><c:out value="${messageInfo}"/></font>
</div>   
<div class="ligne">
		<font style="color: red;"><c:out value="${messageErreur}"/></font>
</div>   

