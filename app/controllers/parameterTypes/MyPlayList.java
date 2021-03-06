package controllers.parameterTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Logger;
import play.Logger.ALogger;
import play.libs.F.Option;
import play.mvc.QueryStringBindable;

public class MyPlayList implements QueryStringBindable<MyPlayList>{

	public List<StringTuple> list = new ArrayList<StringTuple>();
	public static final ALogger log = Logger.of(MyPlayList.class);

	// play framework requires the Bindable to provide a "no Argument" public constructor.
	public MyPlayList() {
	}

	@Override
	public Option<MyPlayList> bind(String key, Map<String, String[]> data) {
		String[] vs = data.get(key);
	    if ((vs != null) && (vs.length > 0)) {
	        String v = vs[0];
			try {
				JsonNode actualObj = new ObjectMapper().readTree(v);
				if (actualObj.isArray()) {
					for (final JsonNode o: actualObj) {
						String[] tupleValue = new String[1];
			        	tupleValue[0] = o.toString();
			        	HashMap<String, String[]> tupleMap = new HashMap<String, String[]>(1);
			        	tupleMap.put(key, tupleValue);
			        	StringTuple x = new StringTuple();
			        	Option<StringTuple> tuple = x.bind(key, tupleMap);
			        	if (tuple.isDefined()) {
			        		list.add(tuple.get());
			        	}
					}
					return Option.Some(this);
				}
				else
					return Option.Some(null);
			} catch (JsonProcessingException e) {
				log.error( "Json problem.",e);
			} catch (IOException e) {
				log.error("IOProblem", e);
			}
	    }
	    return Option.None();
	}
	@Override
	public String javascriptUnbind() {
		return "function(k,v) {return encodeURIComponent(k)+'='+encodeURIComponent(v)}";
	}
	@Override
	public String unbind(String key) {
		String listString = "[";
		HashMap<String, String[]> tupleMap = new HashMap<String, String[]>(1);
		for (int i=0; i <list.size(); i++){
			StringTuple st = list.get(i);
			listString+=st.unbind(key);
			if (i < (list.size() -1))
				listString+=",";
		}
		return listString+"]";
	}
}
