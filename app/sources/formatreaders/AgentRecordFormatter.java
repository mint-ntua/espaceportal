package sources.formatreaders;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import model.basicDataTypes.Language;
import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.ProvenanceInfo;
import model.resources.AgentObject;
import model.resources.RecordResource;
import model.resources.AgentObject.AgentData;
import model.resources.CulturalObject;
import model.resources.CulturalObject.CulturalObjectData;
import play.Logger;
import play.Logger.ALogger;
import sources.FilterValuesMap;
import sources.core.JsonContextRecordFormatReader;
import sources.core.Utils;
import sources.utils.JsonContextRecord;
import sources.utils.StringUtils;

public abstract class AgentRecordFormatter extends JsonContextRecordFormatReader<AgentObject> {
	public static final ALogger log = Logger.of( AgentRecordFormatter.class );
	
	private FilterValuesMap valuesMap;

	public AgentRecordFormatter(FilterValuesMap valuesMap) {
		super();
		this.valuesMap = valuesMap;
	}
	
	protected Language[] getLanguagesFromText(String... text) {
		String full = "";
		for (String string : text) {
			if (Utils.hasInfo(string)){
				full+=string;
			}
		}
		List<Language> res =StringUtils.getLanguages(full);
        Logger.info("["+full+"] Item Detected Languages " + res);
		return res.toArray(new Language[]{});
	}
	
	public AgentObject readObjectFrom(JsonContextRecord text) {
		object = new AgentObject();
		object.getAdministrative().getAccess().setIsPublic(true);

		AgentData model = new AgentData();
		object.setDescriptiveData(model);
		model.setMetadataRights(new LiteralOrResource("http://creativecommons.org/publicdomain/zero/1.0/"));
		model.setRdfType("http://www.europeana.eu/schemas/edm/ProvidedCHO");

		try {
			fillObjectFrom(text);
		} catch (Exception e) {
			log.error("Error Importing object from source",e );
		}
		List<ProvenanceInfo> provenance = object.getProvenance();
		int index = provenance.size() - 1;
		String resourceId = provenance.get(index).getResourceId();
		object.getAdministrative().setExternalId(resourceId);

		return object;
	}
	
	private AgentObject internalOverwriteObjectFrom(JsonContextRecord text) {
		
		try {
			fillObjectFrom(text);
		} catch (Exception e) {
			log.error("Error Importing object from source",e);
		}

		return object;
	}

	public AgentObject readObjectFrom(JsonNode text) {
		return readObjectFrom(new JsonContextRecord(text));
	}

	public AgentObject overwriteObjectFrom(RecordResource object, JsonNode text) {
		if (object==null){
			return readObjectFrom(text);
		} else
		this.object = (AgentObject)object;
		return internalOverwriteObjectFrom(new JsonContextRecord(text));
	}
	
	public FilterValuesMap getValuesMap() {
		return valuesMap;
	}

	public void setValuesMap(FilterValuesMap valuesMap) {
		this.valuesMap = valuesMap;
	}

}