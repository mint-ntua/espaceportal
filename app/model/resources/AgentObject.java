package model.resources;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;

import model.basicDataTypes.Literal;
import model.basicDataTypes.MultiLiteralOrResource;
import model.basicDataTypes.WithDate;
import model.resources.RecordResource.RecordAdmin;
import model.resources.RecordResource.RecordDescriptiveData;
import model.resources.WithResource.WithResourceType;

@Entity("RecordResource")
public class AgentObject extends RecordResource<AgentObject.AgentData> {

	public AgentObject() {
		super();
		this.resourceType = WithResourceType.valueOf(this.getClass().getSimpleName());
	}

	public static class AgentData extends RecordDescriptiveData {

		List<WithDate> birthdate;
		MultiLiteralOrResource birthplace;
		List<WithDate> deathdate;
		public static enum Gender {
			MALE, FEMALE, UNKNOWN
		}
		Gender genderEnum;
		Literal gender;
		
		public MultiLiteralOrResource getBirthPlace() {
			return birthplace;
		}
		
		public void setBirthDate(List<WithDate> birthdate) {
			this.birthdate = birthdate;
		}
		
		public void setDeathDate(List<WithDate> deathDate) {
			this.deathdate = deathDate;
		}
		
		public void setBirthPlace(MultiLiteralOrResource birthplace) {
			this.birthplace = birthplace;
		}
		
		public void setGender(Gender g) {
			this.genderEnum = g;
		}

	}
}
