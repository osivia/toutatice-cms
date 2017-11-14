package org.nuxeo.ecm.automation.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.nuxeo.ecm.automation.client.model.DateUtils;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Version corrigée de {@link PropertyList}.
 * <p>
 * Les corrections sont les suivantes :
 * <ul>
 * <li>La classe implémente l'interface {@link List List&lt;Object&gt;}
 * <li>Fournir une valeur {@code null} à la méthode {@code set(int, Object)} ne risque pas de déclencher une {@link NullPointerException}. {@code null} n'est jamais inséré dans la
 * liste. Cette méthode surcharge normalement, du fait de sa signature, l'ensemble des méthodes {@code set(int, T)} de la classe parente.
 * <li>Des méthodes {@link #equals(Object)} et {@link #hashCode()} ont été définies qui prennent en compte l'état de l'objet.
 * <li>La méthode {@link #toString()} produit une chaîne de caractères dans le format utilisé pour les échanges via l'API Nuxeo.
 * </ul>
 * <p>
 * Un point sur lequel faire particulièrement attention avec cette classe, c'est qu'avant de stocker une valeur qui lui est passée, à moins que cette valeur soit de type
 * {@code PropertyList}, ou {@code PropertyMap}, une conversion vers {@code String} est effectuée. Cela signifie que du point de vue des instances de {@code NuxeoPropertyList},
 * {@code 18} et {@code "18"} sont équivalents.
 *
 * @author Kévin Le Helley
 * @see PropertyList
 */
public final class NuxeoPropertyList extends PropertyList implements List<Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 */
	public NuxeoPropertyList() {
		super();
	}

	/**
	 * Constructeur avec initialisation.
	 *
	 * @param list
	 *            la liste à utiliser pour l'initialisation
	 */
	public NuxeoPropertyList(final PropertyList list) {
		super(list.list());
	}

	/**
	 * Constructeur avec initialisation.
	 *
	 * @param list
	 *            la liste à utiliser pour l'initialisation
	 */
	public NuxeoPropertyList(final List<Object> list) {
		super(list);
	}

	/**
	 * Constructeur.
	 *
	 * @param size
	 *            la taille initiale de la liste
	 */
	public NuxeoPropertyList(final int size) {
		super(size);
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

	private Collection<?> toInternalFormatValues(final Collection<?> c) {
		final List<Object> converted = new ArrayList<Object>(c.size());
		for (final Object o : c) {
			if (o != null) {
				converted.add(toInternalFormatValue(o));
			}
		}
		return converted;
	}

	/**
	 * Place un objet à un index donné. Si la valeur est {@code null}, aucune modification n'est effectuée.
	 *
	 * @param i
	 *            l'index où l'objet doit être placé
	 * @param value
	 *            l'objet à placer
	 * @return le précédent occupant pour l'index fourni ; {@code null} s'il n'y en avait pas
	 */
	@Override
	public Object set(final int i, final Object value) {
		if (value == null) {
			return null;
		} else {
			return list.set(i, toInternalFormatValue(value));
		}
	}

	@Override
	public boolean contains(final Object o) {
		return list.contains(toInternalFormatValue(o));
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(final Object e) {
		if (e == null) {
			return false;
		} else {
			return list.add(toInternalFormatValue(e));
		}
	}

	@Override
	public boolean remove(final Object o) {
		if (o == null) {
			return false;
		} else {
			return list.remove(toInternalFormatValue(o));
		}
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return list.containsAll(toInternalFormatValues(c));
	}

	@Override
	public boolean addAll(final Collection<? extends Object> c) {
		return list.addAll(toInternalFormatValues(c));
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return list.removeAll(toInternalFormatValues(c));
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return list.retainAll(toInternalFormatValues(c));
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends Object> c) {
		return list.addAll(index, toInternalFormatValues(c));
	}

	@Override
	public Object get(final int index) {
		return list.get(index);
	}

	@Override
	public void add(final int index, final Object element) {
		if (element != null) {
			list.add(index, toInternalFormatValue(element));
		}
	}

	@Override
	public Object remove(final int index) {
		return list.remove(index);
	}

	@Override
	public int indexOf(final Object o) {
		if (o == null) {
			return -1;
		} else {
			return list.indexOf(toInternalFormatValue(o));
		}
	}

	@Override
	public int lastIndexOf(final Object o) {
		if (o == null) {
			return -1;
		} else {
			return list.lastIndexOf(toInternalFormatValue(o));
		}
	}

	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(final int index) {
		return list.listIterator(index);
	}

	@Override
	public List<Object> subList(final int fromIndex, final int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public int hashCode() {
		return list.hashCode();
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
		final NuxeoPropertyList other = (NuxeoPropertyList) obj;
		return list.equals(other.list);
	}

	@Override
	public String toString() {
		return NuxeoJsonMapper.toString(this);
	}

}
