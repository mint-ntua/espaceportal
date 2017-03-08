package sources;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.DBPediaController;
import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.AgentObject;
import model.resources.PlaceObject;
import model.resources.RecordResource;
import sources.core.CommonQuery;
import sources.core.ISpaceSource;
import sources.core.JsonContextRecordFormatReader;
import sources.core.RecordJSONMetadata;
import sources.core.RecordJSONMetadata.Format;
import sources.core.SourceResponse;
import sources.core.Utils;
import sources.formatreaders.DBPediaAgentRecordFormatter;
import sources.formatreaders.DBPediaPlaceRecordFormatter;

public class DBPediaSpaceSource extends ISpaceSource {

	protected JsonContextRecordFormatReader<AgentObject> agentformatreader;
	protected JsonContextRecordFormatReader<PlaceObject> placeformatreader;
	
	public DBPediaSpaceSource() {
		super(Sources.DBPedia);
		formatreader = new DBPediaAgentRecordFormatter();
		
		agentformatreader = formatreader; 
		placeformatreader = new DBPediaPlaceRecordFormatter();
	}

	@Override
	public SourceResponse getResults(CommonQuery q) {
		SourceResponse res = new SourceResponse();
		res.source = getSourceName().toString();

		JsonNode response;
		if (checkFilters(q)) {
			try {
				
				response = DBPediaController.doLookup("Person,Place", q.searchTerm, ((Integer.parseInt(q.page) - 1) * Integer.parseInt(q.pageSize)), (Integer.parseInt(q.pageSize) - 1));
				res.totalCount = Utils.readIntAttr(response, "totalcount", true);
				
				int count = 0;
				for (JsonNode item : response.path("results")) {
					for (JsonNode type : item.path("type")) {
						count++;
						if (type.asText().equals("http://dbpedia.org/ontology/Place")) {
							res.addItem(placeformatreader.readObjectFrom(item));
							break;
						} else if (type.asText().equals("http://dbpedia.org/ontology/Person")) {
							res.addItem(agentformatreader.readObjectFrom(item));
							break;
						}
					}
					
				}
				//TODO: what is the count?
				res.count = count;

				res.filtersLogic = new ArrayList<>();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
	}

	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		//WHAT IS: recordID? IT SHOULD BY DBPEDIA URI name AFTER http://dbpedia.org/resource/...
		ArrayList<RecordJSONMetadata> jsonMetadata = new ArrayList<RecordJSONMetadata>();
		JsonNode response;
		try {
			response = getHttpConnector()
					.getURLContent("http://zenon.image.ece.ntua.gr:8890/data/" + recordId + ".rdf");
			jsonMetadata.add(new RecordJSONMetadata(Format.JSON_RDF, response.toString()));
			return jsonMetadata;
		} catch (Exception e) {
			return jsonMetadata;
		}
	}
}
