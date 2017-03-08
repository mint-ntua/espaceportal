package annotators.struct;

import annotators.struct.AnnotationValue;

public class SimpleAnnotationValue extends AnnotationValue {
	private Object value;
	
	public SimpleAnnotationValue(Object value) {
		this.value = value;
	}
	
	public int hashCode() {
		return value.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleAnnotationValue)) {
			return false;
		}
		
		return ((SimpleAnnotationValue)obj).value.equals(value);
	}
	
	public String toString() {
		return value.toString();
	}

	public Object getValue() {
		return value;
	}
}
