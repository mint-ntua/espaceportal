package sources;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.CulturalObject;
import model.resources.RecordResource;
import model.resources.WithResource;
import play.Logger;
import play.libs.Json;
import sources.core.AdditionalQueryModifier;
import sources.core.AutocompleteResponse;
import sources.core.AutocompleteResponse.DataJSON;
import sources.core.AutocompleteResponse.Suggestion;
import sources.core.CommonFilterLogic;
import sources.core.CommonFilters;
import sources.core.CommonQuery;
import sources.core.FacetsModes;
import sources.core.HttpConnector;
import sources.core.ISpaceSource;
import sources.core.QueryBuilder;
import sources.core.QueryModifier;
import sources.core.RecordJSONMetadata;
import sources.core.RecordJSONMetadata.Format;
import sources.core.SourceResponse;
import sources.core.Utils;
import sources.core.Utils.Pair;
import sources.formatreaders.EuropeanaItemRecordFormatter;
import sources.formatreaders.EuropeanaRecordFormatter;
import sources.formatreaders.HistorypinItemRecordFormatter;
import sources.formatreaders.HistorypinRecordFormatter;
import sources.utils.FunctionsUtils;
import utils.ListUtils;

public class HistorypinSpaceSource extends ISpaceSource {
	
	private boolean usingCursor = false;
	private String nextCursor;
	
	public HistorypinSpaceSource() {
		super(Sources.Historypin);
		addDefaultWriter(CommonFilters.TYPE.getId(), qfwriter("pin"));
		formatreader = new HistorypinRecordFormatter();

	}

	private Function<List<String>, Pair<String>> qfwriter(String parameter) {
		return FunctionsUtils.toORList("qf", 
				(s)-> parameter + ":" + FunctionsUtils.smartquote().apply(s)
				);
	}


	public String getHttpQuery(CommonQuery q) {
		QueryBuilder builder = new QueryBuilder("http://www.historypin.org/en/api/explore/pin/get_gallery.json");
		builder.addSearchParam("page", "" +q.page);
		builder.addSearchParam("limit", "" + q.pageSize);
		builder.add(new Pair<String>("search=pin:pin,keyword", q.searchTerm){
			public String getHttp(boolean encode) {
				String string = first + ":" + second.toString();
				if (encode){
					try {
						String encoded = URLEncoder.encode(second.toString(), "UTF-8");
						string = first + ":" + encoded;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				return string;
			}

		});
		
		return addfilters(q, builder).getHttp();
	}

	

	public ArrayList<WithResource<?, ?>> getItems(JsonNode response) {
		ArrayList<WithResource<?, ?>> items = new ArrayList<>();
		try {
				for (JsonNode item : response.path("results")) {
					items.add(formatreader.readObjectFrom(item));
				}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return items;
	}

	@Override
	public SourceResponse getResults(CommonQuery q) {
		SourceResponse res = new SourceResponse();
		res.source = getSourceName().toString();
		String httpQuery = getHttpQuery(q);
		res.query = httpQuery;
		JsonNode response;
		if (checkFilters(q)) {
			try {
				response = getHttpConnector().getURLContent(httpQuery);
				res.totalCount = Utils.readIntAttr(response, "count", true);
				res.count = Utils.readIntAttr(response, "limit", true);
				res.items.setCulturalCHO(getItems(response));
				if (usingCursor) {
					nextCursor = Utils.readAttr(response, "nextCursor", true);
					if (!Utils.hasInfo(nextCursor))
						Logger.error("cursor error!!");
				}
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
	}

	


	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		String key = "ANnuDzRpW";
		ArrayList<RecordJSONMetadata> jsonMetadata = new ArrayList<RecordJSONMetadata>();
		JsonNode response;
		try {
			response = getHttpConnector()
					.getURLContent("http://www.historypin.org/en/api/pin/get.json?id=" + recordId);
			// todo read the other format;
			JsonNode record = response;
			if (response != null) {
				jsonMetadata.add(new RecordJSONMetadata(Format.JSON_Historypin, record.toString()));
				HistorypinItemRecordFormatter f = new HistorypinItemRecordFormatter();
				String json = Json.toJson(f.overwriteObjectFrom(fullRecord,record)).toString();
				jsonMetadata.add(new RecordJSONMetadata(Format.JSON_WITH, json));
			}

			return jsonMetadata;
		} catch (Exception e) {
			return jsonMetadata;
		}
	}

	public boolean isUsingCursor() {
		return usingCursor;
	}

	public void setUsingCursor(boolean useCursor) {
		this.usingCursor = useCursor;
	}
	
}
