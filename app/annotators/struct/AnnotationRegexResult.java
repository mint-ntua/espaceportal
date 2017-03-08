package annotators.struct;

import java.util.ArrayList;
import java.util.Set;

public class AnnotationRegexResult {
	
	public AnnotatedObject ano;
	public ArrayList<String> cats;
	
	public AnnotationRegexResult(AnnotatedObject ano, ArrayList<String> cats) {
		this.ano = ano;
		this.cats = cats;
	}
	
	public Object[] getResult(AnnotationIndex ai) {
		Span span = ano.getSpan();
		if (cats == null) {
			return new Object[] {ai.getText().substring(span.start, span.end)};
		} else {
			Set<AnnotationValue>[] ret = new Set[cats.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = ano.get(cats.get(i));
			}
			return ret;
		}
	}
	
	public String toString() {
		return (ano != null ? ano.toString() : "NULL" ) + " | " + cats.toString();
	}
}
