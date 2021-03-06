package general.elasticsearch;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.basicDataTypes.WithAccess;
import model.basicDataTypes.WithAccess.Access;
import model.resources.WithResource;
import model.resources.WithResource.WithResourceType;
import model.resources.collection.CollectionObject;
import model.usersAndGroups.User;

import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.suggest.Suggest;
import org.junit.Test;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import db.DB;
import utils.Tuple;
import elastic.Elastic;
import elastic.ElasticSearcher;
import elastic.ElasticSearcher.SearchOptions;

public class SearchTest {

	@Test
	public void testFederatedSearch() {
		ElasticSearcher searcher = new ElasticSearcher();
		searcher.addType(Elastic.typeResource);

		SearchOptions options = new SearchOptions(0, 10);
		options.setScroll(false);
		options.setFilterType("and");
		options.addFilter("dataProvider", "");
		options.addFilter("dataProvider", "");
		options.addFilter("provider", "");

		//Access Rights
		List<Tuple<ObjectId, Access>> userAccess = new ArrayList<Tuple<ObjectId, Access>>();
		userAccess.add(new Tuple<ObjectId, WithAccess.Access>(new ObjectId("55b637a7e4b0cbaeed931c95"), Access.READ));
		/*userAccess.add(new Tuple<ObjectId, WithAccess.Access>(new ObjectId(""), Access.READ));
		userAccess.add(new Tuple<ObjectId, WithAccess.Access>(new ObjectId(""), Access.READ));
		userAccess.add(new Tuple<ObjectId, WithAccess.Access>(new ObjectId(""), Access.READ));*/

		List<List<Tuple<ObjectId, Access>>> accessCriteria = new ArrayList<List<Tuple<ObjectId,Access>>>();
		accessCriteria.add(userAccess);
		options.accessList = accessCriteria;


		SearchResponse resp = searcher.search("dance", options);
		System.out.println(resp.getHits().getTotalHits());
	}

	@Test
	public void testRelatedWithDisMax() {
		ElasticSearcher searcher = new ElasticSearcher();
		searcher.addType(Elastic.typeResource);

		SearchOptions options = new SearchOptions(0, 10);
		options.setScroll(false);
		options.setFilterType("and");
		/*options.addFilter("dataProvider", "");
		options.addFilter("dataProvider", "");
		options.addFilter("provider", "");*/

		SearchResponse resp = searcher.relatedWithDisMax("eirinirecord1", "mint", null, options);
		for(SearchHit h: resp.getHits().getHits()) {
			System.out.println(h.getSourceAsString());
		}

	}


	@Test
	public void testRelatedWithMLT() {
		ElasticSearcher searcher = new ElasticSearcher();
		searcher.addType(Elastic.typeResource);

		SearchOptions options = new SearchOptions(0, 10);
		options.setScroll(false);
		options.setFilterType("or");
		options.addFilter("isPublic", "false");

		/*options.addFilter("dataProvider", "");
		options.addFilter("dataProvider", "");
		options.addFilter("provider", "");*/

		List<String> fields = new ArrayList<String>() {{ add("label_all");add("description_all");add("provider"); }};
		SearchResponse resp = searcher.relatedWithMLT("title Mint", null, fields, options);
		for(SearchHit h: resp.getHits().getHits()) {
			System.out.println(h.getSourceAsString());
		}
	}

	@Test
	public void testRelatedWithShouldClauses() {

	}

	@Test
	public void testSearchAccessibleCollections() {

	}

	@Test
	public void testSearchSuggestions() {
		ElasticSearcher searcher = new ElasticSearcher();
		searcher.addType(Elastic.typeResource);

		SearchOptions options = new SearchOptions(0, 10);
		options.setScroll(false);
	  /*options.addFilter("dataProvider", "");
		options.addFilter("dataProvider", "");
		options.addFilter("provider", "");*/

		SuggestResponse resp = searcher.searchSuggestions("eirimnnirecord1", "label_all", options);
		resp.getSuggest().getSuggestion("eirimnnirecord1").forEach( (o) ->  (o.forEach( (s) -> (
								System.out.println(s.getText() + " with score " + s.getScore())) )) );
		System.out.println("done");
	}

	@Test
	public void testListUserCollections() {

		User u = DB.getUserDAO().getByUsername("qwerty");
		if(u==null) return;

		ElasticSearcher searcher = new ElasticSearcher();
		searcher.addType(WithResourceType.CollectionObject.toString().toLowerCase());
		SearchOptions options = new SearchOptions();
		List<Tuple<ObjectId, Access>> user_acl = new ArrayList<Tuple<ObjectId,Access>>();
		user_acl.add(new Tuple<ObjectId, WithAccess.Access>(u.getDbId(), Access.OWN));
		options.accessList.add(user_acl);

		SearchResponse resp = searcher.searchAccessibleCollections(options);


		Tuple<List<CollectionObject>, Tuple<Integer, Integer>> tree =
				DB.getCollectionObjectDAO().getByAcl(options.accessList, u.getDbId(), false, true, 0, 200);

		assertEquals((int)resp.getHits().getTotalHits(), (int)tree.y.x);

	}

	@Test
	public void testSearchMycollections() {

	}

	@Test
	public void testSearchWithinCollection() {

	}
}
