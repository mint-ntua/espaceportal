package sources;

import java.util.ArrayList;

import model.resources.RecordResource;

import org.elasticsearch.action.search.SearchResponse;

import play.Logger;
import sources.core.CommonQuery;
import sources.core.ISpaceSource;
import sources.core.RecordJSONMetadata;
import sources.core.SourceResponse;
import elastic.Elastic;
import elastic.ElasticSearcher;

/*
 * This source is for internal search to WITH collections
 */
public class ElasticSource extends ISpaceSource {
	public static final Logger.ALogger log = Logger.of(ElasticSource.class);
	
	public ElasticSource(){
		super(null);
		throw new RuntimeException("This source needs to be defined on Sources enum");
	}

	@Override
	public String getHttpQuery(CommonQuery q) {
		log.debug("Method not implemented yet");
		return null;
	}

	@Override
	public SourceResponse getResults(CommonQuery q) {
		ElasticSearcher searcher = new ElasticSearcher();
		String term = q.getQuery();
		SearchResponse resp = searcher.search(term);
		searcher.closeClient();
		return new SourceResponse(resp, Integer.parseInt(q.page));
	}

	@Override
	public ArrayList<RecordJSONMetadata> getRecordFromSource(String recordId, RecordResource fullRecord) {
		log.debug("Method not implemented yet");
		return null;
	}

}
