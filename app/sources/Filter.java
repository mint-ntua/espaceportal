package sources;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Filter {

	private String id;
	private String type;
	private String value;
	private String term;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The id
	 */
	public String getId() {
		return id;
	}

	/**
	 *
	 * @param id
	 *            The id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 *
	 * @return The type
	 */
	public String getType() {
		return type;
	}

	/**
	 *
	 * @param type
	 *            The type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 *
	 * @return The value
	 */
	public String getValue() {
		return value;
	}

	/**
	 *
	 * @param value
	 *            The value
	 */
	public void setValue(String value) {
		this.value = value;
	}

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

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}