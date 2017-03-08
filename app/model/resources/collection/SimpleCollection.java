package model.resources.collection;

import org.mongodb.morphia.annotations.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import model.resources.collection.CollectionObject.CollectionDescriptiveData;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@Entity("CollectionObject")
public class SimpleCollection extends CollectionObject<CollectionDescriptiveData> {

	public SimpleCollection() {
		super();
		this.descriptiveData = new CollectionDescriptiveData();
		this.resourceType = WithResourceType.SimpleCollection;
	}

}
