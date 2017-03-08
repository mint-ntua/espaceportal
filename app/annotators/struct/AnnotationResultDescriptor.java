package annotators.struct;

import java.util.ArrayList;

public class AnnotationResultDescriptor {
	
	private String name;
	private ArrayList<String> classes;
	private boolean span;
	private boolean id;
	
	public AnnotationResultDescriptor(String name, ArrayList<String> classes, boolean id, boolean span) {
		this.name = name;
		this.classes = classes;
		this.id = id;
		this.span = span;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getClasses() {
		return classes;
	}
	
	public boolean getSpan() {
		return span;
	}

	public boolean getID() {
		return id;
	}

	public String toString() {
		return "<" + name +  ":" + classes + ":" + span + ">";
	}
	
}
