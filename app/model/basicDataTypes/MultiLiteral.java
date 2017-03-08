package model.basicDataTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.annotations.Converters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import db.converters.MultiLiteralConverter;
import utils.Deserializer.MultiLiteralDesiarilizer;

@Converters(MultiLiteralConverter.class)
@JsonDeserialize(using = MultiLiteralDesiarilizer.class)
public class MultiLiteral extends HashMap<String, List<String>> implements
		ILiteral {

	public MultiLiteral() {
	}

	public MultiLiteral(String label) {
		addLiteral(label);
	}

	public MultiLiteral(Language lang, String label) {
		addLiteral(lang, label);
	}

	@Override
	public void addLiteral(Language lang, String value) {
		add(lang.getDefaultCode(), value);
	}

	public void addMultiLiteral(Language lang, List<String> values) {
		for (String value : values) {
			addLiteral(lang, value);
		}
	}

	public List<String> get(Language lang) {
		/*
		 * if(Language.ANY.equals(lang)) { return
		 * this.get(this.keySet().toArray()[0]);
		 * 
		 * } else
		 */
		return get(lang.getDefaultCode());
	}

	public void add(String key, String value) {
		if (this.containsKey(key)){
			List<String> list = this.get(key);
			if (!list.contains(value))
				list.add(value);
		}
		else
			this.put(key, new ArrayList<String>(Arrays.asList(value)));
	}

	public MultiLiteral fillDEF() {
		return fillDEF(false);
	}

	public MultiLiteral fillDEF(boolean forced) {
		if (forced || !containsKey(Language.DEFAULT.getDefaultCode())) {
			remove(Language.DEFAULT.getDefaultCode());
			String defLang = null;
			if (containsKey(Language.EN.getDefaultCode())) {
				defLang = Language.EN.getDefaultCode();
			}
			if (!containsKey(defLang)) {
				int max = 0;
				for (String k : this.keySet()) {
					if (Language.isLanguage(k)
							&& !k.equals(Language.DEFAULT.getDefaultCode())) {
						int m = get(k).size();
						if (max < m) {
							max = m;
							defLang = k;
						}
					}
				}
			}

			if (defLang != null)
				for (String d : get(defLang)) {
					add(Language.DEFAULT.getDefaultCode(), d);
				}
		}
		return this;
	}
	
	public MultiLiteral merge(MultiLiteral other) {
		this.putAll(other);
		return this;
	}
	
	public Set<Language> getLanguages() {
		Set<Language> res = new HashSet<>();
		for (String s : keySet()) {
			res.add(Language.getLanguage(s));
		}
		
		return res;
	}
}
