package sources.core;

import java.util.ArrayList;
import java.util.List;

import sources.core.Utils.Pair;

public class EuropeanaQuery {

	private String europeanaKey = "ANnuDzRpW";

	private List<String> search;
	private List<Pair<String>> parameters;

	public EuropeanaQuery() {
		super();
		search = new ArrayList<String>();
		parameters = new ArrayList<Utils.Pair<String>>();
	}

	public String getHttp() {
		List<Pair<String>> p = new ArrayList<Utils.Pair<String>>();
		for (Pair<String> pair : parameters) {
			p.add(pair);
		}
		if (search != null && search.size() > 0) {
			p.add(new Pair<String>("query", search.get(0)));
			for (int i = 1; i < search.size(); i++) {
				p.add(new Pair<String>("qf", search.get(i)));
			}
		}
		String res = getQueryBody();
		for (Pair<String> pair : p) {
			res += "&" + pair.getHttp();
		}
		return res;
	}

	private String getQueryBody() {
		return "http://www.europeana.eu/api/v2/search.json?wskey=" + europeanaKey;
	}

	public void addSearch(String q) {
		if (q != null)
			search.add(q);
	}

	public void addSearchParam(String name, String value) {
		parameters.add(new Pair<String>(name, value));
	}

	public void addSearch(Pair<String> attr) {
		if (attr != null) {
			search.add(attr.first + ":" + attr.second);
		}
	}

}
