package org.nuxeo.ecm.automation.client.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Version corrigée de {@link Document}.
 * <p>
 * Les corrections sont les suivantes :
 * <ul>
 * <li>Des constructeurs plus simples ont été ajoutés.
 * <li>Les variables d'instance de type {@code PropertyMap} utilisent {@code NuxeoPropertyMap}.
 * <li>La méthode {@link #remove(String)} a été ajoutée (car {@code set("name", null)} ne supprime pas la propriété {@code "name"}, mais lui associe {@code null}).
 * <li>Des méthodes {@link #equals(Object)} et {@link #hashCode()} ont été définies qui prennent en compte l'état de l'objet.
 * </ul>
 *
 * @author Kévin Le Helley
 * @see Document
 */
public final class NuxeoDocument extends Document {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur simple.
	 *
	 * @param type
	 *            le type du document
	 */
	public NuxeoDocument(final String type) {
		super(null, type, null, null, null, null, null, null, null, null, new NuxeoPropertyMap(), new NuxeoPropertyMap());
	}

	/**
	 * Constructeur.
	 *
	 * @param type
	 *            le type du document
	 * @param id
	 *            l'identifiant du document
	 * @param path
	 *            le chemin du document
	 * @param state
	 *            l'état du document
	 */
	public NuxeoDocument(final String type, final String id, final String path, final String state) {
		super(id, type, null, null, path, state, null, null, null, null, new NuxeoPropertyMap(), new NuxeoPropertyMap());
	}

	/**
	 * Constructeur.
	 *
	 * @param type
	 *            le type du document
	 * @param id
	 *            l'identifiant du document
	 * @param path
	 *            le chemin du document
	 * @param state
	 *            l'état du document
	 * @param properties
	 *            les propriétés du document
	 */
	public NuxeoDocument(final String type, final String id, final String path, final String state, final PropertyMap properties) {
		super(id, type, null, null, path, state, null, null, null, null, new NuxeoPropertyMap(properties), new NuxeoPropertyMap());
	}

	/**
	 * Fournit la valeur d'une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @return la valeur associée si elle existe ; {@code null} si la propriété n'existe pas ou qu'elle ne contient rien
	 */
	public Boolean getBoolean(final String key) {
		return properties.getBoolean(key);
	}

	/**
	 * Fournit la valeur d'une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @return la valeur associée si elle existe ; {@code null} si la propriété n'existe pas ou qu'elle ne contient rien
	 */
	public PropertyMap getMap(final String key) {
		return properties.getMap(key);
	}

	/**
	 * Fournit la valeur d'une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @return la valeur associée si elle existe ; {@code null} si la propriété n'existe pas ou qu'elle ne contient rien
	 */
	public PropertyList getList(final String key) {
		return properties.getList(key);
	}

	/**
	 * Fournit la valeur d'une propriété, ou retourne la valeur par défaut fournie.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @param defValue
	 *            la valeur retournée par défaut
	 * @return la valeur associée si elle existe, sinon la valeur par défaut spécifiée
	 */
	public Boolean getBoolean(final String key, final Boolean defValue) {
		return properties.getBoolean(key, defValue);
	}

	/**
	 * Fournit la valeur d'une propriété, ou retourne la valeur par défaut fournie.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @param defValue
	 *            la valeur retournée par défaut
	 * @return la valeur associée si elle existe, sinon la valeur par défaut spécifiée
	 */
	public PropertyMap getMap(final String key, final PropertyMap defValue) {
		return properties.getMap(key, defValue);
	}

	/**
	 * Fournit la valeur d'une propriété, ou retourne la valeur par défaut fournie.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @param defValue
	 *            la valeur retournée par défaut
	 * @return la valeur associée si elle existe, sinon la valeur par défaut spécifiée
	 */
	public PropertyList getList(final String key, final PropertyList defValue) {
		return properties.getList(key, defValue);
	}

	/**
	 * Ajoute ou modifie une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @param value
	 *            la valeur à lui associer
	 */
	public void set(final String key, final Boolean value) {
		properties.set(key, value);
	}

	/**
	 * Ajoute ou modifie une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @param value
	 *            la valeur à lui associer
	 */
	public void set(final String key, final PropertyMap value) {
		properties.set(key, value);
	}

	/**
	 * Ajoute ou modifie une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 * @param value
	 *            la valeur à lui associer
	 */
	public void set(final String key, final PropertyList value) {
		properties.set(key, value);
	}

	/**
	 * Supprime une propriété.
	 *
	 * @param key
	 *            le nom de la propriété
	 */
	public void remove(final String key) {
		((NuxeoPropertyMap) properties).remove(key);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(83, 13)
				// Object
				.appendSuper(super.hashCode())
				// DocRef
				.append(ref)
				// Document
				.append(repository)
				.append(path)
				.append(type)
				.append(state)
				.append(lockOwner)
				.append(lockCreated)
				.append(versionLabel)
				.append(properties)
				.append(contextParameters)
				.append(changeToken)
				.append(facets)
				.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final NuxeoDocument rhs = (NuxeoDocument) obj;
		return new EqualsBuilder()
				// Object
				.appendSuper(super.equals(obj))
				// DocRef
				.append(ref, rhs.ref)
				// Document
				.append(repository, rhs.repository)
				.append(path, rhs.path)
				.append(type, rhs.type)
				.append(state, rhs.state)
				.append(lockOwner, rhs.lockOwner)
				.append(lockCreated, rhs.lockCreated)
				.append(versionLabel, rhs.versionLabel)
				.append(properties, rhs.properties)
				.append(contextParameters, rhs.contextParameters)
				.append(changeToken, rhs.changeToken)
				.append(facets, rhs.facets)
				.isEquals();
	}

}
