package sources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.RecordResource;
import model.resources.WithResource;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import sources.core.CommonFilterLogic;
import sources.core.CommonFilters;
import sources.core.CommonQuery;
import sources.core.HttpConnector;
import sources.core.ISpaceSource;
import sources.core.QueryBuilder;
import sources.core.RecordJSONMetadata;
import sources.core.RecordJSONMetadata.Format;
import sources.core.Utils.Pair;
import sources.core.SourceResponse;
import sources.core.Utils;
import sources.formatreaders.DDBItemRecordFormatter;
import sources.formatreaders.DDBRecordFormatter;
import sources.utils.FunctionsUtils;

public class DDBSpaceSource extends ISpaceSource {
	public static final ALogger log = Logger.of( DDBSpaceSource.class);
	
	public DDBSpaceSource() {
		super(Sources.DDB);
		apiKey = "4AqtJR7LqxFz0Bl1JHgFOjpFHV2UhEFw30yqyIQOG3MSbdRCaHo1438238341952";
		formatreader = new DDBRecordFormatter();
		
		addDefaultWriter(CommonFilters.TYPE.getId(), fwriter("type_fct"));
		addDefaultWriter(CommonFilters.PROVIDER.getId(), fwriter("provider_fct"));
		addDefaultWriter(CommonFilters.RIGHTS.getId(), fwriter("license_group"));
		addDefaultWriter(CommonFilters.COUNTRY.getId(), fwriter("place_fct"));
		
	}
	
	public List<CommonQuery> splitFilters(CommonQuery q) {
		return q.splitFilters(this);
	}

	
	private Function<List<String>, Pair<String>> fwriter(String parameter) {
		return FunctionsUtils.toORList(parameter, false);
	}

	@Override
	public QueryBuilder getBuilder(CommonQuery q) {
		QueryBuilder builder = super.getBuilder(q);
		builder.setBaseUrl("http://api.deutsche-digitale-bibliothek.de/search?");
		builder.addSearchParam("oauth_consumer_key", apiKey);
		builder.addQuery("query", q.searchTerm);
		builder.addSearchParam("rows", q.pageSize);
		builder.addSearchParam("offset", "" + ((Integer.parseInt(q.page) - 1) * Integer.parseInt(q.pageSize)));
		builder.addSearchParam("facet", "place_fct");
		builder.addSearchParam("facet", "type_fct");
		builder.addSearchParam("facet", "provider_fct");
		builder.addSearchParam("facet", "time_fct");
		builder.addSearchParam("facet", "license_group");
//		builder.addSearchParam("isThumbnailFiltered", "true");
		FashionSearch fq = new FashionSearch();
		fq.setTerm(q.searchTerm);
		fq.setCount(Integer.parseInt(q.pageSize));
		fq.setOffset(((Integer.parseInt(q.page) - 1) * Integer.parseInt(q.pageSize)));
		builder.setData(fq);
		addfilters(q, builder);
		return builder;
	}

	@Override
	public SourceResponse getResults(CommonQuery q) {
		SourceResponse res = new SourceResponse();
		res.source = getSourceName().toString();
		// String httpQuery = getHttpQuery(q);
		QueryBuilder builder = getBuilder(q);
		res.query = builder.getHttp();
		JsonNode response;
		if (checkFilters(q)) {
			try {
				response = getHttpConnector().getURLContent(res.query);
				JsonNode docs = response.path("results").get(0).path("docs");
				res.totalCount = Utils.readIntAttr(response, "numberOfResults", true);
				res.count = docs.size();
				// res.startIndex = Utils.readIntAttr(response, "offset", true);
				for (JsonNode item : docs) {
					res.addItem(formatreader.readObjectFrom(item));
				}
				res.filtersLogic = createFilters(response);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error( "",e );
			}
		}

		return res;
	}
	
	
	public List<CommonFilterLogic> createFilters(JsonNode response) {
		List<CommonFilterLogic> filters = new ArrayList<CommonFilterLogic>();
		CommonFilterLogic type = new CommonFilterLogic(CommonFilters.TYPE).addTo(filters);
		CommonFilterLogic provider = new CommonFilterLogic(CommonFilters.PROVIDER).addTo(filters);
		CommonFilterLogic rights = new CommonFilterLogic(CommonFilters.RIGHTS).addTo(filters);
		CommonFilterLogic country = new CommonFilterLogic(CommonFilters.COUNTRY).addTo(filters);
		CommonFilterLogic year = new CommonFilterLogic(CommonFilters.YEAR).addTo(filters);
				
		for (JsonNode facet : response.path("facets")) {
			String filterType = facet.path("field").asText();
			for (JsonNode jsonNode : facet.path("facetValues")) {
				String label = jsonNode.path("value").asText();
				int count = jsonNode.path("count").asInt();
				switch (filterType) {
				case "type_fct":
					countValue(type, label, count);
					break;
				case "provider_fct":
					countValue(provider, label, false, count);
					break;
				case "license_group":
					countValue(rights, label, count);
					break;
				case "place_fct":
					countValue(country, label, false, count);
					break;
				case "time_fct":
					countValue(year, label, false, count);
					break;
				default:
					break;
				}

			}
		}
		return filters;
	}
	
	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		ArrayList<RecordJSONMetadata> jsonMetadata = new ArrayList<RecordJSONMetadata>();
		JsonNode response;
		try {
			
			
			QueryBuilder builder = new QueryBuilder();
			builder.setBaseUrl("http://api.deutsche-digitale-bibliothek.de/items/"+recordId+"/edm");
			builder.addSearchParam("oauth_consumer_key", apiKey);
			response = getHttpConnector()
					.getURLContent(builder.getHttp());
			// todo read the other format;
			JsonNode record = response;
			if (response != null) {
				jsonMetadata.add(new RecordJSONMetadata(Format.JSON_EDM, record.toString()));
				DDBItemRecordFormatter f = new DDBItemRecordFormatter();
				String json = Json.toJson(f.overwriteObjectFrom(fullRecord,record)).toString();
				jsonMetadata.add(new RecordJSONMetadata(Format.JSON_WITH, json));
			}

			return jsonMetadata;
		} catch (Exception e) {
			log.error("",e);
			return jsonMetadata;
		}
	}

}
