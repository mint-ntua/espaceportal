package model.basicDataTypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.utils.IndexType;

import utils.Serializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CollectionInfo {
	@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
	private ObjectId collectionId;
	private Integer position;

	public CollectionInfo() {
	}

	//position is Integer instead of int, so that we can do the null trick with morphia (see CommonResourcesDAO)
	public CollectionInfo(ObjectId collectionId, Integer position) {
		this.collectionId = collectionId;
		this.position = position;
	}

	public ObjectId getCollectionId() {
		return collectionId;
	}
	public void setCollectionId(ObjectId collectionId) {
		this.collectionId = collectionId;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}

}
