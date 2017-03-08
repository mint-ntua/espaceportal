package sources.core;

import com.fasterxml.jackson.databind.JsonNode;

import model.resources.CulturalObject;
import model.resources.RecordResource;
import model.resources.WithResource;
import sources.utils.JsonContextRecord;

public abstract class JsonContextRecordFormatReader<T extends WithResource> {

	protected T object;

	public T fillObjectFrom(String text) {
		JsonContextRecord rec = new JsonContextRecord(text);
		return fillObjectFrom(rec);
	}

	protected T fillObjectFrom(JsonNode text) {
		JsonContextRecord rec = new JsonContextRecord(text);
		return fillObjectFrom(rec);
	}

	protected abstract T fillObjectFrom(JsonContextRecord text);
	public abstract T overwriteObjectFrom(RecordResource object, JsonNode text);

	public T readObjectFrom(JsonNode text) {
		return readObjectFrom(new JsonContextRecord(text));
	}
	
	public T readObjectFrom(JsonContextRecord text) {
		return fillObjectFrom(text);
	}
	

}
