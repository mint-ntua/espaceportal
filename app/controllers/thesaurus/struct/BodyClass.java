package controllers.thesaurus.struct;

import model.basicDataTypes.MultiLiteral;

public class BodyClass {
	public String uri;
	public MultiLiteral label;
	public int counter;
	
	public BodyClass(String uri, MultiLiteral label) {
		this.uri = uri;
		this.label = label;
	}

	public int hashCode() {
		return uri.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof BodyClass)) {
			return false;
		}
		
		return uri.equals(((BodyClass)obj).uri);
	}
}