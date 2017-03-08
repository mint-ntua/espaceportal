package sources.utils;

import java.util.List;
import java.util.function.Function;

import sources.core.CommonFilters;
import sources.core.Utils;
import sources.core.Utils.Pair;
import utils.ListUtils;

public class FunctionsUtils {
	
	
	/**
	 * makes a function transforming a list of term1, term2,...termn 
	 * into a Pair of (parameter,"term1" OR "term2" ... OR "termn")
	 * @param parameter
	 * @see Pair
	 * @return
	 */
	public static Function<List<String>, Pair<String>> toORList(String parameter) {
		return toORList(parameter, true);
	}
	/**
	 * makes a function transforming a list of term1, term2,...termn 
	 * into a Pair of (parameter,newterm1 OR newterm2 ... OR newtermn).
	 * Where newterm_i will be the same termi or "termi" according to the value of parenthesis 
	 * @param parameter
	 * @param parenthesis
	 * @see Pair
	 * @return
	 */
	public static Function<List<String>, Pair<String>> toORList(String parameter, boolean quotes) {
		return toORList(parameter, (s)->quotes?("\""+s+"\""):s);
	}
	
	
	/**
	 * makes a function transforming a list of term1, term2,...termn 
	 * into a Pair of (parameter,newterm1 OR newterm2 ... OR newtermn).
	 * Where newterm_i will transform.apply(termi) 
	 * @param parameter
	 * @param transform
	 * @see Pair
	 * @return
	 */
	public static Function<List<String>, Pair<String>> toORList(String parameter, Function<String,String> transform) {
		return (list)-> new Pair<String>(parameter, 
						Utils.getORList(ListUtils.transform(list, transform), false));
	}
	
	public static Function<String, String> quote() {
		return (String s) -> "\"" + s + "\"";
	}
	public static Function<String, String> smartquote() {
		return (String s) -> {
			if (s.contains(" ")){
				return "\"" + s + "\"";
			} else
				return s;
		};
	}

}
