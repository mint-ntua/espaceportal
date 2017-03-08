package model.annotations.selectors;

import model.annotations.Annotation;

import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PropertySelector extends SelectorType {
	
	private String property;

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	} 
	
	@Override
    public Object clone() throws CloneNotSupportedException {
		PropertySelector c = (PropertySelector)super.clone();
		c.property = property;
		
		return c;
    }
	
	@Override
	public void addToQuery(Query<Annotation> q) {
		q.field("target.selector.property").equal(property);
	}

}
