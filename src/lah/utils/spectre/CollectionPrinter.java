package lah.utils.spectre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Various static methods to obtain {@link String} representation for
 * collection-kind objects like array, list, map, ...
 * 
 * @author Vu An Hoa
 * 
 */
public class CollectionPrinter {

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
	public static String stringOfArray(Object[] objs, String sep,
			String prefix, String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (objs != null) {
			boolean first_element = true;
			for (Object o : objs) {
				res.append((first_element ? "" : sep));
				res.append(o.toString());
				first_element = false;
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
	public static String stringOfCollection(Collection<?> collection,
			String sep, String prefix, String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (collection != null) {
			boolean first_element = true;
			for (Object elem : collection) {
				res.append((first_element ? "" : sep));
				res.append(elem.toString());
				first_element = false;
			}
		}
		return res.append(suffix == null ? "" : suffix).toString();
	}

	/**
	 * Get a string representation for a list of object
	 * 
	 * @param list
	 * @param sep
	 * @return
	 */
	public static String stringOfList(List<?> list, String sep, String prefix,
			String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (list != null) {
			boolean first_element = true;
			for (Object e : list) {
				res.append((first_element ? "" : sep));
				res.append(e.toString());
				first_element = false;
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
	public static <K, V> String stringOfMap(Map<K, V> map, String sep,
			String mapto, String prefix, String suffix) {
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

	/**
	 * Get a string representation of a set of objects
	 * 
	 * @param set
	 * @param sep
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static String stringOfSet(Set<?> set, String sep, String prefix,
			String suffix) {
		StringBuilder res = new StringBuilder(prefix == null ? "" : prefix);
		if (set != null) {
			boolean first_element = true;
			for (Object m : set) {
				res.append((first_element ? "" : sep));
				res.append(m.toString());
				first_element = false;
			}
		}
		return res.append(suffix == null ? "" : suffix).toString();
	}

}
