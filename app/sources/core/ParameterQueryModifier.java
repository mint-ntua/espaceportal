package sources.core;

import java.util.List;

import sources.core.Utils.Pair;

public class ParameterQueryModifier extends QueryModifier {
	protected Pair<String>[] param;
	
	public ParameterQueryModifier(Pair<String>... param) {
		super();
		this.param = param;
	}
	
	public ParameterQueryModifier(List<Pair<String>> param) {
		super();
		this.param = param.toArray(new Pair[]{});
	}

	public QueryBuilder modify(QueryBuilder builder){
		for (Pair<String> pair : param) {
			builder.add(pair);
		}
		return builder;
	}
}