package model.annotations.targets;

import model.annotations.selectors.SelectorType;

import org.bson.types.ObjectId;

import utils.Deserializer;
import utils.Serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class AnnotationTarget implements Cloneable {

	@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
	private ObjectId recordId;
	/**
	 * The withURI to which the annotation refers.
	 */
	private String withURI;

	/**
	 * The external id of the object to which the annotations refers.
	 */
	private String externalId;
	
	@JsonDeserialize(using = Deserializer.SelectorTypeDeserializer.class)
	private SelectorType selector;

	public ObjectId getRecordId() {
		return recordId;
	}

	public void setRecordId(ObjectId recordId) {
		this.recordId = recordId;
	}

	public String getWithURI() {
		return withURI;
	}

	public void setWithURI(String withURI) {
		this.withURI = withURI;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public SelectorType getSelector() {
		return selector;
	}

	public void setSelector(SelectorType selector) {
		this.selector = selector;
		
	}
	
	@Override
    public Object clone() {
		try {
			AnnotationTarget c = (AnnotationTarget)super.clone();
			c.externalId = this.externalId;
			c.recordId = this.recordId;
			c.withURI = this.withURI;
			
			c.selector = (SelectorType)selector.clone();
			
			return c;
		} catch (CloneNotSupportedException e) {
			return null;
		}
    }

}
