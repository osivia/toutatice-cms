package org.nuxeo.ecm.automation.client.model;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.nuxeo.ecm.automation.client.model.DateUtils;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Version corrigée de {@link PropertyMap}.
 * <p>
 * Les corrections sont les suivantes :
 * <ul>
 * <li>La classe implémente l'interface {@link Map Map&lt;String, Object&gt;}.
 * <li>Fournir une valeur {@code null} aux méthodes {@code set(String, T)} ne risque pas de déclencher une {@link NullPointerException}. Le comportement est cohérent pour toutes
 * ces méthodes, avec {@code null} stocké dans la {@code Map} interne pour la clef fournie.
 * <li>Des méthodes {@link #equals(Object)} et {@link #hashCode()} ont été définies qui prennent en compte l'état de l'objet.
 * <li>La méthode {@link #toString()} produit une chaîne de caractères dans le format utilisé pour les échanges via l'API Nuxeo (propriété {@code properties} pour Document.Create
 * et Document.Update).
 * </ul>
 * <p>
 * Un point sur lequel faire particulièrement attention avec cette classe, c'est qu'avant de stocker une valeur qui lui est passée, à moins que cette valeur soit de type
 * {@code PropertyList}, ou {@code PropertyMap}, une conversion vers {@code String} est effectuée. Cela signifie que du point de vue des instances de {@code NuxeoPropertyMap},
 * {@code 18} et {@code "18"} sont équivalents.
 *
 * @author Kévin Le Helley
 * @see PropertyMap
 */
public final class NuxeoPropertyMap extends PropertyMap implements Map<String, Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 */
	public NuxeoPropertyMap() {
		super();
	}

	/**
	 * Constructeur avec initialisation.
	 *
	 * @param props
	 *            le dictionnaire de propriétés à utiliser pour l'initialisation
	 */
	public NuxeoPropertyMap(final PropertyMap props) {
		super(props);
	}

	/**
	 * Constructeur avec initialisation.
	 *
	 * @param map
	 *            les données avec lesquelles initialiser l'objet
	 */
	public NuxeoPropertyMap(final Map<String, Object> map) {
		super(map);
	}

	/**
	 * Constructeur.
	 *
	 * @param size
	 *            la taille initiale de l'objet
	 */
	public NuxeoPropertyMap(final int size) {
		super(size);
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final String value) {
		map.put(key, toInternalFormatValue(value));
	}

	private Object toInternalFormatValue(final Object value) {
		if (value == null) {
			return "";
		} else if ((value instanceof PropertyMap) || (value instanceof PropertyList)) {
			return value;
		} else if (value instanceof Date) {
			return DateUtils.formatDate((Date) value);
		} else {
			return value.toString();
		}
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final Boolean value) {
		map.put(key, toInternalFormatValue(value));
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final Long value) {
		map.put(key, toInternalFormatValue(value));
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final Double value) {
		map.put(key, toInternalFormatValue(value));
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final Date value) {
		map.put(key, toInternalFormatValue(value));
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final PropertyList value) {
		map.put(key, toInternalFormatValue(value));
	}

	/**
	 * Range la valeur fournie, associée à sa clef, dans le dictionnaire.
	 *
	 * @param key
	 *            la clef
	 * @param value
	 *            la valeur
	 */
	@Override
	public void set(final String key, final PropertyMap value) {
		map.put(key, toInternalFormatValue(value));
	}

	@Override
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return map.containsValue(toInternalFormatValue(value));
	}

	@Override
	public Object get(final Object key) {
		return map.get(key);
	}

	@Override
	public Object put(final String key, final Object value) {
		return map.put(key, toInternalFormatValue(value));
	}

	@Override
	public Object remove(final Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(final Map<? extends String, ? extends Object> m) {
		// m peut contenir des objets de type divers ; or on a un comportement spécial défini pour certains types
		for (final Entry<? extends String, ? extends Object> obj : m.entrySet()) {
			put(obj.getKey(), obj.getValue());
		}
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	@Override
	public int hashCode() {
		return map.hashCode();
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
		final NuxeoPropertyMap other = (NuxeoPropertyMap) obj;
		return map.equals(other.map);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		for (final Entry<String, Object> entry : map.entrySet()) {
			builder.append(entry.getKey())
					.append("=");

			final Object value = entry.getValue();
			if (value != null) {
				if (value instanceof String) {
					builder.append(escapeCrLf((String) value));
				} else if (value instanceof PropertyMap) {
					builder.append(NuxeoJsonMapper.toString((PropertyMap) value));
				} else if (value instanceof PropertyList) {
					builder.append(NuxeoJsonMapper.toString((PropertyList) value));
				} else {
					// Inattendu, car normalement en interne une PropertyList|Map ne contient que les trois types précédemment testés
					// TODO (KLH) LOG.warn à prévoir ?
					builder.append(escapeCrLf(value.toString()));
				}
			}

			builder.append("\n");
		}

		return builder.toString();
	}

	private String escapeCrLf(final String str) {
		return str.replaceAll("\n", "\\n");
	}

}
