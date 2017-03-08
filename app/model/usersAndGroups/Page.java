package model.usersAndGroups;

import java.util.HashMap;
import java.util.List;

import model.EmbeddedMediaObject.MediaVersion;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.geo.Point;
import org.mongodb.morphia.utils.IndexType;

import utils.Serializer.PointSerializer;
import utils.Serializer.ObjectIdSerializer;
import utils.Serializer.ObjectIdArraySerializer;
import utils.Deserializer.PointDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Page {

	private String address;

	@JsonDeserialize(using = PointDeserializer.class)
	@JsonSerialize(using = PointSerializer.class)
	private Point coordinates;

	private String city;
	private String country;
	private String url;

	private HashMap<MediaVersion, String> cover;

	@JsonSerialize(using = ObjectIdArraySerializer.class)
	private List<ObjectId> featuredCollections;
	@JsonSerialize(using = ObjectIdArraySerializer.class)
	private List<ObjectId> featuredExhibitions;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Point getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}

	public List<ObjectId> getFeaturedCollections() {
		return featuredCollections;
	}

	@JsonIgnore
	public void setFeaturedCollections(List<ObjectId> featuredCollections) {
		this.featuredCollections = featuredCollections;
	}

	public void addFeaturedCollection(ObjectId featuredCollection) {
		this.featuredCollections.add(featuredCollection);
	}

	public List<ObjectId> getFeaturedExhibitions() {
		return featuredExhibitions;
	}

	@JsonIgnore
	public void setFeaturedExhibitions(List<ObjectId> featuredExhibitions) {
		this.featuredExhibitions = featuredExhibitions;
	}

	public void addFeaturedExhibition(ObjectId featuredExhibition) {
		this.featuredExhibitions.add(featuredExhibition);
	}

	public HashMap<MediaVersion, String> getCover() {
		return cover;
	}

	public void setCover(HashMap<MediaVersion, String> cover) {
		this.cover = cover;
	}

}
