package sources.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.basicDataTypes.ProvenanceInfo.Sources;

public class CommonFilterResponse {
	public String filterName;
	public String filterID;
	public List<Sources> sources;
	public List<ValueCount> suggestedValues;

	public CommonFilterResponse() {
		sources = new ArrayList<>();
	}
	
	public CommonFilterResponse(String filterID, String filterName) {
		super();
		this.filterID = filterID;
		this.filterName = filterName;
	}

	public CommonFilterResponse(String filterID) {
		super();
		this.filterID = filterID;
		this.filterName = filterID;
	}

	@Override
	public String toString() {
		return "CommonFilterResponse [filterName=" + filterName.toString() + ", suggestedValues=" + suggestedValues.size() + "]";
	}
	
	public void addSource(Sources s){
		if (!sources.contains(s)){
			sources.add(s);
		}
	}

}