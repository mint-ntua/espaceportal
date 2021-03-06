package controllers.thesaurus.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.atlas.lib.SetUtils;
import org.bson.types.ObjectId;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.basicDataTypes.Language;
import model.basicDataTypes.Literal;
import model.resources.ThesaurusObject.SKOSSemantic;

public class DAGNode<T> implements Comparable<DAGNode<T>> {

	private Set<T> label;
	private Set<String> instances;
	private int size;
	
	public boolean explicit;
	
	private Collection<DAGNode<T>> children;
	private Collection<DAGNode<T>> parents;

	public DAGNode() {
		label = new HashSet<>();
		
//		children = new ArrayList<>();
		children = new TreeSet<>();
		parents = new HashSet<>();
	}
	
	public DAGNode(T l) {
		label = new HashSet<>();
		label.add(l);
		
//		children = new ArrayList<>();
		children = new TreeSet<>();
		parents = new HashSet<>();
	}

	public DAGNode(T l, int size) {
		label = new HashSet<>();
		label.add(l);
		
//		children = new ArrayList<>();
		children = new TreeSet<>();
		parents = new HashSet<>();
		
		this.size = size; 
	}

	
	public Set<T> getLabel() {
		return label;
	}
	
	public Collection<DAGNode<T>> getChildren() {
		return children;
	}
	
	public Collection<DAGNode<T>> getParents() {
		return parents;
	}

	public void addChild(DAGNode<T> child) {
//		if (!children.contains(child)) {
			children.add(child);
			child.parents.add(this);
//		}
	}
	
	public void removeChild(DAGNode<T> child) {
//		if (!children.contains(child)) {
			children.remove(child);
			child.parents.remove(this);
//		}
	}


	public Set<String> getInstances() {
		return instances;
	}
	
	public int size() {
		return size;
	}


	private DAGNode<T> find(Set<String> expr, Set<DAGNode<T>> visited) {
		if (visited.add(this)) {
			if (label.containsAll(expr)) {
				return this;
			}

			
			for (DAGNode<T> node : children) {
				DAGNode<T> dg = node.find(expr, visited);
				if (dg != null) {
					return dg;
				}
			} 
		} 
		
		return null;
		
	}
	
//	
//	/// NEW CODE ------------------------------------------------------------------
//	public ArrayList<DAGNode> getDescendents(boolean include) {
//		Set<DAGNode> visited = new HashSet<>();
//		
//		ArrayList<DAGNode> res = new ArrayList<>();
//		res.add(this);
//		
//		int i = 0;
//		while (i < res.size()) {
//			DAGNode node = res.get(i++);
//			
//			for (DAGNode child : node.children) {
//				if (visited.add(child)) {
//					res.add(child);
//				}
//			}
//		}
//		
//		if (!include) {
//			res.remove(0);
//		}
//		
//		return res;
//		
//	}
//	
//	public ArrayList<DAGNode> getAncestors(boolean include) {
//		Set<DAGNode> visited = new HashSet<>();
//		
//		ArrayList<DAGNode> res = new ArrayList<>();
//		res.add(this);
//		
//		int i = 0;
//		while (i < res.size()) {
//			DAGNode node = res.get(i++);
//			
//			for (DAGNode parent : node.parents) {
//				if (visited.add(parent)) {
//					res.add(parent);
//				}
//			}
//		}
//		
//		if (!include) {
//			res.remove(0);
//		}
//		
//		return res;
//		
//	}
//	

	public String toString() {
		return (label !=null?label.toString():"NULL") + "  :  " + size;
	}

	public ObjectNode toJSON(Map<String, ObjectId> idMap, Map<String, SKOSSemantic> map, Language lang) {
		return itoJSON(idMap, map, lang, new HashSet<>());
	}

	
	private ObjectNode itoJSON(Map<String, ObjectId> idMap, Map<String, SKOSSemantic> map, Language lang, Set<DAGNode<T>> used) {
		if (!used.add(this)) {
			return Json.newObject();
		}

		T s = label.iterator().next();

		ObjectId id = idMap.get(s);
		
		ObjectNode element = Json.newObject();
		
		if (id != null) {
			Literal plabel = map.get(s).getPrefLabel();
			String ss = "";
			if (plabel != null) {
				ss = plabel.getLiteral(lang);
				if (ss == null && plabel.values().size() > 0) {
					ss = plabel.values().iterator().next();
				}
			} else {
				String sv = s.toString();
				
				ss = sv.substring(Math.max(sv.lastIndexOf("/"), sv.lastIndexOf("#")));
			}
			
//			String ss = map.get(s).getPrefLabel().getLiteral(lang);
			
			element.put("id", idMap.get(s).toString());
			element.put("uri", s.toString());
			element.put("label", ss);
			element.put("size", size);
			
			ArrayNode jchildren = Json.newObject().arrayNode();
			
			element.put("children", jchildren);
			
			for (DAGNode<T> node : children) {
				jchildren.add(node.itoJSON(idMap, map, lang, used));
			}
		}
		
		return element;
	}


	public void print(Map<String, SKOSSemantic> map) {
		print(map, 0);
	}
	
	public void print(Map<String, SKOSSemantic> map, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("   ");
		}
		
//		System.out.println(toString());
		String ss = "";
		for (T s : label) {
			ss += (explicit? "*":" ") + map.get(s).getPrefLabel().getLiteral(Language.EN) + " " + size;// + " " + map.get(s).getUri();
		}
		System.out.println(ss);
		
		for (DAGNode<T> node : children) {
			node.print(map, depth + 1);
		}
	}
	public int hashCode() {
		return label.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof DAGNode) {
			return label.equals(((DAGNode)obj).label);
		}
		
		return false;
	}
	
//	private static double THRESHOLD = 0.05;
	
//	public boolean childRemoved = false;
	
	public void normalize(Set<T> selected) {
		normalize(selected, new HashSet<>());
	}
	
	public void normalize(Set<T> selected, Set<DAGNode<T>> used) {
		if (!used.add(this)) {
			return;
		}
		
//		int totalch = 0;
//		Collection<DAGNode<T>> toAdd = new HashSet<>();
		
//		System.out.println(this.getLabel());
//		for (Iterator<DAGNode<T>> iter =  children.iterator(); iter.hasNext();) {
//			System.out.println("\t" + iter.next().getLabel());
//		}

		for (Iterator<DAGNode<T>> iter =  children.iterator(); iter.hasNext();) {
			DAGNode<T> child = iter.next();
			child.normalize(selected, used);
		}
		
//		for (Iterator<DAGNode<T>> iter =  children.iterator(); iter.hasNext();) {
//			DAGNode<T> child = iter.next();
//			
//			if (child.childRemoved && child.children.size() == 0) {
////				iter.remove();
//				childRemoved = true;
//				continue;
//			}
//
//			if (SetUtils.intersection(child.label, selected).size() > 0) {
//				iter.remove();
//				childRemoved = true;
//				
//				toAdd.addAll(child.children);
//			}			

			
//			if (child.size() < size*THRESHOLD) {
//				iter.remove();
//			}
//			totalch += child.size();
//		}
		
//		for (DAGNode<T> cc : toAdd) {
//			children.add(cc);
//		}
		
		Set<DAGNode<T>> toAdd = new HashSet<>();
//		for (Iterator<DAGNode<T>> iter = children.iterator(); iter.hasNext();) {
//			DAGNode<T> child = iter.next();
//			int psize = child.size();
//			if (child.children.size() > 0) {
//				int csize = 0;
//				for (DAGNode<T> ch : child.children) {
//					csize += ch.size;
//				}
//				
//				if (csize == psize) {
//					iter.remove();
//					toAdd.addAll(child.children);
//				}
//			}
//		}
//
//		for (DAGNode<T> ch : toAdd) {
//			addChild(ch);
//		}

//		if (children.size() == 1) {
//			DAGNode<T> child = children.iterator().next();
//			if (size == child.size) {
//				children.clear();
//				children.addAll(child.children);
//				label.clear();
//				label.addAll(child.getLabel());
//			}
//		}
		
		toAdd.clear();
//		Set<DAGNode<T>> toAdd = new HashSet<>();
		for (Iterator<DAGNode<T>> iter = children.iterator(); iter.hasNext();) {
			DAGNode<T> child = iter.next();
			
			if (child.size() == size) {
				iter.remove();
				toAdd.addAll(child.children);
				label.clear();
				label.addAll(child.getLabel());
			}
		}
				
		for (DAGNode<T> ch : toAdd) {
			addChild(ch);
		}
	}

	@Override
	public int compareTo(DAGNode<T> o) {
		if (size < o.size) {
			return 1;
		} else if (size > o.size) {
			return -1;
		} else {
			return label.toString().compareTo(o.label.toString());
		}
	}
	

	
}
