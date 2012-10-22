package anhoavu.utils.spectre;

import java.util.Map;
import java.util.TreeMap;

public class CommandLineArguments {

	/**
	 * General method to parse a String of command line arguments. This method
	 * will generate a map that associates switches to their value.
	 * 
	 * TODO consider boolean switch.
	 * 
	 * @param args
	 * @return
	 */
	public static Map<String, String> parseCommandLineArguments(String[] args) {
		Map<String, String> arg_map = new TreeMap<String, String>();
		String key = null, value = null;
		if (BuildConfig.DEBUG)
			System.out.println("TeX.parseCommandLineArguments : Parse "
					+ CollectionPrinter.stringOfObjectArray(args, " | "));

		for (int i = 0; i < args.length; i++) {
			// Special handling for the program name
			if (i == 0) {
				arg_map.put("$PROGNAME", args[0]);
				continue;
			}

			if (args[i].startsWith("--")) {
				// args[i] is a switch; we have three scenarios: either
				// args[i] is of form --<key> and args[i+1] indicates the
				// value assigned to this variable <key>; or
				// args[i] is of form --<key> and is a boolean switch (i.e.
				// either on/off, does not have any extra value); or
				// args[i] is of form --<key>=<value>
				int eq_sign = args[i].indexOf('=');
				if (eq_sign >= 0) {
					// args[i] is of form --<key>=<value>
					key = args[i].substring(0, eq_sign);
					value = args[i].substring(eq_sign + 1);
					arg_map.put(key, value);
				} else {
					// args[i] is of form --<key>, set it so that the next
					// argument will be
					// its value. The tricky part is that the argument itself
					// has "--"
					key = args[i];
				}
			} else {
				// If we have a <key> waiting for its argument, set this
				// argument as that key's value
				// Otherwise, accumulate this into a special argument $ARG
				if (key != null) {
					arg_map.put(key, args[i]);
					key = null;
				} else {
					arg_map.put("$ARG", args[i]);
				}
			}
		}

		if (BuildConfig.DEBUG)
			System.out.println("TeX.parseCommandLineArguments : Result = "
					+ CollectionPrinter.stringOfMap(arg_map));

		return arg_map;
	}
}
