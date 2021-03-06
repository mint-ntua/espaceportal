package sources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.RecordResource;
import model.resources.WithResource;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import sources.core.AdditionalQueryModifier;
import sources.core.AutocompleteResponse;
import sources.core.AutocompleteResponse.DataJSON;
import sources.core.AutocompleteResponse.Suggestion;
import sources.core.CommonFilterLogic;
import sources.core.CommonFilters;
import sources.core.CommonQuery;
import sources.core.FacetsModes;
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
import sources.utils.FunctionsUtils;
import utils.ListUtils;

public class EuropeanaSpaceSource extends ISpaceSource {
	
	public static final ALogger log = Logger.of( EuropeanaSpaceSource.class);
	
	private boolean usingCursor = false;
	private String nextCursor;
	private String profile;
	
	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public EuropeanaSpaceSource() {
		super(Sources.Europeana);
		profile = "rich facets";
//		profile = "rich";
		apiKey = "ANnuDzRpW";
		
		addDefaultWriter(CommonFilters.MIME_TYPE.getId(), qfwriter("MIME_TYPE"));
		addDefaultWriter(CommonFilters.IMAGE_SIZE.getId(), qfwriter("IMAGE_SIZE"));
		addDefaultWriter(CommonFilters.IMAGE_COLOUR.getId(), qfwriter("IMAGE_COLOUR"));
		addDefaultWriter(CommonFilters.COLOURPALETE.getId(), qfwriter("COLOURPALETE"));
		
		
		addDefaultQueryModifier(CommonFilters.PROVIDER.getId(), qwriter("PROVIDER"));
		addDefaultQueryModifier(CommonFilters.DATA_PROVIDER.getId(), qwriter("DATA_PROVIDER"));
		addDefaultQueryModifier(CommonFilters.COUNTRY.getId(), qwriter("COUNTRY"));

		addDefaultQueryModifier(CommonFilters.YEAR.getId(), qDatewriter());

		addDefaultQueryModifier(CommonFilters.CREATOR.getId(), qwriter("CREATOR"));

		// addDefaultWriter(CommonFilters.CONTRIBUTOR_ID,
		// qfwriter("proxy_dc_contributor"));

		addDefaultQueryModifier(CommonFilters.RIGHTS.getId(), qrightwriter());

		addDefaultQueryModifier(CommonFilters.TYPE.getId(), qwriter("TYPE"));

		
		formatreader = new EuropeanaRecordFormatter();

	}

	private void addIdentityWriter(String filterId) {
		addDefaultWriter(filterId, qfwriter(filterId));
	}

	private Function<List<String>, QueryModifier> qrightwriter() {
		Function<String, String> function = (String s) -> {
			s = s.replace("(?!.*nc)", "* NOT *nc");
			s = s.replace("(?!.*nd)", "* NOT *nd");
			return "RIGHTS:(" + s.replace(".", "") + ")";
		};
		return new Function<List<String>, QueryModifier>() {
			@Override
			public AdditionalQueryModifier apply(List<String> t) {
				return new AdditionalQueryModifier(" " + Utils.getORList(ListUtils.transform(t, function), false));
			}
		};
	}
	
	private Function<List<String>, QueryModifier> qwriter(String parameter) {
		Function<String, String> function = (String s) -> {
			return parameter+":" + FunctionsUtils.smartquote().apply(s) + "";
		};
		return new Function<List<String>, QueryModifier>() {
			@Override
			public AdditionalQueryModifier apply(List<String> t) {
				return new AdditionalQueryModifier(" " + Utils.getORList(ListUtils.transform(t, function), false));
			}
		};
		
		
	}
	
	private Function<List<String>, QueryModifier> qDatewriter() {
		return new Function<List<String>, QueryModifier>() {
			@Override
			public AdditionalQueryModifier apply(List<String> t) {
				String val = dateRange(t);
				return new AdditionalQueryModifier(" YEAR:" + val);
			}
		};
		
		
	}
	

	private Function<List<String>, Pair<String>> qfwriter(String parameter) {
		if (parameter.equals(CommonFilters.YEAR.name())) {
			return qfwriterYEAR();
		}
		return FunctionsUtils.toORList("qf", 
				(s)-> parameter + ":" + FunctionsUtils.smartquote().apply(s)
				);
	}

	private Function<List<String>, Pair<String>> qfwriterYEAR() {
		return new Function<List<String>, Pair<String>>() {
			@Override
			public Pair<String> apply(List<String> t) {
				String val = dateRange(t);
				return new Pair<String>("qf", "YEAR:" + val);
			}
		};
	}

	class EuropeanaQueryBuilder extends QueryBuilder {

		public EuropeanaQueryBuilder() {
			super();
		}

		public EuropeanaQueryBuilder(String baseUrl) {
			super(baseUrl);
		}

		public String getHttp() {
			String res = getBaseUrl();
			Iterator<Pair<String>> it = parameters.iterator();
			boolean skipqf = false;
			boolean added = false;
			if (query.second != null) {
				res += ("?" + query.getHttp());
				added = true;
			} else {
				skipqf = true;
			}
			for (; it.hasNext();) {

				Pair<String> next = it.next();
				String string = added ? "&" : "?";
				if (next.first.equals("qf") && skipqf) {
					skipqf = false;
					query.second = next.second;
					res += (string + query.getHttp());
					added = true;
				} else {
					added = true;
					res += string + next.getHttp();
				}
			}
			if (Utils.hasInfo(tail)){
				res+=tail;
			}
			return res;
		}

	}

	public String getHttpQuery(CommonQuery q) {
		QueryBuilder builder = new EuropeanaQueryBuilder("http://www.europeana.eu/api/v2/search.json");
		builder.addSearchParam("wskey", apiKey);

		builder.addQuery("query", q.searchTerm);

		if (usingCursor){
			if (q.page.equals("1"))
				builder.addSearchParam("cursor", "*");
			else
				builder.addSearchParam("cursor", nextCursor);
		} else
		builder.addSearchParam("start", "" + (((Integer.parseInt(q.page) - 1) * Integer.parseInt(q.pageSize)) + 1));

		builder.addSearchParam("rows", "" + q.pageSize);
		builder.addSearchParam("profile", profile);
		String facets = "DEFAULT";
		if (q.facetsMode != null) {
			switch (q.facetsMode) {
			case FacetsModes.SOME:
				facets = "proxy_dc_creator," + facets;
				break;
			case FacetsModes.ALL:
				facets = "proxy_dc_creator,proxy_dc_contributor," + 
			             "MIME_TYPE,IMAGE_SIZE,IMAGE_COLOUR,IMAGE_GREYSCALE,"+
						facets;
				break;
			default:
				break;
			}
		}
		if (q.hasMedia)
			builder.addSearchParam("media", "true");
//		builder.addSearchParam("facet", facets);
		builder.setTail(q.tail);
		return addfilters(q, builder).getHttp();
	}

	public List<CommonFilterLogic> createFilters(JsonNode response) {
		List<CommonFilterLogic> filters = new ArrayList<CommonFilterLogic>();
		CommonFilterLogic type = new CommonFilterLogic(CommonFilters.TYPE).addTo(filters);
		CommonFilterLogic provider = new CommonFilterLogic(CommonFilters.PROVIDER).addTo(filters);
		CommonFilterLogic dataprovider = new CommonFilterLogic(CommonFilters.DATA_PROVIDER).addTo(filters);
		CommonFilterLogic creator = new CommonFilterLogic(CommonFilters.CREATOR).addTo(filters);
		CommonFilterLogic rights = new CommonFilterLogic(CommonFilters.RIGHTS).addTo(filters);
		CommonFilterLogic country = new CommonFilterLogic(CommonFilters.COUNTRY).addTo(filters);
		CommonFilterLogic year = new CommonFilterLogic(CommonFilters.YEAR).addTo(filters);
		CommonFilterLogic mtype = new CommonFilterLogic(CommonFilters.MIME_TYPE).addTo(filters);
		CommonFilterLogic isize = new CommonFilterLogic(CommonFilters.IMAGE_SIZE).addTo(filters);
		CommonFilterLogic icolor = new CommonFilterLogic(CommonFilters.IMAGE_COLOUR).addTo(filters);
		CommonFilterLogic cpalete = new CommonFilterLogic(CommonFilters.COLOURPALETE).addTo(filters);
				
		for (JsonNode facet : response.path("facets")) {
			String filterType = facet.path("name").asText();
			for (JsonNode jsonNode : facet.path("fields")) {
				String label = jsonNode.path("label").asText();
				int count = jsonNode.path("count").asInt();
				switch (filterType) {
				case "TYPE":
					countValue(type, label, count);
					break;
				case "DATA_PROVIDER":
					countValue(dataprovider, label, false, count);
					break;
				case "PROVIDER":
					countValue(provider, label, false, count);
					break;
				case "RIGHTS":
					countValue(rights, label, count);
					break;
				case "proxy_dc_creator":
					countValue(creator, label, false, count);
					break;
				case "COUNTRY":
					countValue(country, label, false, count);
					break;
				case "YEAR":
					countValue(year, label, false, count);
					break;
				case "MIME_TYPE":
					countValue(mtype, label, false, count);
					break;
				case "IMAGE_SIZE":
					countValue(isize, label, false, count);
					break;
				case "IMAGE_COLOUR":
					countValue(icolor, label, false, count);
					break;
				case "COLOURPALETE":
					countValue(cpalete, label, false, count);
					break;

				default:
					break;
				}

			}
		}
		return filters;
	}

	public ArrayList<WithResource<?, ?>> getItems(JsonNode response) {
		ArrayList<WithResource<?, ?>> items = new ArrayList<>();
		try {
			if (response.path("success").asBoolean()) {
				for (JsonNode item : response.path("items")) {
					items.add(formatreader.readObjectFrom(item));
				}
			}
		} catch (Exception e) {
			log.error("",e);
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
				res.totalCount = Utils.readIntAttr(response, "totalResults", true);
				res.count = Utils.readIntAttr(response, "itemsCount", true);
				res.items.setCulturalCHO(getItems(response));
				// res.facets = response.path("facets");
				res.filtersLogic = createFilters(response);
				if (usingCursor) {
					nextCursor = Utils.readAttr(response, "nextCursor", true);
					if (!Utils.hasInfo(nextCursor))
						Logger.error("cursor error!!");
				}
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error( "", e );
			}
		}
		return res;
	}

	public String autocompleteQuery(String term, int limit) {
		return "http://www.europeana.eu/api/v2/suggestions.json?rows=" + limit + "&phrases=false&query=" + term;
	}

	public AutocompleteResponse autocompleteResponse(String response) {
		try {
			JSONObject jsonResp = new JSONObject(response);
			if ((jsonResp == null) || !jsonResp.getBoolean("success") || (jsonResp.getJSONArray("items") == null))
				return new AutocompleteResponse();
			else {
				JSONArray items = jsonResp.getJSONArray("items");
				AutocompleteResponse ar = new AutocompleteResponse();
				ar.suggestions = new ArrayList<Suggestion>();
				for (int i = 0; i < items.length(); i++) {
					JSONObject item = items.getJSONObject(i);
					Suggestion s = new Suggestion();
					s.value = item.getString("term");
					DataJSON data = new DataJSON();
					data.category = "Europeana";
					data.frequencey = item.getInt("frequency");
					data.field = item.getString("field");
					s.data = data;
					ar.suggestions.add(s);
				}
				return ar;
			}
		} catch (JSONException e) {
			log.error("", e);
			return new AutocompleteResponse();
		}
	}

	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		String key = "ANnuDzRpW";
		ArrayList<RecordJSONMetadata> jsonMetadata = new ArrayList<RecordJSONMetadata>();
		JsonNode response;
		try {
			response = getHttpConnector()
					.getURLContent("http://www.europeana.eu/api/v2/record/" + recordId + ".json?wskey=" + key);
			// todo read the other format;
			JsonNode record = response.get("object");
			if (response != null) {
//				jsonMetadata.add(new RecordJSONMetadata(Format.JSON_EDM, record.toString()));
				EuropeanaItemRecordFormatter f = new EuropeanaItemRecordFormatter();
				String json = Json.toJson(f.overwriteObjectFrom(fullRecord,record)).toString();
				jsonMetadata.add(new RecordJSONMetadata(Format.JSON_WITH, json));
//				RecordQuality q = new RecordQuality();
//				q.compute(new JsonContextRecord(json));
			}
			response = getHttpConnector()
					.getURLContent("http://www.europeana.eu/api/v2/record/" + recordId + ".jsonld?wskey=" + key);
			if (response != null) {
				record = response;
				jsonMetadata.add(new RecordJSONMetadata(Format.JSONLD_EDM, record.toString()));
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

	private String dateRange(List<String> t) {
		String val = "\"" + t.get(0) + "\"";
		if (t.size() > 1) {
			val = "[" + val + " TO \"" + t.get(1) + "\"]";
		}
		return val;
	}
	
}
