package org.nuxeo.ecm.automation.client.model;

import java.util.Iterator;
import java.util.Map.Entry;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Classe utilitaire en charge des opérations de conversion {@link PropertyList} et {@link PropertyMap} vers format JSON minimal.
 */
final class NuxeoJsonMapper {

	private NuxeoJsonMapper() {
		throw new AssertionError("Classe utilitaire, ne pas instancier");
	}

	/**
	 * Convertit la liste passée en argument en chaîne de caractère au format JSON.
	 *
	 * @param list
	 *            l'objet à convertir
	 * @return la chaîne JSON correspondant à l'objet
	 */
	public static String toString(final PropertyList list) {
		final StringBuilder builder = new StringBuilder("[");

		final Iterator<Object> iter = list.list().iterator();
		while (iter.hasNext()) {
			builder.append(objectToString(iter.next()));
			if (iter.hasNext()) {
				builder.append(",");
			}
		}

		builder.append("]");
		return builder.toString();
	}

	/**
	 * Convertit la map passée en argument en chaîne de caractère au format JSON.
	 *
	 * @param map
	 *            l'objet à convertir
	 * @return la chaîne JSON correspondant à l'objet
	 */
	public static String toString(final PropertyMap map) {
		final StringBuilder builder = new StringBuilder("{");

		final Iterator<Entry<String, Object>> iter = map.map().entrySet().iterator();
		while (iter.hasNext()) {
			final Entry<String, Object> entry = iter.next();
			builder.append(entry.getKey())
					.append(":")
					.append(objectToString(entry.getValue()));
			if (iter.hasNext()) {
				builder.append(",");
			}
		}

		builder.append("}");
		return builder.toString();
	}

	private static String objectToString(final Object obj) {
		if (obj == null) {
			return "null";
		} else {
			if (obj instanceof String) {
				return '"' + escape((String) obj) + '"';
			} else if (obj instanceof PropertyMap) {
				return toString((PropertyMap) obj);
			} else if (obj instanceof PropertyList) {
				return toString((PropertyList) obj);
			} else {
				// Inattendu, car normalement en interne une PropertyList|Map ne contient que les trois types précédemment testés
				// TODO (KLH) LOG.warn à prévoir ?
				return '"' + escape(obj.toString()) + '"';
			}
		}
	}

	private static String escape(final String str) {
		return str.replaceAll("\n", "\\n")
				.replaceAll("\"", "\\\"");
	}

}
