package annotators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import model.annotations.Annotation;
import model.annotations.Annotation.AnnotationAdmin;
import model.annotations.Annotation.MotivationType;
import model.annotations.bodies.AnnotationBodyTagging;
import model.annotations.selectors.PropertyTextFragmentSelector;
import model.annotations.targets.AnnotationTarget;
import model.basicDataTypes.Language;


import model.basicDataTypes.MultiLiteral;
import annotators.struct.AnnotatedObject;
import annotators.struct.AnnotationIndex;
import annotators.struct.AnnotationValue;
import annotators.struct.SimpleAnnotationValue;
import annotators.struct.Span;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NERAnnotator extends Annotator {

	public String getName() {
		return "WITH NLP Annotator";
	}
	
	public String getService() {
		return "";
	}
	
    protected static Map<Language, NERAnnotator> annotators = new HashMap<>();

    public static NERAnnotator getAnnotator(Language lang) {
    	if (lang != Language.EN) {
    		return null;
    	}
    	
    	NERAnnotator ta = annotators.get(lang);
    	if (ta == null) {
    		synchronized (NERAnnotator.class) {
    			ta = annotators.get(lang);
    			if (ta == null) {
    				ta  = new NERAnnotator();
    				annotators.put(lang, ta);
    			}
    		}
    	}
    	
    	return ta;
    }    	
    	
    private StanfordCoreNLP pipeline;
    
    private NERAnnotator() {
    	Properties props = new Properties();
//    	props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//    	props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
    	props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
//    	props.put("annotators", "tokenize, ssplit, pos, lemma");
//    	props.put("annotators", "tokenize, ssplit, pos");
//    	props.put("annotators", "tokenize, ssplit");
    	
    	pipeline = new StanfordCoreNLP(props);
    }

	@Override
	public List<Annotation> annotate(String text, AnnotationTarget target, Map<String, Object> properties) throws Exception {
		text = strip(text);
		
		List<Annotation> res = new ArrayList<>();
		
		AnnotationIndex ai = analyze(text);
		
		ArrayList<Span> locations = ai.getLocations("NE", new SimpleAnnotationValue("LOCATION"));
		if (locations != null) {
			for (Span span : locations) {
	    		Annotation<AnnotationBodyTagging> ann = new Annotation<>();
	    		
	    		AnnotationBodyTagging annBody = new AnnotationBodyTagging();
	    		annBody.setUri("http://nerd.eurecom.fr/ontology#Location");
	    		annBody.setUriVocabulary(AnnotationBodyTagging.Vocabulary.NERD);
	    		MultiLiteral ml = new MultiLiteral(Language.EN, "Location");
	    		ml.fillDEF();
    			annBody.setLabel(ml);

	    		AnnotationTarget annTarget = (AnnotationTarget) target.clone();
	    		
	    		PropertyTextFragmentSelector selector = (PropertyTextFragmentSelector)annTarget.getSelector();
	    		selector.setStart(span.start);
	    		selector.setEnd(span.end);
	    		
	    		annTarget.setSelector(selector);

	    		ArrayList<AnnotationAdmin> admins  = new ArrayList<>();
	    		AnnotationAdmin admin = new Annotation.AnnotationAdmin();
	    		admin.setGenerator(getName());
	    		admin.setGenerated(new Date());
	    		admin.setConfidence(-1.0f);
//	    		admin.setWithCreator(withCreator);
//	    		admin.setCreated(new Date());
	    		
	    		admins.add(admin);
	    		
	    		ann.setBody(annBody);
	    		ann.setTarget(annTarget);
	    		ann.setAnnotators(admins);
	    		ann.setMotivation(MotivationType.Tagging);
	    		
	    		res.add(ann);
			}
		}
		
		ArrayList<Span> persons = ai.getLocations("NE", new SimpleAnnotationValue("PERSON"));
		if (persons != null) {
			for (Span span : persons) {
	    		Annotation<AnnotationBodyTagging> ann = new Annotation<>();
	    		
	    		AnnotationBodyTagging annBody = new AnnotationBodyTagging();
	    		annBody.setUri("http://nerd.eurecom.fr/ontology#Person");
	    		annBody.setUriVocabulary(AnnotationBodyTagging.Vocabulary.NERD);
	    		MultiLiteral ml = new MultiLiteral(Language.EN, "Person");
	    		ml.fillDEF();
    			annBody.setLabel(ml);

	    		AnnotationTarget annTarget = (AnnotationTarget) target.clone();
	    		
	    		PropertyTextFragmentSelector selector = (PropertyTextFragmentSelector)annTarget.getSelector();
	    		selector.setStart(span.start);
	    		selector.setEnd(span.end);
	    		
	    		annTarget.setSelector(selector);

	    		ArrayList<AnnotationAdmin> admins  = new ArrayList<>();
	    		AnnotationAdmin admin = new Annotation.AnnotationAdmin();
	    		admin.setGenerator(getName());
	    		admin.setGenerated(new Date());
	    		admin.setConfidence(-1.0f);
//	    		admin.setWithCreator(withCreator);
//	    		admin.setCreated(new Date());

	    		admins.add(admin);
	    		
	    		ann.setBody(annBody);
	    		ann.setTarget(annTarget);
	    		ann.setAnnotators(admins);
	    		ann.setMotivation(MotivationType.Tagging);
	    		
	    		res.add(ann);
			}
		}

		ArrayList<Span> orgs = ai.getLocations("NE", new SimpleAnnotationValue("ORGANIZATION"));
		if (orgs != null) {
			for (Span span : orgs ) {
	    		Annotation<AnnotationBodyTagging> ann = new Annotation<>();
	    		
	    		AnnotationBodyTagging annBody = new AnnotationBodyTagging();
	    		annBody.setUri("http://nerd.eurecom.fr/ontology#Organization");
	    		annBody.setUriVocabulary(AnnotationBodyTagging.Vocabulary.NERD);
	    		MultiLiteral ml = new MultiLiteral(Language.EN, "Organization");
	    		ml.fillDEF();
    			annBody.setLabel(ml);

	    		AnnotationTarget annTarget = (AnnotationTarget) target.clone();
	    		
	    		PropertyTextFragmentSelector selector = (PropertyTextFragmentSelector)annTarget.getSelector();
	    		selector.setStart(span.start);
	    		selector.setEnd(span.end);
	    		
	    		annTarget.setSelector(selector);

	    		ArrayList<AnnotationAdmin> admins  = new ArrayList<>();
	    		AnnotationAdmin admin = new Annotation.AnnotationAdmin();
	    		admin.setGenerator(getName());
	    		admin.setGenerated(new Date());
	    		admin.setConfidence(-1.0f);
//	    		admin.setWithCreator(withCreator);
//	    		admin.setCreated(new Date());

	    		admins.add(admin);
	    		
	    		ann.setBody(annBody);
	    		ann.setTarget(annTarget);
	    		ann.setAnnotators(admins);
	    		ann.setMotivation(MotivationType.Tagging);
	    		
	    		res.add(ann);
			}
		}
		
		return res;
	}
	
	public AnnotationIndex analyze(String text) throws Exception {
		AnnotationIndex ta = new AnnotationIndex(text);
		
		edu.stanford.nlp.pipeline.Annotation document = new edu.stanford.nlp.pipeline.Annotation(text);

		pipeline.annotate(document);

       	for(CoreMap sentence: document.get(SentencesAnnotation.class)) {
       		
       		int ss = Integer.MAX_VALUE;
       		int se = Integer.MIN_VALUE;
    	   
       		int nerStart = -1;
       		int nerEnd= -1;
       		String nerType = "";

       		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {

       			int s = token.beginPosition();
       			int e = token.endPosition();
        	   
       			if (s < ss) {
        			ss = s;
       			}
       			if (e > ss) {
       				se = e;
       			}
        	   
       			ta.add("TOKEN", null, s, e);
        	   
       			ta.add("POS", new SimpleAnnotationValue(token.get(PartOfSpeechAnnotation.class)), s, e);
       			ta.add("LEMMA", new SimpleAnnotationValue(token.get(LemmaAnnotation.class)), s, e);

       			String n = token.get(NamedEntityTagAnnotation.class);
       			
       			if (n.equals("O") || !nerType.equals(n)) {
       				if (nerStart != -1) {
       					ta.add("NE", new SimpleAnnotationValue(nerType), nerStart, nerEnd);
       					nerStart = -1;
       					nerEnd = -1;
       				}
        		   
       				if (!n.equals("O")) {
       					if (nerStart == -1) {
       						nerStart = s;
       					}
       					nerEnd = e;
       					nerType = n;
       				}
       			} else {
       				if (nerStart == -1) {
   						nerStart = s;
   					}
   					nerEnd = e;
   					nerType = n;
       			}
       		}
           
       		if (nerStart != -1) {
       			ta.add("NE", new SimpleAnnotationValue(nerType), nerStart, nerEnd);
       		}
           
       		ta.add("SENTENCE", null, ss, se);
       	}
       
       

		Comparator<Span> ssc = new Span.StartSpanComparator();
		Comparator<Span> esc = new Span.EndSpanComparator();
		
		ArrayList<Span> sortedWordSpans = ta.getLocations("TOKEN", null);
			
		for (Span sentenceSpan : ta.getLocations("SENTENCE", null)) {
			ArrayList<String> tokens = new ArrayList<>();
			ArrayList<Span> annTokens = new ArrayList<>();
				
			int pos1 = Collections.binarySearch(sortedWordSpans, sentenceSpan, ssc);
			if (pos1 < 0) {
				pos1 = -pos1 - 1;
			}
			int pos2 = Collections.binarySearch(sortedWordSpans, sentenceSpan, esc);
			if (pos2 < 0) {
				pos2 = -pos2 - 1;
			}
				
			ArrayList<Span> spanList = new ArrayList<>();
			
			for (int i = pos1; i <= pos2; i++) {
				Span span = sortedWordSpans.get(i);
				spanList.add(span);
				AnnotatedObject anns = ta.getAnnotations(span); 
				Set<AnnotationValue> val = anns.get("TOKEN");
				if (val != null) {
					String word = text.substring(span.start, span.end);
			
					tokens.add(word);
					annTokens.add(span);
				}
			}
		}
		
       return ta;
	}

	private class Struct implements Comparable<Struct> {
		public int id;
		public String lemma;
		public String surface;
		
		public Struct(int id, String lemma, String surface) {
			this.id = id;
			this.lemma = lemma;
			this.surface = surface;
		}

		@Override
		public int compareTo(Struct o) {
			if (id < o.id) {
				return -1;
			} else if (id > o.id) {
				return 1;
			} else {
				return 0;
			}
		}
		
		public String toString() {
			return surface; 
		}
		
	}
	

}
