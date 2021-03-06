package sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.basicDataTypes.ProvenanceInfo.Sources;
import model.basicDataTypes.WithAccess;
import model.basicDataTypes.WithAccess.Access;
import model.resources.RecordResource;
import model.resources.WithResource;
import model.resources.WithResource.WithResourceType;
import model.resources.collection.CollectionObject;
import model.usersAndGroups.User;

import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import db.DB;
import play.Logger;
import play.libs.Json;
import sources.core.CommonFilter;
import sources.core.CommonFilterLogic;
import sources.core.CommonFilters;
import sources.core.CommonQuery;
import sources.core.ISpaceSource;
import sources.core.RecordJSONMetadata;
import sources.core.SourceResponse;
import utils.ListUtils;
import utils.Tuple;
import elastic.Elastic;
import elastic.ElasticSearcher;
import elastic.ElasticSearcher.SearchOptions;
import elastic.ElasticUtils;

public class WithSpaceSource extends ISpaceSource {
	public static final Logger.ALogger log = Logger.of(WithSpaceSource.class);

	//there should be a filter on source
	//in general, more filters in new model for search within WITH db
	/*public enum WithinFilters {
		Provider("provider"), Type("type"), DataProvider("dataprovider"),
		Creator("creator"), Rights("rights"),
		Country("country"), Year("year");

		private String value;

		WithinFilters(String value) {
	        this.value = value;
	    }

	    @Override
	    public String toString() {
	        return value;
	    }
	}*/

	@Override
	public SourceResponse getResults(CommonQuery q) {

		ElasticSearcher searcher = new ElasticSearcher();
		SearchOptions elasticoptions = new SearchOptions();

		/*
		 * Get the search parameters from the query
		 */
		String term = q.getQuery();
		List<String> types = q.getTypes();
		int count = Integer.parseInt(q.pageSize);
		int offset = (Integer.parseInt(q.page)-1)*count;

		/* Access parameters */
		List<String> userIds = q.getEffectiveUserIds();
		List<Tuple<ObjectId, Access>> userAccess = new ArrayList<Tuple<ObjectId, Access>>();
		if ((userIds != null) && !userIds.isEmpty()) {
			for (String userId : userIds)
				userAccess.add(new Tuple<ObjectId, Access>(
						new ObjectId(userId), Access.READ));
		}
		List<List<Tuple<ObjectId, Access>>> accessFilters = mergeLists(
				q.getDirectlyAccessedByGroupName(),
				q.getDirectlyAccessedByUserName());
		if (!userAccess.isEmpty())
			accessFilters.add(userAccess);

		/*
		 * Prepare options for searching
		 */
		searcher.setTypes(types);
		elasticoptions.setCount(count);
		elasticoptions.setOffset(offset);
		elasticoptions.setScroll(false);
		elasticoptions.accessList = accessFilters;

		/* Filters */
		//elasticoptions.addFilter("isPublic", "true");
		List<CommonFilter> filters = q.filters;
		if (filters!=null){
			for (CommonFilter f: filters) {
				for (String filterValue: f.values) {
		    		elasticoptions.addFilter(f.filterID+".all", filterValue);
				}
			}

		}
		
		/*
		 * Search index for accessible resources
		 */
		SearchResponse elasticResponse = searcher
				.searchResourceWithWeights(term, elasticoptions);
		Map<String, List<?>> resourcesPerType = ElasticUtils.getResourcesPerType(elasticResponse);

		/* Finalize the searcher client and create the SourceResponse */
		searcher.closeClient();
		SourceResponse sourceResponse =
				new SourceResponse((int) elasticResponse.getHits().getTotalHits(), offset, count);
		sourceResponse.source = getSourceName().toString();
		sourceResponse.setResourcesPerType(resourcesPerType);
		//sourceResponse.transformResourcesToItems();
		filterRecordsOnly(sourceResponse);
		/* Check whether we need the aggregated values or not */
		if (checkFilters(q)) {
			sourceResponse.filtersLogic = new ArrayList<CommonFilterLogic>();
			if(elasticResponse.getAggregations() != null)
				for (Aggregation agg : elasticResponse.getAggregations().asList()) {
					InternalTerms aggTerm = (InternalTerms) agg;
					if (aggTerm.getBuckets().size() > 0) {
						CommonFilterLogic filter = new CommonFilterLogic(agg.getName());
						//CommonFilters.valueOf(agg.getName()));
						for (int i=0; i< aggTerm.getBuckets().size(); i++) {
							countValue(filter, aggTerm.getBuckets().get(i).getKey(),
								(int) aggTerm.getBuckets().get(0).getDocCount());
						}
						sourceResponse.filtersLogic.add(filter);
					}
				}
		}
		return sourceResponse;
	}

	public WithSpaceSource() {
		super(Sources.WITHin);
	}

	private List<SearchHit> getTotalHitsFromScroll(SearchResponse scrollResp) {
		List<SearchHit> totalHits = new ArrayList<SearchHit>();
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits())
				totalHits.add(hit);

			scrollResp = Elastic.getTransportClient()
					.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(60000)).execute().actionGet();

			if (scrollResp.getHits().getHits().length == 0)
				break;
		}
		return totalHits;
	}

	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		log.debug("Method not implemented yet");
		return null;
	}

	private List<List<Tuple<ObjectId, Access>>> mergeLists(List<Tuple<ObjectId, Access>>... lists) {
		List<List<Tuple<ObjectId, Access>>> outputList = new ArrayList<List<Tuple<ObjectId, Access>>>();
		for (List<Tuple<ObjectId, Access>> list : lists) {
			for (Tuple<ObjectId, Access> tuple : list) {
				List<Tuple<ObjectId, Access>> sepList = new ArrayList<Tuple<ObjectId, Access>>(
						1);
				sepList.add(tuple);
				outputList.add(sepList);
			}
		}
		return outputList;
	}

	
	//TODO: When WIthin search is separated from external resources, 
	// and the response may contain resources of all types (not only CHO and RecordResource as defined in ItemsGrouping),
	//this method becomes obsolete.
	//Types should be passed from the API call, and handled at the within search controller level.
	private void filterRecordsOnly(SourceResponse sourceResponse) {
		List<WithResource<?, ?>> choItems = sourceResponse.items.getCulturalCHO();
		List<WithResource<?, ?>> recordResources = sourceResponse.items.getRecordResource();
		for (Entry<String, List<?>> e: sourceResponse.resourcesPerType.entrySet()) {
			if (e.getKey().equals(WithResourceType.CulturalObject.toString().toLowerCase())) 
				for (WithResource<?, ?>  record: (List<WithResource<?, ?>>) e.getValue()) {
					RecordResource profiledRecord = ((RecordResource) record).getRecordProfile("MEDIUM");
					choItems.add(profiledRecord);
				}
			if (e.getKey().equals(WithResourceType.RecordResource.toString().toLowerCase())) 
				for (WithResource<?, ?>  record: (List<WithResource<?, ?>>) e.getValue()) {
					RecordResource profiledRecord = ((RecordResource) record).getRecordProfile("MEDIUM");
					recordResources.add(profiledRecord);
				}
		}
		sourceResponse.items.setCulturalCHO(choItems);
		sourceResponse.items.setRecordResource(recordResources);
	}
}
