package sources.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import model.basicDataTypes.ProvenanceInfo.Sources;
import play.Logger;
import play.Logger.ALogger;
import utils.SortedList;

public class CommonFilterLogic implements Cloneable {
	public static final ALogger log = Logger.of( CommonFilterLogic.class);
	
	private HashMap<String, ValueCount> counts = new HashMap<String, ValueCount>();

	public CommonFilterResponse data = new CommonFilterResponse();

	public CommonFilterLogic(CommonFilters filter) {
		this.data.filterID = filter.getId();
		this.data.filterName = filter.getText();
	}
	
	public CommonFilterLogic(CommonFilterResponse filter) {
		this.data.filterID = filter.filterID;
		this.data.filterName = filter.filterName;
		for ( ValueCount v : filter.suggestedValues) {
			counts.put(v.value, v);
		}
	}
	
	public CommonFilterLogic(String filter) {
		this.data.filterID = filter;
		this.data.filterName = filter;
	}
	
	public CommonFilterLogic(String filter, String text) {
		this.data.filterID = filter;
		this.data.filterName = text;
	}
	
	public void addValue(String value, int count) {
		if (value != null) {
			getOrSet(value).add(count);
		}
	}

	public void addValueCounts(Collection<ValueCount> value) {
		if (value != null) {
			for (ValueCount valueCount : value) {
				getOrSet(valueCount.value).add(valueCount.count);
			}
		}
	}

	private ValueCount getOrSet(String value) {
		if (!counts.containsKey(value)) {
			counts.put(value, new ValueCount(value, 0));
		}
		return counts.get(value);
	}

	public void addValue(Collection<String> values, int count) {
		for (String string : values) {
			addValue(string, count);
		}
	}


	@Override
	public String toString() {
		return "Filter [" + data.filterID + ", values=" + counts.values().size() + "]";
	}

	public CommonFilterResponse export() {
		data.suggestedValues = new SortedList<>(ValueCount.comparator());
		data.suggestedValues.addAll(counts.values());
		return data;
	}

	@Override
	protected CommonFilterLogic clone() {
		CommonFilterLogic res = new CommonFilterLogic(this.data.filterID);
		res.data.filterName = data.filterName;
		res.counts = (HashMap<String, ValueCount>) counts.clone();
		res.data.sources.addAll(data.sources);
		return res;
	}

	public Collection<ValueCount> values() {
		return counts.values();
	}
	
	public CommonFilterLogic addTo(List<CommonFilterLogic> list){
		list.add(this);
		return this;
	}

	public void addSourceFrom(CommonFilterLogic b) {
		addSourceFrom(b.data);
	}
	public void addSourceFrom(CommonFilterResponse b) {
		for (Sources s : b.sources) {
			data.addSource(s);	
		}
	}
}
