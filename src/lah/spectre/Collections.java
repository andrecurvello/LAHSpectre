package lah.spectre;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Various static array and collection manipulation methods such as obtain human--friendly {@link String} representation
 * select a sub--collection etc
 * 
 * @author L.A.H.
 * 
 */
public class Collections {

	public static <T> boolean[] getMembershipIndicator(T[] all_values, Collection<T> selected_values) {
		if (all_values == null || selected_values == null)
			return null;
		boolean[] selected = new boolean[all_values.length];
		Set<T> set_of_selected_values = setOfCollection(selected_values);
		for (int i = 0; i < all_values.length; i++)
			selected[i] = set_of_selected_values.contains(all_values[i]);
		return selected;
	}

	public static <T> boolean[] getMembershipIndicator(T[] all_values, T[] selected_values) {
		if (all_values == null || selected_values == null)
			return null;
		boolean[] selected = new boolean[all_values.length];
		Set<T> set_of_selected_values = setOfArray(selected_values);
		for (int i = 0; i < all_values.length; i++)
			selected[i] = set_of_selected_values.contains(all_values[i]);
		return selected;
	}

	public static <T> Set<T> setOfArray(T[] array) {
		Set<T> result = new HashSet<T>();
		for (T member : array)
			result.add(member);
		return result;
	}

	public static <T> Set<T> setOfCollection(Iterable<T> collection) {
		Set<T> result = new HashSet<T>();
		for (T member : collection)
			result.add(member);
		return result;
	}

	/**
	 * Get a string representation for an array of objects
	 * 
	 * @param objs
	 *            The array of objects
	 * @param sep
	 *            The separator to insert between objects
	 * @param prefix
	 *            Prefix of string result
	 * @param suffix
	 *            Suffix of string result
	 * @return
	 */
	public static String stringOfArray(Object[] objs, String sep, String prefix, String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (objs != null) {
			boolean first_element = true;
			for (Object o : objs) {
				if (o != null) {
					res.append((first_element ? "" : sep));
					res.append(o.toString());
					first_element = false;
				}
			}
		}
		return res.append(suffix == null ? "" : suffix).toString();
	}

	/**
	 * Get a string representation for a collection of object
	 * 
	 * @param collection
	 * @param sep
	 * @return
	 */
	public static String stringOfCollection(Collection<?> collection, String sep, String prefix, String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (collection != null) {
			boolean first_element = true;
			for (Object elem : collection) {
				if (elem != null) {
					res.append((first_element ? "" : sep));
					res.append(elem.toString());
					first_element = false;
				}
			}
		}
		return res.append(suffix == null ? "" : suffix).toString();
	}

	/**
	 * Get a string representation of a key-value map
	 * 
	 * @param map
	 *            The map to get string representation
	 * @param sep
	 *            The string to separate key-value pairs, usually comma
	 * @param mapto
	 *            The string to insert between key and value, usually "->"
	 * @param prefix
	 *            Prefix of the output string
	 * @param suffix
	 *            Suffix of the output string
	 * @return
	 */
	public static <K, V> String stringOfMap(Map<K, V> map, String sep, String mapto, String prefix, String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (map != null) {
			Set<Map.Entry<K, V>> entries = map.entrySet();
			boolean first_element = true;
			for (Map.Entry<K, V> e : entries) {
				K key = e.getKey();
				V value = e.getValue();
				res.append((first_element ? "" : sep));
				res.append(key.toString());
				res.append(mapto);
				res.append(value.toString());
				first_element = false;
			}
		}
		return res.append(suffix == null ? "" : suffix).toString();
	}

}
