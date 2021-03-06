package model.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import controllers.WithController.Profile;
import model.DescriptiveData;
import model.EmbeddedMediaObject;
import model.EmbeddedMediaObject.MediaVersion;
import model.basicDataTypes.Language;


public class RecordResource<T extends RecordResource.RecordDescriptiveData>
	extends WithResource<T, RecordResource.RecordAdmin> {

	public RecordResource() {
		super();
		this.administrative = new RecordAdmin();
		this.descriptiveData = (T) new RecordDescriptiveData();
	}

	public RecordResource(ObjectId id) {
		this.administrative = new RecordAdmin();
		this.setDbId(id);
	}

	public static class RecordDescriptiveData extends DescriptiveData {

	}

	public static class RecordAdmin extends WithResource.WithAdmin {

		// if this resource / record is derived (modified) from a different Record.
		private ObjectId parentResourceId;

		public ObjectId getParentResourceId() {
			return parentResourceId;
		}

		public void setParentResourceId(ObjectId parentResourceId) {
			this.parentResourceId = parentResourceId;
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
		Map<String, Object> idx_map =  this.transformWR();

		List<ObjectId> colIn = new ArrayList<ObjectId>();
		this.getCollectedIn().forEach( (ci) -> {colIn.add(ci);} );

		idx_map.put("collectedId", colIn);

		return idx_map;
	}
	
	public RecordResource<T> getRecordProfile(String profileString) {
		Profile profile = Profile.valueOf(profileString);
		if (profile == null)
			profile = Profile.BASIC;
		if (profile.equals(Profile.FULL))
			return this;
		else {
			WithResourceType recordType = getResourceType();
			try {
				Class<?> clazz = Class.forName("model.resources." + recordType.toString());
				RecordResource<T> output;
				output = (RecordResource<T>) clazz.newInstance();
				if (profile.equals(Profile.BASIC)) { //for thumbnails view
				//if (input.getResourceType().equals(WithResourceType.CulturalObject)) {
					addBasicRecordFields(output);
					HashMap<MediaVersion, EmbeddedMediaObject> media = new HashMap<MediaVersion, EmbeddedMediaObject>(1);
					if (getMedia() != null && getMedia().size() > 0) {
						EmbeddedMediaObject thumbnail = ((HashMap<MediaVersion, EmbeddedMediaObject>) getMedia().get(0)).get(MediaVersion.Thumbnail);
						if (thumbnail != null) {
							media.put(MediaVersion.Thumbnail, thumbnail);
						}
					}
					output.setMedia(new ArrayList<>(Arrays.asList(media)));
					return output;
				//}
				}
				else if (profile.equals(Profile.MEDIUM)) {
					addMediumRecordFields(output);
					EmbeddedMediaObject thumbnail = ((HashMap<MediaVersion, EmbeddedMediaObject>) getMedia().get(0)).get(MediaVersion.Thumbnail);
					EmbeddedMediaObject original = ((HashMap<MediaVersion, EmbeddedMediaObject>) getMedia().get(0)).get(MediaVersion.Original);
					HashMap<MediaVersion, EmbeddedMediaObject> media = new HashMap<MediaVersion, EmbeddedMediaObject>(2);
					media.put(MediaVersion.Thumbnail, thumbnail);
					media.put(MediaVersion.Original, original);
					output.setMedia(new ArrayList<>(Arrays.asList(media)));
					return output;
				}
				else return this;
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private void addBasicRecordFields(RecordResource output) {
		output.setResourceType(getResourceType());
		output.setAdministrative((RecordAdmin) getAdministrative());
		output.getDescriptiveData().setLabel(getDescriptiveData().getLabel());
		output.getDescriptiveData().setDescription(getDescriptiveData().getDescription());
		//add more fields from descriptive?
		output.setDbId(getDbId());
		output.setProvenance(getProvenance());
	}
	
	//adds all descriptiveData
	private void addMediumRecordFields(RecordResource output) {
		output.setResourceType(getResourceType());
		output.setAdministrative((RecordAdmin) getAdministrative());
		output.setDbId(getDbId());
		output.setProvenance(getProvenance());
		output.setDescriptiveData(getDescriptiveData());
		output.setAnnotationIds(getAnnotationIds());
		output.fillAnnotations();
	}
}