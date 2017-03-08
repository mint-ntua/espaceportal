package sources.core;

public class AdditionalQueryModifier extends QueryModifier {
	private String queryText;
	
	public AdditionalQueryModifier(String queryText) {
		super();
		this.queryText = queryText;
	}

	public QueryBuilder modify(QueryBuilder builder){
		return builder.addToQuery(queryText);
	}
}