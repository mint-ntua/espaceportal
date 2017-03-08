package model.usersAndGroups;

import java.util.Date;
import java.util.HashMap;

import model.EmbeddedMediaObject.MediaVersion;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

import utils.Deserializer;
import utils.Serializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class UserOrGroup {

	@Id
	@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
	private ObjectId dbId;
	private String username;
	private HashMap<MediaVersion, String> avatar;
	private String about;
	@JsonSerialize(using = Serializer.DateSerializer.class)
	@JsonDeserialize(using = Deserializer.DateDeserializer.class)
	private Date created;

	public ObjectId getDbId() {
		return dbId;
	}

	public void setDbId(ObjectId dbId) {
		this.dbId = dbId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String name) {
		this.username = name;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public HashMap<MediaVersion, String> getAvatar() {
		return avatar;
	}

	public void setAvatar(HashMap<MediaVersion, String> avatar) {
		this.avatar = avatar;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}
