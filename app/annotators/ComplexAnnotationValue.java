package annotators;

import annotators.struct.AnnotatedObject;
import annotators.struct.AnnotationValue;

public class ComplexAnnotationValue extends AnnotationValue {
	private Object[] value;
	
	public ComplexAnnotationValue(Object... vv) {
		this.value = vv;
	}
	
	public int hashCode() {
		return value.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof ComplexAnnotationValue)) {
			return false;
		}
		
		Object[] v2 = ((ComplexAnnotationValue)obj).value;
		
		if (v2.length != value.length) {
			return false;
		}
		
		for (int i = 0; i < value.length; i++) {
			if (!value[i].equals(v2[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	public String toString() {
		String s = "[";
		for (int i = 0; i < value.length; i++) {
			if (i > 0) {
				s += ", ";
			}
			if (value[i] instanceof AnnotatedObject) {
				s += "@" + ((AnnotatedObject)value[i]).getID() + ":" + ((AnnotatedObject)value[i]).getText() + "@";
			} else {
				s += value[i].toString();
			}
		}
		
		return s + "]";
	}

	public Object getValue() {
		return value;
	}

}
