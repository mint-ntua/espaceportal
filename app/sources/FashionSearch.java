package sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

public class FashionSearch {

	private String term;
	private List<Filter> filters = new ArrayList<Filter>();
	private Integer count;
	private Integer offset;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 *
	 * @param term
	 *            The term
	 */
	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 *
	 * @return The filters
	 */
	public List<Filter> getFilters() {
		return filters;
	}

	/**
	 *
	 * @param filters
	 *            The filters
	 */
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	/**
	 *
	 * @return The count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 *
	 * @param count
	 *            The count
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 *
	 * @return The offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 *
	 * @param offset
	 *            The offset
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}