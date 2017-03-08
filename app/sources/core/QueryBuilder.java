package sources.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sources.core.Utils.LongPair;
import sources.core.Utils.Pair;
public class QueryBuilder {

	protected String baseUrl;
	protected String tail;
	protected Pair<String> query;
	protected List<Pair<String>> parameters;
	protected Object data;

	
	public QueryBuilder(String baseUrl) {
		this();
		this.baseUrl = baseUrl;
	}

	public QueryBuilder() {
		super();
		parameters = new ArrayList<Utils.Pair<String>>();
	}

	public String getHttp(boolean encode) {
		String res = getBaseUrl();
		Iterator<Pair<String>> it = parameters.iterator();
		boolean added = false;
		if (query != null && query.second != null) {
			res += ("?" + query.getHttp(encode));
			added = true;
		}
		for (; it.hasNext();) {
			String string = added ? "&" : "?";
			res += string + it.next().getHttp(encode);
			added = true;
		}
		if (Utils.hasInfo(tail)){
			res+=tail;
		}
		return res;
	}
	
	public String getHttp() {
		return getHttp(true);
	}

	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public QueryBuilder addSearchParam(String name, String value) {
		parameters.add(new Pair<String>(name, value));
		return this;
	}
	
	public QueryBuilder addLongSearchParam(String name, String value) {
		parameters.add(new LongPair<String>(name, value));
		return this;
	}
	
	public QueryBuilder add(Pair<String> searchParam) {
		parameters.add(searchParam);
		return this;
	}
	
	public QueryBuilder addQuery(Pair<String> q) {
		query = q;
		return this;
	}
	
	public QueryBuilder addQuery(String name, String value) {
		return addQuery(new Pair<String>(name, value));
	}
	
	public QueryBuilder addToQuery(String q) {
		query.second+=q;
		return this;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getTail() {
		return tail;
	}

	public void setTail(String tail) {
		this.tail = tail;
	}
	
	
	

}
