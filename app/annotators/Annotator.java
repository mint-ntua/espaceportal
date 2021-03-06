package annotators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import model.annotations.Annotation;
import model.annotations.targets.AnnotationTarget;
import model.basicDataTypes.Language;
import model.resources.RecordResource;

public abstract class Annotator {

	protected Language lang;
	
	public static String LANGUAGE = "lang";
	
	public abstract String getName();
	
	public abstract String getService();
	
	public abstract List<Annotation> annotate(String text, AnnotationTarget target, Map<String, Object> properties) throws Exception;
	
	private Pattern p = Pattern.compile("(<.*?>)");
	
	protected String strip(String text) {
		Matcher m = p.matcher(text);
		
		StringBuffer sb = new StringBuffer();
		
		int prev = -1;
		while (m.find()) {
			int s = m.start(1);
			int e = m.end(1);
			
			if (prev == -1) {
				sb.append(text.substring(0,s));
			} else {
				sb.append(text.substring(prev, s));
			}
			
			char[] c = new char[e - s];
			Arrays.fill(c, ' ');
			
			sb.append(c);
			
			prev = e;
		}
		
		if (prev == -1) {
			return text;
		} else {
			sb.append(text.substring(prev));
		
			return sb.toString();
		}
	}
	
	public static List<Annotator> getAnnotators(Language lang) {
		List<Annotator> res = new ArrayList<>();
		
		Annotator ann;
		ann = DBPediaAnnotator.getAnnotator(lang);
		if (ann != null) {
			res.add(ann);
		}
		
		ann = DictionaryAnnotator.getAnnotator(lang, true);
		if (ann != null) {
			res.add(ann);
		}

		ann = NERAnnotator.getAnnotator(lang);
		if (ann != null) {
			res.add(ann);
		}
		
		return res;
	}
	
	public static Annotator getAnnotator(Class<? extends Annotator> clazz, Language lang) {

		if (clazz.equals(DBPediaAnnotator.class)) {
			return DBPediaAnnotator.getAnnotator(lang);
		}
		
		if (clazz.equals(DictionaryAnnotator.class)) {
			return DictionaryAnnotator.getAnnotator(lang, true);
		}

		if (clazz.equals(NERAnnotator.class)) {
			return NERAnnotator.getAnnotator(lang);
		}
		
		return null;
	}
	

	
}
