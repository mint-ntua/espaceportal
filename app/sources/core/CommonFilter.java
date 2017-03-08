package sources.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;

public class CommonFilter implements Cloneable {
	public static final ALogger log = Logger.of( CommonFilter.class);
	
	public String filterID;
	public List<String> values;
	
	public CommonFilter() {
		super();
	}

	public CommonFilter(String filterID, String... values) {
		super();
		this.filterID = filterID;
		this.values = Arrays.asList(values);
	}

	@Override
	public String toString() {
		return "CommonFilter [filterID=" + filterID + ", values=" + values + "]";
	}

	public List<CommonFilter> splitValues(ISpaceSource src) {
		ArrayList<CommonFilter> res = new ArrayList<>();

		for (String v : values) {
			for (Object vv : src.translateToSpecific(filterID, v)) {

				try {
					CommonFilter c = (CommonFilter) super.clone();
					c.values = Arrays.asList(vv.toString());
					res.add(c);
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					log.error("",e);
				}
			}
		}
		return res;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
