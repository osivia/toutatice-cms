<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />

<form method="post" action="<portlet:actionURL />">
    <div class="osivia-portal-table">
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Nombre de groupes par noeud :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="groups-count" value="5" required="required" size="1" maxlength="1" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Profondeur de l'arbre des groupes :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="groups-depth" value="3" required="required" size="1" maxlength="1" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Nombre d'utilisateurs :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="users-count" value="300" required="required" size="4" maxlength="4" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Probabilités d'appartenance des utilisateurs à des groupes :</p></div>
            <div class="osivia-portal-cell">
                <input type="number" value="1.00" readonly="readonly" size="4" />
                <input type="hidden" name="users-probability-1" value="1.00" />
                <input type="number" value="1.00" readonly="readonly" size="4" />
                <input type="hidden" name="users-probability-2" value="1.00" />
                <input type="number" name="users-probability-3" value="0.50" size="4" />
                <input type="number" name="users-probability-4" size="4" />
                <input type="number" name="users-probability-5" size="4" />
            </div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Chemin de l'espace de diffusion Nuxeo :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="documents-parent" required="required" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Chemin du workspace Nuxeo :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="documents-workspace" required="required" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Nombre de sites ou de pages par noeud :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="documents-count" value="4" required="required" size="1" maxlength="1" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Nombre de notes par noeud :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="documents-notes-count" value="6" required="required" size="1" maxlength="1" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Profondeur de l'arbre des pages :</p></div>
            <div class="osivia-portal-cell"><input type="text" name="documents-depth" value="4" required="required" size="1" maxlength="1" /></div>
        </div>
        <div class="osivia-portal-row">
            <div class="osivia-portal-cell"><p>Probabilités d'appartenance des notes à des groupes :</p></div>
            <div class="osivia-portal-cell">
                <input type="number" name="documents-probability-1" value="0.80" size="4" />
                <input type="number" name="documents-probability-2" size="4" />
                <input type="number" name="documents-probability-3" size="4" />
                <input type="number" name="documents-probability-4" size="4" />
                <input type="number" name="documents-probability-5" size="4" />
            </div>
        </div>    
    </div>

    <p><input type="submit" /></p>
</form>
