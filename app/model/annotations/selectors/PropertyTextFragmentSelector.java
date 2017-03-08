package model.annotations.selectors;

import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.annotation.JsonInclude;

import model.annotations.Annotation;
import model.basicDataTypes.Language;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PropertyTextFragmentSelector extends PropertySelector {
	
	private String origValue;
	private Language origLang;
	
	private int start;
	private int end;
	
	public int getStart() {
		return start;
	}
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public void setEnd(int end) {
		this.end = end;
	}

	public String getOrigValue() {
		return origValue;
	}

	public void setOrigValue(String origValue) {
		this.origValue = origValue;
	}

	public Language getOrigLang() {
		return origLang;
	}

	public void setOrigLang(Language origLang) {
		this.origLang = origLang;
	}
	
	@Override
    public Object clone() throws CloneNotSupportedException {
		PropertyTextFragmentSelector c = (PropertyTextFragmentSelector)super.clone();
		c.origLang = origLang;
		c.origValue = origValue;

		return c;
    }
	
	@Override
	public void addToQuery(Query<Annotation> q) {
		super.addToQuery(q);
		
		q.field("target.selector.start").equal(start);
		q.field("target.selector.end").equal(end);
		
		if (origValue != null) {
			q.field("target.selector.origValue").equal(origValue);
		}
		
		if (origLang != null) {
			q.field("target.selector.origLang").equal(origLang);
		}
	}



}
