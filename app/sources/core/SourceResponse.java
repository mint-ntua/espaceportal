package sources.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.CulturalObject;
import model.resources.RecordResource;
import model.resources.WithResource;

import org.elasticsearch.action.search.SearchResponse;

import utils.ListUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties({"filtersLogic","resourcesPerType", "facets"})
public class SourceResponse {

	public String query;
	public int totalCount;
	public int startIndex;
	public int count;
	public ItemsGrouping items;
	public Map<String, List<?>> resourcesPerType;
	public String source;
	public JsonNode facets;
	private List<CommonFilterResponse> filters;
	public List<CommonFilterLogic> filtersLogic;

	public SourceResponse() {
		items = new ItemsGrouping();
		filters = new ArrayList<>();
	}

	public SourceResponse(int totalHits, int offset, int count) {
		items = new ItemsGrouping();
		filters = new ArrayList<>();
		this.totalCount = totalHits;
		this.startIndex = offset;
		this.count = count;
	}
	//source refers to the external APIs and the WITH db
	//comesFrom in each record in the WITH db indicates where it was imported from, i.e. external APIs, Mint or UploadedByUser
	public SourceResponse(SearchResponse resp, int offset) {
		this.totalCount = (int) resp.getHits().getTotalHits();
		this.source = "WITHin";
		this.startIndex = offset;
		List<WithResource<?, ?>> items = new ArrayList<WithResource<?, ?>>();

//		TODO make it using the new model...
//for (CollectionRecord er : elasticrecords) {
//	ItemsResponse it = new ItemsResponse();
//	List<CollectionRecord> rs = DB.getCollectionRecordDAO().getByExternalId(er.getExternalId());
//	if (rs != null && !rs.isEmpty()) {
//		CollectionRecord r = rs.get(0);
//		it.comesFrom = r.getSource();
//		it.title = r.getTitle();
//		it.description = r.getDescription();
//		it.id = r.getDbId().toString();
//		if(r.getThumbnailUrl() != null)
//			it.thumb = Arrays.asList(r.getThumbnailUrl().toString());
//		if (r.getIsShownBy() != null)
//			it.fullresolution = Arrays.asList(r.getIsShownBy().toString());
//		it.url = new MyURL();
//		it.url.fromSourceAPI = r.getSourceUrl();
//		it.provider = r.getProvider();
//		it.externalId = r.getExternalId();
//		it.type = r.getType();
//		it.rights = r.getItemRights();
//		it.dataProvider = r.getDataProvider();
//		it.creator = r.getCreator();
//		it.year = new ArrayList<>(Arrays.asList(r.getYear()));
//	    it.tags = er.getTags();
//		items.add(it);
//	}
//}
		this.items.setCulturalCHO(items);
	}

	public Map<String, List<?>> getResourcesPerType() {
		return resourcesPerType;
	}
	public void setResourcesPerType(Map<String, List<?>> resourcesPerType) {
		this.resourcesPerType = resourcesPerType;
	}
	
	public void setSource(){
		for (CommonFilterResponse f : filters) {
			f.addSource(Sources.getSourceByID(this.source));
		}
	}

	public List<CommonFilterResponse> getFilters() {
		return filters;
	}

	public void setFilters(List<CommonFilterResponse> filters) {
		this.filters = filters;
		setSource();
	}

	public SourceResponse merge(SourceResponse r2) {
		SourceResponse res = new SourceResponse();
		res.source = r2.source;
		res.query = query;
		res.count = count + r2.count;
		res.items = new ItemsGrouping();
		if (items != null)
			res.items.addAll(items);
		if (r2.items != null)
			res.items.addAll(r2.items);
		if ((filtersLogic != null) && (r2.filtersLogic != null)) {
			res.filtersLogic = filtersLogic;
			FiltersHelper.merge(res.filtersLogic, r2.filtersLogic);
			res.setFilters(ListUtils.transform(res.filtersLogic, (CommonFilterLogic x) -> {
				return x.export();
			}));
		}
		return res;
	}

	public void addItem(WithResource<?, ?> record) {
		if (record != null) {
			if (record instanceof CulturalObject)
				items.getCulturalCHO().add(record);
			else if (record instanceof RecordResource)
				items.getRecordResource().add(record);
		}
	}

	@Override
	public String toString() {
		return "SourceResponse [source=" + source + ", query=" + query + ", totalCount=" + totalCount + "]";
	}

	public void transformResourcesToItems() {
		List<WithResource<?, ?>> resources = new ArrayList<WithResource<?, ?>>();
		for (Entry<String, List<?>> e: resourcesPerType.entrySet())
			if (!e.getKey().equals("collectionobject"))
				resources.addAll((List<WithResource<?, ?>>) e.getValue());
		items.setCulturalCHO(resources);
	}

}

