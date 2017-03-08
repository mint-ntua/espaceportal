package model.resources;

import java.util.Date;

import org.mongodb.morphia.annotations.Entity;
import model.resources.RecordResource.RecordDescriptiveData;


@Entity("RecordResource")
public class EUscreenObject extends RecordResource<EUscreenObject.EUscreenData> {
	
	public static class EUscreenData extends RecordDescriptiveData { 
		// title is filled in with original language title and english title
		// description dito
		
		String broadcastChannel;
		Date brodcastDate;
		
		// in year we keep the production year
		
	}

}
