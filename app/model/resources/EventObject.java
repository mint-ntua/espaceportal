package model.resources;

import java.util.ArrayList;

import org.mongodb.morphia.annotations.Entity;

import model.basicDataTypes.MultiLiteralOrResource;
import model.basicDataTypes.WithPeriod;
import model.DescriptiveData;
import model.resources.RecordResource.RecordDescriptiveData;

@Entity("RecordResource")
public class EventObject extends RecordResource<EventObject.EventData> {
	
	public static class EventData extends RecordDescriptiveData {
		ArrayList<WithPeriod> period;
		MultiLiteralOrResource personsInvolved;
		MultiLiteralOrResource placesInvolved;
		MultiLiteralOrResource objectsInvolved;
	}
}
