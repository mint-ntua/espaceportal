
package sources;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import sources.core.ApacheHttpConnector;
import sources.core.CommonFilter;
import sources.core.CommonQuery;
import sources.core.HttpConnector;
import sources.core.SourceResponse;
import sources.core.Utils.Pair;
import sources.utils.FunctionsUtils;

public class EuropeanaCollectionSpaceSource extends EuropeanaSpaceSource {

	private String collectionName;
	private String nextCursor;

	public EuropeanaCollectionSpaceSource(String collectionName) {
		super();
		setProfile("rich");
		setUsingCursor(true);
		addDefaultWriter("europeana_collectionName", qfwriter("europeana_collectionName"));
		this.collectionName = collectionName;
	}

	@Override
	public HttpConnector getHttpConnector() {
		return ApacheHttpConnector.getApacheHttpConnector();
	}

	private Function<List<String>, Pair<String>> qfwriter(String parameter) {
//		Function<String, String> function = (String s) -> {
//			return "\"" + s + "\"";
//		};
//		return new Function<List<String>, Pair<String>>() {
//			@Override
//			public Pair<String> apply(List<String> t) {
//				return new Pair<String>("qf", parameter + ":" + function.apply(t.get(0)));
//			}
//		};
		return FunctionsUtils.toORList("qf", 
				(s)-> parameter + ":" + FunctionsUtils.quote().apply(s)
				);
	}

	@Override
	public SourceResponse getResults(CommonQuery q) {
		q.filters = Arrays.asList(new CommonFilter("europeana_collectionName", getCollectionName()));
		return super.getResults(q);
	}


	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

}

