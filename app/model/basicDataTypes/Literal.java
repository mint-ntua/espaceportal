package model.basicDataTypes;

import java.util.HashMap;

import utils.Deserializer.LiteralDesiarilizer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = LiteralDesiarilizer.class)
public class Literal extends HashMap<String, String> implements ILiteral {

	public Literal() {
	}

	public Literal(String label) {
		this.put(Language.UNKNOWN.getDefaultCode(), label);
	}

	public Literal(Language lang, String label) {
		this.put(lang.getDefaultCode(), label);
	}

	/* (non-Javadoc)
	 * @see model.basicDataTypes.ILiteral#addLiteral(model.basicDataTypes.Language, java.lang.String)
	 */
	@Override
	public void addLiteral(Language lang, String value) {
		this.put(lang.getDefaultCode(), value);
	}

	/* (non-Javadoc)
	 * @see model.basicDataTypes.ILiteral#addLiteral(java.lang.String)
	 */
	@Override
	public void addLiteral(String value) {
		addLiteral(Language.UNKNOWN, value);
	}

	/**
	 * Don't request the "unknown" language, request "any" if you don't care
	 * 
	 * @param lang
	 * @return
	 */
	public String getLiteral(Language lang) {
		return get(lang.getDefaultCode());
	}

	public Literal fillDEF() {
		if (containsKey(Language.DEFAULT.getDefaultCode())) {
			return this;
		}
		if (containsKey(Language.EN.getDefaultCode())) {
			put(Language.DEFAULT.getDefaultCode(), getLiteral(Language.EN));
			return this;
		}
		for (String lang : this.keySet()) {
			if (Language.isLanguage(lang)) {
				put(Language.DEFAULT.getDefaultCode(), get(lang));
				return this;
			}
		}
		return this;
	}
}
