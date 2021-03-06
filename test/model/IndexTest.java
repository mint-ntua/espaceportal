package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import model.basicDataTypes.Language;
import model.basicDataTypes.Literal;
import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.MultiLiteral;
import model.resources.CollectionObject;
import model.resources.ThesaurusObject;
import model.resources.ThesaurusObject.SKOSSemantic;
import model.resources.ThesaurusObject.SKOSTerm;
import model.EmbeddedMediaObject.WithMediaRights;
import model.EmbeddedMediaObject.WithMediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AccessManager.Action;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;

import controllers.CollectionObjectController;
import controllers.thesaurus.CollectionIndexController;
import controllers.thesaurus.ThesaurusFacet;
import db.DB;
import db.ThesaurusObjectDAO;
import elastic.Elastic;
import elastic.ElasticSearcher;
import elastic.ElasticUtils;
import elastic.ElasticSearcher.SearchOptions;

public class IndexTest {

	@Test
	public void genericTest() throws FileNotFoundException {

//		Object d = DB.getRecordResourceDAO().getById(new ObjectId("56fa7b92c743431f1cd7264c"));
//		System.out.println(d);
		
		
		String id = "571a1b25c743434b2c454c79";
//		String[] uris = new String[] {"http://bib.arts.kuleuven.be/photoVocabulary/30214"};
		
//	Result x = CollectionIndexController.getCollectionIndex(id);
		
//		CollectionObjectController.xlistRecordResources(id, "contentOnly", 0, 10, uris);
		
		System.out.println(id);
		ElasticSearcher es = new ElasticSearcher();
		
		
		JsonNode json = Json.parse("{ \"terms\": [ ] }");
//		JsonNode json = Json.parse("{ \"terms\": [ { \"top\": \"http://bib.arts.kuleuven.be/photoVocabulary/31401\" } ] }");
		System.out.println(id);
		
//				MatchQueryBuilder query = QueryBuilders.matchQuery("collectedIn.collectionId", id);
		QueryBuilder query = CollectionIndexController.getIndexCollectionQuery(new ObjectId(id), json);

		System.out.println(query);
		
		SearchOptions so = new SearchOptions(0, Integer.MAX_VALUE);

		SearchResponse res = es.execute(query, so, CollectionIndexController.indexFacetFields);
		
		SearchHits sh = res.getHits();

		List<String[]> list = new ArrayList<>();

		for (Iterator<SearchHit> iter = sh.iterator(); iter.hasNext();) {
			SearchHit hit = iter.next();

			List<Object> olist = new ArrayList<>();
			
			for (String field : CollectionIndexController.indexFacetFields) {
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
		
//		PrintWriter out = new PrintWriter("collection-index.txt");
		
		System.out.println("RES " + tf.toJSON(Language.EN));
//		out.print(tf.toJSON(Language.EN));
//		out.close();
		
//		JsonNode json = Json.parse("{ \"administrative\": { \"externalId\": \"http://vocab.getty.edu/aat/300073692\", \"created\": { \"$date\": 1450700821484 } , \"lastModified\": { \"$date\": 1450700821484 } }, \"semantic\": { \"uri\": \"http://vocab.getty.edu/aat/300073692\", \"type\": \"http://www.w3.org/2004/02/skos/core#Collection\", \"prefLabel\": { \"en\": \"<balustrades, railings and their components>\", \"es\": \"<balaustradas, barandas y sus componentes>\", \"nl\": \"<balustrades, leuningen en hun onderdelen>\" }, \"members\": [ { \"uri\": \"http://vocab.getty.edu/aat/300001988\", \"type\": \"http://www.w3.org/2004/02/skos/core#Concept\", \"prefLabel\": { \"en\": \"barrier elements\", \"es\": \"elementos de barrera\", \"nl\": \"hekwerkelementen\" }, \"altLabel\": { \"en\": [ \"barrier element\", \"elements, barrier\" ] } }, { \"uri\": \"http://vocab.getty.edu/aat/300163880\", \"type\": \"http://www.w3.org/2004/02/skos/core#Concept\", \"prefLabel\": { \"en\": \"barriers and barrier elements\", \"es\": \"barreras y elementos de barrera\", \"nl\": \"hekwerken en hekwerkonderdelen\" } }, { \"uri\": \"http://vocab.getty.edu/aat/300000885\", \"type\": \"http://www.w3.org/2004/02/skos/core#Concept\", \"prefLabel\": { \"en\": \"architectural elements\", \"es\": \"elementos de arquitectura\", \"nl\": \"architecturale elementen\", \"zh\": \"建築元素\" }, \"altLabel\": { \"en\": [ \"architectural element\", \"elements, architectural\" ], \"es\": [ \"elemento de arquitectura\" ], \"nl\": [ \"architecturaal element\", \"bouwfragment\", \"bouwfragmenten\" ], \"zh\": [ \"jiàn zhú yuán sù\", \"jian zhu yuan su\", \"chien chu yüan su\" ] } }, { \"uri\": \"http://vocab.getty.edu/aat/300241584\", \"type\": \"http://www.w3.org/2004/02/skos/core#Collection\", \"prefLabel\": { \"en\": \"<components by specific context>\", \"es\": \"<componentes según contexto específico>\", \"nl\": \"<onderdelen naar specifieke context>\" }, \"altLabel\": { \"nl\": [ \"<onderdeel naar specifieke context>\" ] } }, { \"uri\": \"http://vocab.getty.edu/aat/300241583\", \"type\": \"http://www.w3.org/2004/02/skos/core#Concept\", \"prefLabel\": { \"en\": \"components (objects parts)\", \"es\": \"componentes\", \"nl\": \"onderdelen\" }, \"altLabel\": { \"en\": [ \"component (component object)\", \"parts (component objects)\" ], \"es\": [ \"componente\" ], \"nl\": [ \"onderdeel\" ] } }, { \"uri\": \"http://vocab.getty.edu/aat/300241490\", \"type\": \"http://www.w3.org/2004/02/skos/core#Collection\", \"prefLabel\": { \"en\": \"Components (hierarchy name)\", \"es\": \"componentes\", \"fr\": \"Composantes\", \"nl\": \"Onderdelen\" } }, { \"uri\": \"http://vocab.getty.edu/aat/300264092\", \"type\": \"http://www.w3.org/2004/02/skos/core#Collection\", \"prefLabel\": { \"en\": \"Objects Facet\", \"es\": \"faceta objetos\", \"nl\": \"Facet Objecten\" } }, { \"uri\": \"http://vocab.getty.edu/aat/300241508\", \"type\": \"http://www.w3.org/2004/02/skos/core#Collection\", \"prefLabel\": { \"en\": \"<object groupings by general context>\", \"es\": \"<grupos de objetos por contexto general>\", \"nl\": \"<objectgroepen naar algemene context>\" }, \"altLabel\": { \"nl\": [ \"<objectgroep naar algemene context>\" ] } }, { \"uri\": \"http://vocab.getty.edu/aat/300241507\", \"type\": \"http://www.w3.org/2004/02/skos/core#Concept\", \"prefLabel\": { \"en\": \"object groupings\", \"es\": \"grupos de objetos\", \"fr\": \"groupements d'objets\", \"nl\": \"objectgroepen\" }, \"altLabel\": { \"en\": [ \"object grouping\", \"groupings, object\" ], \"nl\": [ \"objectgroep\" ] } }, { \"uri\": \"http://vocab.getty.edu/aat/300241489\", \"type\": \"http://www.w3.org/2004/02/skos/core#Collection\", \"prefLabel\": { \"en\": \"Object Groupings and Systems (hierarchy name)\", \"es\": \"sistemas y grupos de objetos\", \"fr\": \"Ensembles d'objets et systèmes\", \"nl\": \"Objectgroepen en systemen\" } } ] } }");
		
//		ThesaurusObject to = DB.getThesaurusDAO().getByUri("http://vocab.getty.edu/aat/300179417");
////		
//		Map<String, Object> map = to.transform();
//
//		for (Map.Entry<String, Object> entry : map.entrySet()) {
//		
//			if (entry.getValue() instanceof Map) {
//				for (Map.Entry<String, Object> entry2 : ((Map<String,Object>)entry.getValue()).entrySet()) {
//					System.out.println(entry.getKey() + "." + entry2.getKey() + " : " + entry2.getValue());
//				}
//			} else {
//				System.out.println(entry.getKey() + " : " + entry.getValue());
//			
//			}
//		}
		
//		
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/11076" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10182" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10070" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10110" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10403" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10030" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10055" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10345" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10327" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10432" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/11041" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10153" });
//		list.add(new String[] { "http://thesaurus.europeanafashion.eu/thesaurus/10811" });
//
//
//		ThesaurusFacet tf = new ThesaurusFacet();
//		tf.create(list);
//		System.out.println("OK");
//		String json = tf.toJSON(Language.EN);
//		System.out.println(json);
		

		
		
//		ThesaurusObject so = new ThesaurusObject();
//		so.setAdministrative(new ThesaurusObject.SKOSAdmin(new Date(), new Date(), "test"));
//		Literal lit = new Literal();
//		lit.put("en", "english");
//		lit.put("de", "german");
//
//		MultiLiteral mlit = new MultiLiteral();
//		ArrayList<String> text = new ArrayList<>();
//		text.add("enlgish1");
//		text.add("enlgish2");
//		mlit.put("en", text);
//
//		SKOSSemantic sem = new SKOSSemantic();
//		sem.setUri("http://vocab.getty.edu/aat/300073692");
//		sem.setType("http://www.w3.org/2004/02/skos/core#Collection");
//		
//		Literal sempref = new Literal();
//		sempref.put("en", "<balustrades, railings and their components>");
//		sempref.put("es", "<balaustradas, barandas y sus componentes>");
//		
//		sem.setPrefLabel(sempref);
//
//		Literal narpref = new Literal();
//		narpref.put("en", "nar1");
//		narpref.put("es", "nar2");
//
//		List<SKOSTerm> semnar = new ArrayList<>();
//		semnar.add(new SKOSTerm("a1","b1",narpref,null));
//		semnar.add(new SKOSTerm("a2","b2",narpref,null));
//		
//		sem.setNarrower(semnar);
//		so.setSemantic(sem);
//		
//		
////		so.term = new SKOSTerm("uri1", "type1", lit, mlit);

	}
	
	
}
