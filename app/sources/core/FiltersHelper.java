package sources.core;

import java.util.Collection;

public class FiltersHelper {

	public static Collection<CommonFilterLogic> merge(Collection<CommonFilterLogic> mergedfilters,
			Collection<CommonFilterLogic> newfilters) {
		if (newfilters != null) {
			for (CommonFilterLogic commonFilterResponse : newfilters) {
				boolean merged = false;
				for (CommonFilterLogic old : mergedfilters) {
					if (old.data.filterID.equals(commonFilterResponse.data.filterID)) {
						merged = true;
						// Do the merge
						merge(old, commonFilterResponse);
						break;
					}
				}
				if (!merged) {
					CommonFilterLogic clone = commonFilterResponse.clone();
					mergedfilters.add(clone);
				}
			}
		}
		return mergedfilters;
	}
	
	public static Collection<CommonFilterLogic> mergeAux(Collection<CommonFilterLogic> mergedfilters,
			Collection<CommonFilterResponse> newfilters) {
		if (newfilters != null) {
			for (CommonFilterResponse commonFilterResponse : newfilters) {
				boolean merged = false;
				for (CommonFilterLogic old : mergedfilters) {
					if (old.data.filterID.equals(commonFilterResponse.filterID)) {
						merged = true;
						// Do the merge
						merge(old, commonFilterResponse);
						break;
					}
				}
				if (!merged) {
					CommonFilterLogic clone = new CommonFilterLogic(commonFilterResponse);
					mergedfilters.add(clone);
				}
			}
		}
		return mergedfilters;
	}

	public static CommonFilterLogic merge(CommonFilterLogic a, CommonFilterLogic b) {
		a.addValueCounts(b.values());
		a.addSourceFrom(b);
		return a;
	}

	public static CommonFilterLogic merge(CommonFilterLogic a, CommonFilterResponse b) {
		a.addValueCounts(b.suggestedValues);
		a.addSourceFrom(b);
		return a;
	}
}
