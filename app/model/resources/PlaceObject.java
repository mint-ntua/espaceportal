package model.resources;

import java.util.ArrayList;

import org.mongodb.morphia.annotations.Entity;

import model.DescriptiveData;
import model.basicDataTypes.MultiLiteralOrResource;
import model.resources.RecordResource.RecordDescriptiveData;
import model.resources.WithResource.WithResourceType;

@Entity("RecordResource")
public class PlaceObject extends RecordResource<PlaceObject.PlaceData> {

	public PlaceObject() {
		super();
		this.resourceType = WithResourceType.valueOf(this.getClass().getSimpleName());
	}

	public static class PlaceData extends RecordDescriptiveData {

		// city, archeological site, area, nature reserve, historical site
		MultiLiteralOrResource nation;
		MultiLiteralOrResource continent;
		MultiLiteralOrResource partOfPlace;
		
		Double wgsposlat, wgsposlong, wgsposalt;
		
		// in meters how in accurate the position is
		// also describes the extend of the position
		Double accuracy;

		public MultiLiteralOrResource getNation() {
			return nation;
		}
		
		public MultiLiteralOrResource getContinent() {
			return continent;
		}
		
		public MultiLiteralOrResource getPartOfPlace() {
			return partOfPlace;
		}
		
		public void setNation(MultiLiteralOrResource nation) {
			this.nation = nation;
		}

		public void setContinent(MultiLiteralOrResource continent) {
			this.continent = continent;
		}
		
		public void setPartOfPlace(MultiLiteralOrResource partOfPlace) {
			this.partOfPlace = partOfPlace;
		}

	}
}