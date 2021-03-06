package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.basicDataTypes.Language;
import model.resources.ThesaurusObject;
import model.resources.ThesaurusObject.SKOSSemantic;
import model.resources.ThesaurusObject.SKOSTerm;
import db.DB;
import db.ThesaurusObjectDAO;

public class ThesaurusFacet {
	private static boolean precompute = false;
	private static Map<String, SKOSSemantic> map;
	
	private static ThesaurusObjectDAO dao;
	
	static {
		dao = DB.getThesaurusDAO();
		map = new HashMap<>();
		
		if (precompute) {
			System.out.println("READING THESAURUSES");
			
			long start = System.currentTimeMillis();
	
			for (ThesaurusObject to : dao.getAll()) {
				map.put(to.getSemantic().getUri(), to.getSemantic()); 
			}
	
			System.out.println("INIT TIME: " + (System.currentTimeMillis() - start));
		}
	}
	
	public ThesaurusFacet() {
	}
	

	
	public SKOSSemantic getSemantic(String term) {
		SKOSSemantic res = map.get(term);
		if (res == null) {
			res = DB.getThesaurusDAO().getByUri(term).getSemantic();
			map.put(res.getUri(), res);
		}
		
		return res;
	}
	
	public static void count(Map<String, Counter> counterMap, String term, Set<String> used) {
		Counter c = counterMap.get(term);
		if (c == null) {
			c = new Counter(0);
			counterMap.put(term, c);
		}
		if (used.add(term)) {
			c.increase();
		}
	}
	
	private Set<DAGNode<String>> tops;
	
	public String toJSON(Language lang) {
		StringBuffer res = new StringBuffer("[ ");
		
		int i = 0;
		for (DAGNode<String> node : tops) {
			if (i++ > 0) {
				res.append(", ");
			}
			res.append(node.toJSON(map, lang));
		}
		
		res.append(" ]");
		
		return res.toString();
	}
	
	public void create(List<String[]> list) {
		long start = System.currentTimeMillis();
		
		Map<String, DAGNode<String>> nodeMap = new HashMap<>();
		Map<String, Counter> counterMap = new HashMap<>();
		
		Map<String, Counter> levelMap = new HashMap<>();
		
		tops = new HashSet<>();
		
//		DAGNode<String> flattop = new DAGNode<String>();
		
		for (String[] uris : list) {
			Set<String> used = new HashSet<>();
			Set<String> fused = new HashSet<>();
			for (String uri : uris) {
				SKOSSemantic semantic = getSemantic(uri);
				
				count(counterMap, uri, used);
				count(levelMap, uri, fused);
				
				List<SKOSTerm> broader = semantic.getBroaderTransitive();
				if (broader != null) {
					for (SKOSTerm term : broader) {
						count(counterMap, term.getUri(), used);
					}
				}
			}
		}
		
		
//		System.out.println("A " + (System.currentTimeMillis() - start));

		Set<String> used = new HashSet<>();
		
		for (Map.Entry<String, Counter> entry : counterMap.entrySet()) {
			String term = entry.getKey();
//			System.out.println("ADDING " + term);
			if (!used.add(term)) {
//				System.out.println("RETURN");
				continue;
			}
			
			DAGNode<String> node = nodeMap.get(term);
//			System.out.println("------ " + node);
			if (node == null) {
				node = new DAGNode<String>(term, entry.getValue().getValue());
				nodeMap.put(term, node);
				tops.add(node);
			}
			
//			node.explicit = levelMap.containsKey(term);
				
			List<SKOSTerm> broaderList = getSemantic(term).getBroader();
//			System.out.println("BROADERLIST " + broaderList);
			
			if (broaderList != null) {
				for (SKOSTerm broader : broaderList) {
					String broaderURI = broader.getUri();
					
					DAGNode<String> parent = nodeMap.get(broaderURI);
//					System.out.println("PARENT " + parent);
					if (parent == null) {
						parent = new DAGNode<String>(broaderURI, counterMap.get(broaderURI).getValue());
						nodeMap.put(broaderURI, parent);
						tops.add(parent);
					}
					
//					parent.explicit = levelMap.containsKey(broaderURI);
					
					parent.addChild(node);
					tops.remove(node);
				}
			}
		}
		
		System.out.println("FACET TIME " + (System.currentTimeMillis() - start));
		
		System.out.println(tops);
		for (DAGNode<String> top : tops) {
			System.out.println("NEXT TOP");
//			System.out.println("----");
//			top.print(map);
			top.normalize();
//			System.out.println("    ");
			top.print(map);
//			res.addChild(top);
		}
		
		
		
//		System.out.println("---------------");
////		System.out.println(levelMap);
//		
//		for (Map.Entry<String, Counter> entry : levelMap.entrySet()) {
//			flattop.addChild(new DAGNode<String>(entry.getKey(), entry.getValue().getValue()));
//		}
//		
//		flattop.print(map);

	}

}
