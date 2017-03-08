package model.basicDataTypes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import sources.core.Utils;
import utils.Deserializer.LiteralOrResourceDesiarilizer;

@JsonDeserialize(using = LiteralOrResourceDesiarilizer.class)
public class LiteralOrResource extends Literal {

	public static final String URI = "uri";

	public LiteralOrResource() {
		super();
	}

	public LiteralOrResource(Language key, String value) {
		super(key, value);
	}

	public LiteralOrResource(String label) {
		if (Utils.isValidURL(label)) {
			addURI(label);
		} else {
			addSmartLiteral(label);
		}
	}
	
	@Override
	public LiteralOrResource fillDEF() {
		return (LiteralOrResource)super.fillDEF();
	}

	public void addLiteral(Language lang, String value) {
		if (Utils.isValidURL(value)) {
			addURI(value);
		} else {
			super.addLiteral(lang, value);
		}
	}

	public void addURI(String uri) {
		put(URI, uri);
	}

	public String getURI() {
		return get(URI);
	}

}
