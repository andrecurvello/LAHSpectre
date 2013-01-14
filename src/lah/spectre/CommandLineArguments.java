package lah.spectre;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineArguments {

	/**
	 * Regular expression pattern for each argument, basically, there are three
	 * cases:
	 * 
	 * 1) single quoted argument '[any except single quote character]*'
	 * 
	 * 2) double quoted argument "[any except double quote character]*"
	 * 
	 * 3) any number of non-white-space characters or backslash escaped
	 * white-space.
	 * 
	 * Note that this pattern does not consider Bash meta-characters such as `|`
	 * (pipe), `&` (execute in background), `;` (command sequence separator),
	 * `(`, `)` (command grouping), `>`, `|>` (output redirection), `<` (input
	 * redirection) and control characters `&&` (logical and), `||` (logical or)
	 */
	public static final Pattern argument_pattern = Pattern
			.compile("('[^']*'|\"[^\"]*\"|([^\\s]|\\\\s)+)\\s*");

	/**
	 * Parse a single <em>command</em> (not a shell or batch <em>script</em>) to
	 * a {@link String} array of arguments. The command could contain single
	 * quote, double quote or generic quote using backslash (\).
	 * 
	 * @param command
	 *            The command line to parse, it should be of form
	 *            {@code arg_0 arg_1 arg_2 ... arg_n} where arg_0 should be a
	 *            program name (or absolute path) and arg_i are arguments passed
	 *            to it. The arguments are white space (space or tab) separated
	 *            and it can be quoted.
	 * @return The array containing the arguments or {@literal null} if command
	 *         is {@literal null} or we cannot match the whole command, for
	 *         example, due to syntax error
	 */
	public static String[] getArgs(String command) {
		if (command == null)
			return null;

		Matcher arg_matcher = argument_pattern.matcher(command);
		LinkedList<String> args = new LinkedList<String>();
		int e = -1;
		while (arg_matcher.find()) {
			args.add(arg_matcher.group(1));
			e = arg_matcher.end(0);
		}

		// if we cannot match the whole command, this means there is error
		return (e < command.length()) ? null : args.toArray(new String[args
				.size()]);
	}

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
			System.out.println("parseCommandLineArguments : Parse "
					+ CollectionPrinter.stringOfArray(args, " | ", "[ ", " ]"));

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
			System.out.println("parseCommandLineArguments : Result = "
					+ CollectionPrinter.stringOfMap(arg_map, ", ", "-->", "[ ",
							" ]"));

		return arg_map;
	}
}
