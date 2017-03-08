package db.converters;

import model.basicDataTypes.ProvenanceInfo;

import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import com.mongodb.DBObject;

public class ProvenanceInfoConverter extends TypeConverter{

		public ProvenanceInfoConverter() {
			super(ProvenanceInfo.class);
		}

		@Override
		public Object decode(Class<?> arg0, Object fromDbObject, MappedField arg2) {
			DBObject dbObj = (DBObject) fromDbObject;
			String provider = (String) dbObj.get("provider");
			String uri = "";
			String recordId = "";
		    if (dbObj.containsField("uri"))
		    	uri = (String) dbObj.get("uri");
		    if (dbObj.containsField("recordId"))
		    	recordId = (String) dbObj.get("recordId");
			ProvenanceInfo p = new ProvenanceInfo(provider);
			if (!uri.isEmpty())
				p.setUri(uri);
			if (!recordId.isEmpty())
				p.setResourceId(recordId);
			return p;
		}
}
