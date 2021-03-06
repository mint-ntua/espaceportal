package controllers.thesaurus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import model.DescriptiveData;
import model.annotations.Annotation;
import model.annotations.ContextData;
import model.annotations.ContextData.ContextDataBody;
import model.annotations.bodies.AnnotationBodyTagging;
import model.basicDataTypes.Language;
import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.MultiLiteral;
import model.basicDataTypes.MultiLiteralOrResource;
import model.resources.CulturalObject.CulturalObjectData;
import model.resources.PlaceObject.PlaceData;
import model.resources.AgentObject.AgentData;
import model.resources.RecordResource;
import model.resources.ThesaurusObject.SKOSTerm;

import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;

import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.CollectionObjectController;
import controllers.WithResourceController;
import controllers.thesaurus.struct.BodyClass;
import controllers.thesaurus.struct.Counter;
import controllers.thesaurus.struct.SortClass;
import controllers.thesaurus.struct.ThesaurusFacet;
import db.DB;
import db.ThesaurusObjectDAO;
import elastic.ElasticSearcher;
import elastic.ElasticSearcher.SearchOptions;

public class CollectionIndexController extends WithResourceController	{

	public static final ALogger log = Logger.of(CollectionObjectController.class);

	public static String[] indexFacetFields = new String[] {
		"keywords.uri.all",
		"dctype.uri.all",
		"dcformat.uri.all",
		"dctermsmedium.uri.all"
	};

	public static String[] indexAutocompleteFields = new String[] {
		"keywords_all.all",
		"dctype_all.all",
		"dcformat_all.all",
		"dctermsmedium_all.all"
	};
	
	public static Result getCollectionFacets(String id) {
		ObjectNode result = Json.newObject();
		
		try {
			Result response = errorIfNoAccessToCollection(Action.READ, new ObjectId(id));
					
			if (!response.toString().equals(ok().toString())) {
				return response;
			}
			
			JsonNode json = request().body().asJson();

			ElasticSearcher es = new ElasticSearcher();
			
			QueryBuilder query = getIndexCollectionQuery(new ObjectId(id), json);
			
			SearchOptions so = new SearchOptions(0, Integer.MAX_VALUE);
			SearchResponse res = es.execute(query, so, indexFacetFields);
			SearchHits sh = res.getHits();

			List<String[]> list = new ArrayList<>();

			for (Iterator<SearchHit> iter = sh.iterator(); iter.hasNext();) {
				SearchHit hit = iter.next();

				List<Object> olist = new ArrayList<>();
				
				for (String field : indexFacetFields) {
					SearchHitField shf = hit.field(field);
				
					if (shf != null) {
						olist.addAll(shf.getValues());
					}				
				}				
				
				if (olist.size() > 0 ) {
					list.add(olist.toArray(new String[] {}));
				}
			}
			
			Set<String> selected = new HashSet<>();
			if (json != null) {
				for (Iterator<JsonNode> iter = json.get("terms").elements(); iter.hasNext();) {
					selected.add(iter.next().get("top").asText());
				}
			}
			
			ThesaurusFacet tf = new ThesaurusFacet();
			tf.create(list, selected);
			
			return ok(tf.toJSON(Language.EN));

		} catch (Exception e) {
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}

	public static Result getCollectionKeywords(String id) {
		ObjectNode result = Json.newObject();
		
		try {
			Result response = errorIfNoAccessToCollection(Action.READ, new ObjectId(id));
						
			if (!response.toString().equals(ok().toString())) {
				return response;
			}
			
			JsonNode json = request().body().asJson();

			ElasticSearcher es = new ElasticSearcher();
			
			QueryBuilder query = getIndexCollectionQuery(new ObjectId(id), json);

			SearchResponse res = es.execute(query, new SearchOptions(0, Integer.MAX_VALUE), indexAutocompleteFields);
			SearchHits sh = res.getHits();

			Map<Object, Counter> list = new HashMap<>();

			for (Iterator<SearchHit> iter = sh.iterator(); iter.hasNext();) {
				SearchHit hit = iter.next();

				Set<Object> olist = new HashSet<>();
				
				for (String field : indexAutocompleteFields) {
					SearchHitField shf = hit.field(field);
				
					if (shf != null) {
						olist.addAll(shf.getValues());
					}				
				}		
				
				for (Object t : olist) {
					if (!t.toString().startsWith("http")) {
						Counter c = list.get(t);
						if (c == null) {
							c = new Counter(0);
							list.put(t, c);
						}
						c.increase();
					}
				}
			}
			
			ObjectNode reply = Json.newObject();
			ArrayNode terms = Json.newObject().arrayNode();

			for (Map.Entry<Object, Counter> entry : list.entrySet()) {
				ObjectNode element = Json.newObject();
				element.put("word", entry.getKey().toString());
				element.put("count", entry.getValue().getValue());
				
				terms.add(element);
			}
			
			reply.put("terms", terms);
			
			return ok(reply);

		} catch (Exception e) {
//			e.printStackTrace();
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}
	
	public static Result getCollectionAnnotations(String cid) {
		ObjectNode result = Json.newObject();
		
		try {
			Result response = errorIfNoAccessToCollection(Action.READ, new ObjectId(cid));
			
			if (!response.toString().equals(ok().toString())) {
				return response;
			}
			
			List<ContextData<ContextDataBody>> rr = DB.getCollectionObjectDAO().getById(new ObjectId(cid)).getCollectedResources();
			
			Map<BodyClass, Counter> annMap = new HashMap();
			
			for (ContextData<ContextDataBody> cd : rr) {
				ObjectId rid = cd.getTarget().getRecordId();

				RecordResource<RecordResource.RecordDescriptiveData> rec = DB.getRecordResourceDAO().getById(rid);
				rec.fillAnnotations();
					
				Set<BodyClass> uris = new HashSet<>();
					
				for (Annotation ann : rec.getAnnotations()) {
					AnnotationBodyTagging body = (AnnotationBodyTagging)ann.getBody();
					if (body.getUri() != null) {
						uris.add(new BodyClass(body.getUri(), body.getLabel()));
					}
				}
				
				for (BodyClass bc : uris) {
					Counter cc = annMap.get(bc);
					if (cc == null) {
						cc = new Counter(0);
						annMap.put(bc, cc);
					}
					
					cc.increase();
				}
			}
			
			Set<SortClass> sorted = new TreeSet<>();
			
			for (Map.Entry<BodyClass, Counter> entry : annMap.entrySet()) {
				sorted.add(new SortClass(entry.getKey().uri, entry.getKey().label, entry.getValue().getValue()));
			}
			
			ArrayNode array = Json.newObject().arrayNode();

			for (SortClass sc : sorted) {
				ObjectNode entry = Json.newObject();
				entry.put("uri", sc.uri);
				entry.put("label", Json.toJson(sc.label));
				entry.put("count", sc.count);
				
				array.add(entry);
			}

			result.put("annotations", array);
			
			return ok(result);
			
		} catch (Exception e) {
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}
	

	public static QueryBuilder getIndexCollectionQuery(ObjectId colId, JsonNode json) {
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		query.must(QueryBuilders.termQuery("collectedId", colId));

		if (json != null) {
			JsonNode terms = json.get("terms");
			
			if (terms != null) {
				for (Iterator<JsonNode> iter = terms.elements(); iter.hasNext();) {
					BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		
					JsonNode inode = iter.next();
					for (Iterator<JsonNode> iter2 = inode.get("sub").elements(); iter2.hasNext();) {
						String s = iter2.next().asText();
		
						for (String f : indexFacetFields) {
							boolQuery = boolQuery.should(QueryBuilders.termQuery(f, s));
						}
					}
					
					query.must(boolQuery);
				}
			}
			
			JsonNode keywords = json.get("keywords");
			if (keywords != null) {
				for (Iterator<JsonNode> iter = keywords.elements(); iter.hasNext();) {
					BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		
					String s = iter.next().asText();
					for (String f : indexAutocompleteFields) {
						boolQuery = boolQuery.should(QueryBuilders.termQuery(f, s));
					}
					
					query.must(boolQuery);
				}
			}
		}

		return query;
	}

	public static QueryBuilder getSimilarItemsIndexCollectionQuery(ObjectId colId, DescriptiveData dd) {
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		query.must(QueryBuilders.termQuery("collectedId", colId));

		ThesaurusObjectDAO thesaurusDAO = DB.getThesaurusDAO();
		
		MultiLiteral label = dd.getLabel();
		if (label != null) {
			for (Map.Entry<String, List<String>> entry : label.entrySet()) {
				String lang = entry.getKey();
				if (lang.equals(Language.DEFAULT.getDefaultCode())) {
					continue;
				}
				lang = lang.toLowerCase();
				
				for (String v : entry.getValue()) {
					query.should(QueryBuilders.matchQuery("label." + lang, v));	
					query.should(QueryBuilders.matchQuery("altLabels." + lang, v));
				}
			}
		}
		
		MultiLiteral altlabel = dd.getAltLabels();
		if (altlabel != null) {
			for (Map.Entry<String, List<String>> entry : altlabel.entrySet()) {
				String lang = entry.getKey();
				if (lang.equals(Language.DEFAULT.getDefaultCode())) {
					continue;
				}
				lang = lang.toLowerCase();
				
				for (String v : entry.getValue()) {
					query.should(QueryBuilders.matchQuery("label." + lang, v));	
					query.should(QueryBuilders.matchQuery("altLabels." + lang, v));
				}
			}
		}
		
		MultiLiteral descr = dd.getDescription();
		if (descr != null) {
			for (Map.Entry<String, List<String>> entry : descr.entrySet()) {
				String lang = entry.getKey();
				if (lang.equals(Language.DEFAULT.getDefaultCode())) {
					continue;
				}
				lang = lang.toLowerCase();

				for (String v : entry.getValue()) {
					query.should(QueryBuilders.matchQuery("description." + lang, v));
				}
			}
		}
		
		addMultiLiteralOrResource(dd.getKeywords(), "keywords", thesaurusDAO, query);
		
		if (dd instanceof CulturalObjectData) {
			addMultiLiteralOrResource(((CulturalObjectData)dd).getDctype(), "dctype", thesaurusDAO, query);
			addMultiLiteralOrResource(((CulturalObjectData)dd).getDcformat(), "dcformat", thesaurusDAO, query);
			addMultiLiteralOrResource(((CulturalObjectData)dd).getDctermsmedium(), "dctermsmedium", thesaurusDAO, query);
		} else if (dd instanceof PlaceData) {
			addMultiLiteralOrResource(((PlaceData)dd).getNation(), "nation", thesaurusDAO, query);
			addMultiLiteralOrResource(((PlaceData)dd).getContinent(), "continent", thesaurusDAO, query);
			addMultiLiteralOrResource(((PlaceData)dd).getPartOfPlace(), "partofplace", thesaurusDAO, query);
		} else if (dd instanceof AgentData) {
			addMultiLiteralOrResource(((AgentData)dd).getBirthPlace(), "birthplace", thesaurusDAO, query);
		}
		
//		System.out.println(query);

		return query;
	}
	
	private static void addMultiLiteralOrResource(MultiLiteralOrResource source, String field, ThesaurusObjectDAO thesaurusDAO, BoolQueryBuilder query) {
		if (source != null) {
			List<String> uris = source.get(LiteralOrResource.URI);
			if (uris != null) {
				Set<String> broader = new HashSet<>();
				for (String uri : uris) {
					List<SKOSTerm> terms = thesaurusDAO.getByUri(uri).getSemantic().getBroaderTransitive();
					if (terms != null) {
						for (SKOSTerm t : terms) {
							broader.add(t.getUri());
						}
					}
				}
				
				for (String f : uris) {
					query.should(QueryBuilders.termQuery(field + ".uri.all", f).boost(4));
				}
				
				for (String f : broader) {
					query.should(QueryBuilders.termQuery(field + ".uri.all", f).boost(2));
				}
			}
			
			for (Map.Entry<String, List<String>> entry : source.entrySet()) {
				String lang = entry.getKey();
				if (lang.equals(LiteralOrResource.URI) || lang.equals(Language.DEFAULT.getDefaultCode())) {
					continue;
				}
				lang = lang.toLowerCase();
				
				for (String v : entry.getValue()) {
					query.should(QueryBuilders.matchQuery(field + "." + lang, v));
				}
			}
		}
	}

}
