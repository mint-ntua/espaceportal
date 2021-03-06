package sources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import sources.core.CommonFilterLogic;
import sources.core.CommonFilters;
import sources.core.CommonQuery;
import sources.core.HttpConnector;
import sources.core.ISpaceSource;
import sources.core.QueryBuilder;
import sources.core.QueryModifier;
import sources.core.RecordJSONMetadata;
import sources.core.SourceResponse;
import sources.core.RecordJSONMetadata.Format;
import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.RecordResource;

import com.fasterxml.jackson.databind.JsonNode;

public class EuropeanaFashionSpaceSource extends ISpaceSource {

	public EuropeanaFashionSpaceSource() {
		super(Sources.EFashion);
		// addDefaultWriter(CommonFilters.TYPE_ID, qfwriter("TYPE"));
		// addDefaultQueryModifier(CommonFilters.TYPE_ID, getFunction("219",
		// "objectType"));
		addDefaultQueryModifier(CommonFilters.DATA_PROVIDER.name(), getFunction("302", "dataProviders"));

		// addMapping(CommonFilters.TYPE_ID, TypeValues.IMAGE, getURI("10303"));
		// addMapping(CommonFilters.TYPE_ID, TypeValues.IMAGE, getURI("10460"));
		// addMapping(CommonFilters.TYPE_ID, TypeValues.IMAGE, getURI("10518"));
		// addMapping(CommonFilters.TYPE_ID, TypeValues.IMAGE, getURI("10462"));
		// addMapping(CommonFilters.TYPE_ID, TypeValues.IMAGE, "artwork");
		// addMapping(CommonFilters.TYPE_ID, TypeValues.VIDEO, "VIDEO");
		// addMapping(CommonFilters.TYPE_ID, TypeValues.SOUND, "SOUND");
		// addMapping(CommonFilters.TYPE_ID, TypeValues.TEXT, "TEXT");

	}

	// public String getHttpQuery(CommonQuery q) {
	// addfilters(q, builder)
	// return "http://www.europeanafashion.eu/api/search/";
	// }

	public List<CommonQuery> splitFilters(CommonQuery q) {
		return q.splitFilters(this);
	}

	private String getURI(String ID) {
		return "http://thesaurus.europeanafashion.eu/thesaurus/" + ID;
	}

	public Function<List<String>, QueryModifier> getFunction(String ID, String type) {
		Function<List<String>, QueryModifier> fprov = (List<String> args) -> {
			return new QueryModifier() {
				@Override
				public QueryBuilder modify(QueryBuilder builder) {
					FashionSearch fs = (FashionSearch) builder.getData();
					for (String string : args) {
						Filter ft = new Filter();
						ft.setType(type);
						ft.setId(ID);
						ft.setValue(string);
						ft.setTerm(string);
						fs.getFilters().add(ft);
					}
					return builder;
				}
			};
		};
		return fprov;
	}

	@Override
	public QueryBuilder getBuilder(CommonQuery q) {
		QueryBuilder builder = super.getBuilder(q);
		builder.setBaseUrl("http://www.europeanafashion.eu/api/search/");
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
//		res.source = getSourceName();
//		// String httpQuery = getHttpQuery(q);
//		QueryBuilder builder = getBuilder(q);
//		res.query = builder.getHttp();
//		JsonNode response;
//		if (checkFilters(q)) {
//			try {
//				response = HttpConnector.getPOSTURLContent(res.query, Json.toJson(builder.getData()).toString());
//				// System.out.println(response.toString());
//				JsonNode docs = response.path("results");
//				res.totalCount = Utils.readIntAttr(response, "total", true);
//				res.count = docs.size();
//				res.startIndex = Utils.readIntAttr(response, "offset", true);
//				ArrayList<ItemsResponse> a = new ArrayList<ItemsResponse>();
//
//				for (JsonNode item : docs) {
//					ItemsResponse it = new ItemsResponse();
//					it.id = Utils.readAttr(item, "id", true);
//					it.thumb = Utils.readArrayAttr(item, "thumbnail", false);
//					it.fullresolution = null;
//					it.title = Utils.readAttr(item, "title", false);
//					it.description = Utils.readAttr(item, "description", false);
//					// it.creator =
//					// Utils.readLangAttr(item.path("sourceResource"),
//					// "creator", false);
//					// it.year = null;
//					// it.dataProvider =
//					// Utils.readLangAttr(item.path("provider"),
//					// "name", false);
//					it.url = new MyURL();
//					// it.url.original = Utils.readArrayAttr(item, "isShownAt",
//					// false);
//					it.url.fromSourceAPI = "http://www.europeanafashion.eu/record/a/" + it.id;
//					a.add(it);
//				}
//				res.items = a;
//
//				CommonFilterLogic dataProvider = CommonFilterLogic.dataproviderFilter();
//
//				JsonNode o = response.path("facets");
//				readList(o.path("dataProviders"), dataProvider);
//
//				res.filtersLogic = new ArrayList<>();
//				res.filtersLogic.add(dataProvider);
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

		return res;
	}

	private void readList(JsonNode json, CommonFilterLogic filter) {
		for (JsonNode node : json.path("terms")) {
			String label = node.path("term").asText();
			int count = node.path("count").asInt();
			String id = node.path("uri").asText();
			countValue(filter, label, count);
		}
	}

	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		ArrayList<RecordJSONMetadata> jsonMetadata = new ArrayList<RecordJSONMetadata>();
		JsonNode response;
		try {
			response = getHttpConnector().getURLContent("http://www.europeanafashion.eu/api/record/" + recordId);
			JsonNode record = response;
			jsonMetadata.add(new RecordJSONMetadata(Format.JSON_EDM, record.toString()));
			return jsonMetadata;
		} catch (Exception e) {
			return jsonMetadata;
		}
	}

}
