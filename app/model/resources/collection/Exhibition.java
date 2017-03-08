package model.resources.collection;

import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.annotations.Entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import model.EmbeddedMediaObject;
import model.EmbeddedMediaObject.MediaVersion;
import model.resources.collection.CollectionObject.CollectionDescriptiveData;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@Entity("CollectionObject")
public class Exhibition extends CollectionObject<Exhibition.ExhibitionDescriptiveData> {

	public Exhibition() {
		super();
		this.descriptiveData = new ExhibitionDescriptiveData();
		this.resourceType = WithResourceType.Exhibition;
	}

	public static class ExhibitionDescriptiveData extends CollectionDescriptiveData {

		private String intro;
		private HashMap<MediaVersion, EmbeddedMediaObject> backgroundImg;
		private String credits;

		public String getIntro() {
			return intro;
		}

		public void setIntro(String intro) {
			this.intro = intro;
		}

		public HashMap<MediaVersion, EmbeddedMediaObject> getBackgroundImg() {
			return backgroundImg;
		}

		public void setBackgroundImg(HashMap<MediaVersion, EmbeddedMediaObject> backgroundImg) {
			this.backgroundImg = backgroundImg;
		}

		public String getCredits() {
			return credits;
		}

		public void setCredits(String credits) {
			this.credits = credits;
		}
	}


	/*
	 * Elastic transformations
	 */

	/*
	 * Currently we are indexing only Resources that represent
	 * collected records
	 */
	public Map<String, Object> transform() {
		Map<String, Object> idx_map =  super.transform();

		idx_map.put("intro", this.getDescriptiveData().getIntro());
		idx_map.put("credits", this.getDescriptiveData().getCredits());
		return idx_map;
	}
}
