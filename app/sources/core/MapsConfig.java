package sources.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.basicDataTypes.ProvenanceInfo.Sources;
import play.Logger;
import play.Logger.ALogger;
import sources.FilterValuesMap;

public class MapsConfig {

	public static final ALogger log = Logger.of( MapsConfig.class);
	
	private static HashMap<String, HashMap<String, HashMap<String, List<String>> >  > map;
	private static void loadMap(){
		String json = "";
		ObjectMapper mapper = new ObjectMapper();
		//JSON from file to Object
		try {
			map = mapper.readValue(new File("conf/filtermaps.conf"), HashMap.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("error parsing filters maps config file: " + e.toString());
		}
	}
	public static FilterValuesMap buildFilterValuesMap(Sources source){
		String sourceID = source.toString();
		FilterValuesMap res = new FilterValuesMap();
		HashMap<String, HashMap<String, List<String>>> hashMap = getmap().get(sourceID);
		if (hashMap==null){
			for (Entry<String, HashMap<String, HashMap<String, List<String>>>> e : getmap().entrySet()) {
				if (sourceID.matches(e.getKey())){
					hashMap= e.getValue();
					break;
				}
			}
		}
		if (hashMap!=null)
		for (String filterID : hashMap.keySet()) {
			for (Entry<String, List<String>> entry : hashMap.get(filterID).entrySet()) {
				res.addMap(filterID, entry.getKey(), entry.getValue());	
			}
		}
		return res;
	}
	private static HashMap<String, HashMap<String, HashMap<String, List<String>>>> getmap() {
		if (map==null)
			loadMap();
		return map;
	}
}
