package model.annotations.selectors;

import org.mongodb.morphia.query.Query;

import model.annotations.Annotation;
import model.basicDataTypes.Language;

public abstract class SelectorType implements Cloneable {

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public abstract void addToQuery(Query<Annotation> q);
}
