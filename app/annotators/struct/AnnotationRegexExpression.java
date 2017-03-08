package annotators.struct;

import java.util.ArrayList;

public interface AnnotationRegexExpression {

	public boolean satisfies(AnnotatedObject ano);
	
	public boolean satisfies(AnnotatedObject ano, ArrayList<AnnotationRegexResult> result);
	
	public boolean satisfies2(AnnotatedObject ano, AnnotationResult result);
	
	public ArrayList<String> getReturnElements();
	
	public boolean isStart();
	
	public boolean isEnd();


}
