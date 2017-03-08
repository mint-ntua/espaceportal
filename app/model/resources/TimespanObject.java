package model.resources;

import java.util.ArrayList;

import org.mongodb.morphia.annotations.Entity;

import model.basicDataTypes.WithPeriod;
import model.resources.RecordResource.RecordDescriptiveData;


@Entity("RecordResource")
public class TimespanObject extends RecordResource<TimespanObject.TimespanData>{
	
	public static class TimespanData extends RecordDescriptiveData {

		ArrayList<WithPeriod> timespan;
	}
	

}
