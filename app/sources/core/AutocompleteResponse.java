package sources.core;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;

import com.fasterxml.jackson.databind.JsonNode;

//everythign public and in one place for brevity
public class AutocompleteResponse {
	
    public static class Suggestion {
    	public String value;
    	public DataJSON data;
    }
	public static class DataJSON {
		public String category;
		public int frequencey = -1;
		public String field = "";
	}
	
	
	public List<Suggestion> suggestions;
}
