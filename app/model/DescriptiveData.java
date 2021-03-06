package model;

import java.util.List;

import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.utils.IndexType;

import utils.Deserializer.PointDeserializer;
import utils.Serializer.PointSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.MultiLiteral;
import model.basicDataTypes.MultiLiteralOrResource;
import model.basicDataTypes.WithDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)

//TODO: check how to index MultiLiteral in mongo. Actually only need to query based on the "default" value.
@DescriptiveDataAnnotation
public class DescriptiveData {

	public static enum Quality {
		POOR, AVERAGE, GOOD, EXCELLENT
	}

	public DescriptiveData() {
	}

	public DescriptiveData(MultiLiteral label) {
		this.label = label;
	}

	// one line content description with identifiable characteristic
	//@NotNull
	//@Pattern(regexp="[^\\s]")
	private MultiLiteral label;

	// arbitrary length content description
	private MultiLiteral description;

	// an indexers dream !! They can be literal concepts and enriched easily
	private MultiLiteralOrResource keywords;

	// This are reachable URLs
	private LiteralOrResource isShownAt, isShownBy;

	// The whole legal bla, unedited, from the source, mostly cc0
	private LiteralOrResource metadataRights;

	// rdf .. Agent, Artist, Painter, Painting, Series
	private String rdfType;

	// URIs how this Resource is known elsewhere
	private MultiLiteralOrResource sameAs;

	// in a timeline where would this resource appear
	private List<WithDate> dates;

	// alternative title or name or placename
	private MultiLiteral altLabels;

	// The country at which the resource is currently located
	//in case of Agents, Places, Timespan
	private MultiLiteralOrResource country;

	// The city at which the resource is currently located
	//in case of Agents, Places, Timespan
	private MultiLiteralOrResource city;

	@JsonDeserialize(using = PointDeserializer.class)
	@JsonSerialize(using = PointSerializer.class)
	private org.mongodb.morphia.geo.Point coordinates;

	private Quality metadataQuality;


	public MultiLiteral getLabel() {
		return label;
	}

	public void setLabel(MultiLiteral label) {
		this.label = label;
	}

	public MultiLiteral getDescription() {
		return description;
	}

	public void setDescription(MultiLiteral description) {
		this.description = description;
	}

	public MultiLiteralOrResource getKeywords() {
		return keywords;
	}

	public void setKeywords(MultiLiteralOrResource keywords) {
		this.keywords = keywords;
	}

	public LiteralOrResource getIsShownAt() {
		return isShownAt;
	}

	public void setIsShownAt(LiteralOrResource isShownAt) {
		this.isShownAt = isShownAt;
	}

	public LiteralOrResource getIsShownBy() {
		return isShownBy;
	}

	public void setIsShownBy(LiteralOrResource isShownBy) {
		this.isShownBy = isShownBy;
	}

	public LiteralOrResource getMetadataRights() {
		return metadataRights;
	}

	public void setMetadataRights(LiteralOrResource metadataRights) {
		this.metadataRights = metadataRights;
	}

	public String getRdfType() {
		return rdfType;
	}

	public void setRdfType(String rdfType) {
		this.rdfType = rdfType;
	}

	public MultiLiteralOrResource getSameAs() {
		return sameAs;
	}

	public void setSameAs(MultiLiteralOrResource sameAs) {
		this.sameAs = sameAs;
	}

	public List<WithDate> getDates() {
		return dates;
	}

	public void setDates(List<WithDate> dates) {
		this.dates = dates;
	}

	public MultiLiteral getAltLabels() {
		return altLabels;
	}

	public void setAltLabels(MultiLiteral altLabels) {
		this.altLabels = altLabels;
	}

	public MultiLiteralOrResource getCountry() {
		return country;
	}

	public void setCountry(MultiLiteralOrResource country) {
		this.country = country;
	}

	public MultiLiteralOrResource getCity() {
		return city;
	}

	public void setCity(MultiLiteralOrResource city) {
		this.city = city;
	}

	public org.mongodb.morphia.geo.Point getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(org.mongodb.morphia.geo.Point coordinates) {
		this.coordinates = coordinates;
	}

	public Quality getMetadataQuality() {
		return metadataQuality;
	}

	public void setMetadataQuality(Quality metadataQuality) {
		this.metadataQuality = metadataQuality;
	}
}
