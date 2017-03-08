package controllers.thesaurus.struct;

import model.basicDataTypes.MultiLiteral;

public class SortClass implements Comparable<SortClass> {
	public String uri;
	public MultiLiteral label;
	public int count;
	
	public SortClass(String uri, MultiLiteral label, int count) {
		this.uri = uri;
		this.label = label;
		this.count = count;
	}

	@Override
	public int compareTo(SortClass so) {
		if (count < so.count) {
			return 1;
		} else if (count > so.count) {
			return -1;
		} else {
			return 0;
		}
	}
}
