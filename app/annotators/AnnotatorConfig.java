package annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.annotations.bodies.AnnotationBodyTagging;
import model.annotations.bodies.AnnotationBodyTagging.Vocabulary;

import com.fasterxml.jackson.databind.JsonNode;

public class AnnotatorConfig {
	private Class<? extends Annotator> annotator;
	private Map<String, Object> props;
	
	public AnnotatorConfig(Class<? extends Annotator> annotator, Map<String, Object> props) {
		this.setAnnotatorClass(annotator);
		this.setProps(props);
	}

	public Class<? extends Annotator> getAnnotatorClass() {
		return annotator;
	}

	public void setAnnotatorClass(Class<? extends Annotator> annotator) {
		this.annotator = annotator;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}
	
	public String toString() {
		return annotator.getName() + " -- " + (props != null ? props.toString() : "null");
	}
	
	public static List<AnnotatorConfig> createAnnotationConfigs(JsonNode json) {
		List<AnnotatorConfig> annConfigs = new ArrayList<>();
		
		JsonNode vocs = json.get("vocabulary");
		if (vocs != null) {
			Map<Class<? extends Annotator>, Set<AnnotationBodyTagging.Vocabulary>> annotatorMap = new HashMap<>();

			for (Iterator<JsonNode> iter = vocs.elements(); iter.hasNext();) {
				Vocabulary voc = AnnotationBodyTagging.Vocabulary.getVocabulary(iter.next().asText());
				Class<? extends Annotator> annClass = voc.getAnnotator();
				
				Set<AnnotationBodyTagging.Vocabulary> vocSet = annotatorMap.get(annClass);
				if (vocSet == null) {
					vocSet = new HashSet<AnnotationBodyTagging.Vocabulary>();
					annotatorMap.put(annClass, vocSet);
				}
				
				vocSet.add(voc);
			}
			
			for (Map.Entry<Class<? extends Annotator>, Set<AnnotationBodyTagging.Vocabulary>> entry : annotatorMap.entrySet()) {
				Map<String, Object> props = new HashMap<>();
				props.put(DictionaryAnnotator.VOCABULARIES, entry.getValue());
				
				annConfigs.add(new AnnotatorConfig(entry.getKey(), props));
			}
		}
		
		JsonNode ners = json.get("ner");
		if (ners != null) {
			Set<Class<? extends Annotator>> annotatorMap = new HashSet<>();
			
			for (Iterator<JsonNode> iter = ners.elements(); iter.hasNext();) {
				Vocabulary voc = AnnotationBodyTagging.Vocabulary.getVocabulary(iter.next().asText());
				Class<? extends Annotator> annClass = voc.getAnnotator();
				
				annotatorMap.add(annClass);
			}
			
			for (Class<? extends Annotator> entry : annotatorMap) {
				annConfigs.add(new AnnotatorConfig(entry, null));
			}
		}
		
		return annConfigs;
	}
	
}