package model.basicDataTypes;

import java.util.List;

import org.mongodb.morphia.annotations.Converters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import utils.Deserializer.MultiLiteralOrResourceDesiarilizer;
import db.converters.MultiLiteralOrResourceConverter;
import sources.core.Utils;

@Converters(MultiLiteralOrResourceConverter.class)

@JsonDeserialize(using = MultiLiteralOrResourceDesiarilizer.class)
public class MultiLiteralOrResource extends MultiLiteral {

	public MultiLiteralOrResource() {
	}

	public MultiLiteralOrResource(Language lang, String label) {
		super(lang, label);
	}

	public MultiLiteralOrResource(String label) {
		if (Utils.isValidURL(label)) {
			addURI(label);
		} else
			addLiteral(label);
	}

	public void addLiteral(Language lang, String value) {
		if (Utils.isValidURL(value))
			addURI(value);
		else
			super.addLiteral(lang, value);
	}

	public void addURI(String uri) {
		add(LiteralOrResource.URI, uri);
	}

	public void addURI(List<String> uris) {
		for (String uri : uris) {
			addURI(uri);
		}
	}
	
	public MultiLiteralOrResource merge(MultiLiteralOrResource other) {
		for (String k : other.keySet()) {
			List<String> value = other.get(k);
			for (String string : value) {
				this.add(k, string);
			}
		}
		return this;
	}
	
	public MultiLiteralOrResource fillDEF() {
		return (MultiLiteralOrResource) fillDEF(false);
	}

}
