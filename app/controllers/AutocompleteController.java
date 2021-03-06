package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import play.Logger;
import play.Logger.ALogger;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Result;
import sources.core.AutocompleteResponse;
import sources.core.AutocompleteResponse.Suggestion;
import sources.core.ESpaceSources;
import sources.core.ISpaceSource;
import sources.core.ParallelAPICall;

public class AutocompleteController extends WithController {

	public static final ALogger log = Logger.of(AutocompleteController.class);

	public static Promise<Result> autocompleteExt(String term, Integer limit, List<String> sourceFromUI) {
		List<ISpaceSource> sourcesForAutocomplete = new ArrayList<ISpaceSource>();
		if (sourceFromUI.isEmpty())
			sourcesForAutocomplete = ESpaceSources.getESources();
		else {
			for (ISpaceSource s: ESpaceSources.getESources()) {
				if (sourceFromUI.contains(s.getSourceName()))
					sourcesForAutocomplete.add(s);
			}
		}
		return getSuggestionsResponse(sourcesForAutocomplete, term, limit);
	}

	//the union of the suggestions collected from the sources APIs is returned
	//if no source returns suggestions, then empty content is returned
	@BodyParser.Of(BodyParser.Json.class)
	private static Promise<Result> getSuggestionsResponse(List<ISpaceSource> sourcesWithAutocomplete, final String term, int limit) {
		BiFunction<String, ISpaceSource, AutocompleteResponse> methodQuery = (String autocompleteQuery, ISpaceSource src) -> {
				try {
					URL url = new URL(autocompleteQuery.replace(" ", "%20"));
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			        conn.setRequestMethod("GET");
			        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			        StringBuffer response = new StringBuffer();
			        String line = "";
			        while ((line = rd.readLine()) != null) {
			            response.append(line);
			        }
			        rd.close();
			        /*
			         * Hack-ish FIX. The problem is that the JSON response from
			         * the request to Europeana is not well-formatted or is empty.
			         * Enrique need to check that.
			         */
			        if(response.toString().equals(""))
			        	response.append("{ \"success\": false }");
			        //transform response into standard json
			        AutocompleteResponse standardResponse = src.autocompleteResponse(response.toString());;
			        return standardResponse;
				} catch (IOException e) {
					log.error( "Autocomplete problem.", e );
					return new AutocompleteResponse();
				}
		};

		Iterable<Promise<AutocompleteResponse>> promises = new ArrayList<Promise<AutocompleteResponse>>();
		for (final ISpaceSource source: sourcesWithAutocomplete) {
			final String autocompleteQuery = source.autocompleteQuery(term, limit);
			if (!autocompleteQuery.isEmpty()) {
				((ArrayList<Promise<AutocompleteResponse>>) promises).add(
					ParallelAPICall.createPromise(methodQuery, autocompleteQuery, source)
				);
			}
		}

		Function<AutocompleteResponse, Boolean> responseCollectionMethod =
				(AutocompleteResponse response) -> ((response.suggestions !=null) && !response.suggestions.isEmpty());

		Function<List<AutocompleteResponse>, List<AutocompleteResponse>> filter = (List<AutocompleteResponse> response) -> {
			List<AutocompleteResponse> finalResponses = new ArrayList<AutocompleteResponse>();
			List<Suggestion> filteredSuggestions = new ArrayList<Suggestion>();
			for (AutocompleteResponse r: response) {
				List<Suggestion> sugg = r.suggestions;
				filteredSuggestions.addAll(sugg);
			}
			List<Suggestion> outSugg = filteredSuggestions.stream().filter(distinctByValue(p -> p.value)).collect(Collectors.toList());;
			AutocompleteResponse ar = new AutocompleteResponse();
			ar.suggestions = outSugg;
			finalResponses.add(ar);
			return finalResponses;
		};

		return ParallelAPICall.<AutocompleteResponse>combineResponses(responseCollectionMethod, promises, filter);
	}


	private static Predicate<Suggestion> distinctByValue(Function<Suggestion, String> s) {
		Set<String> seen = new HashSet<String>();
		return t -> {
			boolean contains = seen.contains(t.value);
			seen.add(t.value);
			return !contains;//return (seen.putIfAbsent(t.value, Boolean.TRUE) == null);};
		};
	}

}
