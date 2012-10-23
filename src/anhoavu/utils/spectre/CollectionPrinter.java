package anhoavu.utils.spectre;

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
	 * Concatenate a list of object
	 * 
	 * @param list
	 * @param sep
	 * @return
	 */
	public static String concatList(List<?> list, String sep) {
		if (list == null)
			return null;

		switch (list.size()) {
		case 0:
			return "{}";
		case 1:
			return "{" + list.get(0).toString() + "}";
		default:
			StringBuilder result = new StringBuilder("{"
					+ list.get(0).toString());
			for (int i = 1; i < list.size(); i++) {
				result.append(sep).append(list.get(i).toString());
			}
			return result.append("}").toString();
		}
	}

	/**
	 * Concat all strings in a list of string using a specified separator
	 * 
	 * @param strs
	 * @param sep
	 * @return
	 */
	public static String concatListStrings(List<String> strs, String sep) {
		if (strs != null) {
			int num_str = strs.size();

			if (num_str == 0)
				return "";

			StringBuilder res = new StringBuilder();
			res.append(strs.get(0));
			for (int i = 1; i < num_str; i++) {
				res.append(sep + strs.get(i));
			}
			return res.toString();
		}
		return null;
	}

	public static String stringOfMap(Map<?, ?> map) {
		StringBuilder res = new StringBuilder();
		Set<?> keys = map.keySet();
		for (Object k : keys) {
			Object v = map.get(k);
			res.append("\"" + k.toString() + "\" --> \"" + v.toString()
					+ "\" | ");
		}
		return "{" + res.toString() + "}";
	}

	/**
	 * Get a string representation for an array of objects
	 * 
	 * @param strs
	 * @param sep
	 * @return
	 */
	public static String stringOfObjectArray(Object[] strs, String sep) {
		StringBuilder res = new StringBuilder();
		for (Object s : strs)
			res.append(s + sep);
		return res.toString();
	}

	public static String stringOfSet(Set<?> set, String sep) {
		StringBuilder res = new StringBuilder("{");
		boolean first_element = true;
		for (Object m : set) {
			res.append((first_element ? "" : sep));
			res.append(m.toString());
			first_element = false;
		}
		return res.append("}").toString();
	}

}
