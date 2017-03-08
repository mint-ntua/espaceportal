package model;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Test;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.RecordResourceController;
import db.DB;
import model.basicDataTypes.Language;
import model.basicDataTypes.MultiLiteral;
import model.resources.RecordResource;
import annotators.Annotation;
import annotators.Annotator;

public class AnnotationTest {

	@Test
	public void genericTest() throws Exception {
		String id = "571f767c713f216fa1b99307";
		
		RecordResource record = DB.getRecordResourceDAO().get(new ObjectId(id));

		String[] fields = new String[] {"description", "label"};
		
		ObjectNode annotations = Json.newObject();
		ArrayNode array = Json.newObject().arrayNode();
				
		Annotator annotator = Annotator.getAnnotatorByName("DBPediaSpotlightAnnotator");
		
		DescriptiveData dd = record.getDescriptiveData();
		
		for (String p : fields) {
			Method method = dd.getClass().getMethod("get" + p.substring(0,1).toUpperCase() + p.substring(1));
		
			Object res = method.invoke(dd);
			if (res instanceof MultiLiteral) {
				MultiLiteral value = (MultiLiteral)res;

				for (Language lang : value.getLanguages()) {
					Map<String, Object> props = new HashMap();
					props.put(Annotator.LANGUAGE, lang);
					
					for (String text : value.get(lang)) {

						for (Annotation ann : annotator.annotate(text, props)) {

							ObjectNode annotation = Json.newObject();
							annotation.put("generator", annotator.getName());

							ObjectNode body = Json.newObject();
							body.put("@id", ann.getURI());
							
							annotation.put("body", body);

							ObjectNode target = Json.newObject();
							
							target.put("source", "record/" + id);
							
							ObjectNode targetSelector = Json.newObject();
							targetSelector.put("@type", "FragmentSelector");
							targetSelector.put("value", "r=" + p + "&t=" +ann.getStartPosition() + "," + ann.getEndPosition());
							
							target.put("selector", targetSelector);

							annotation.put("target", target);

							array.add(annotation);
						}
					}
				}
			}
		}
		
		annotations.put("annotations", array);
		
		System.out.println(annotations); 
	}
}
