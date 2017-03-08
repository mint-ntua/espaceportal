package sources.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import model.basicDataTypes.Language;
import model.basicDataTypes.Literal;
import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.MultiLiteral;
import model.basicDataTypes.MultiLiteralOrResource;
import model.basicDataTypes.WithDate;
import play.Logger;
import play.Logger.ALogger;
import sources.core.Utils;

public class JsonNodeUtils {

	public static final ALogger log = Logger.of( JsonNodeUtils.class );
	
	public static String asString(JsonNode node) {
		if (node != null && !node.isMissingNode()) {
			if (node.isArray()) {
				JsonNode jsonNode = node.get(0);
				if (jsonNode != null)
					return jsonNode.asText();
			} else if (node.isTextual()) {
				return node.asText();
			} else {
				return asStringArray(node).get(0);
			}
		}
		return null;
	}

	public static MultiLiteral readMultiLiteral(MultiLiteral res, JsonNode node, Language... suggestedLanguages) {
		if (node != null && !node.isMissingNode()) {
			if (node.isTextual()) {
				res.addSmartLiteral(node.asText(), suggestedLanguages);
				return res.fillDEF();
			}
			if (node.isArray()) {
				for (int i = 0; i < node.size(); i++) {
					readMultiLiteral(res, node.get(i), suggestedLanguages);
				}
				return res.fillDEF(true);
			}
			for (Iterator<Entry<String, JsonNode>> iterator = node.fields(); iterator.hasNext();) {
				Entry<String, JsonNode> next = iterator.next();
				Language language = Language.getLanguage(next.getKey());
				JsonNode value = next.getValue();
				if (language != null) {
					for (int i = 0; i < value.size(); i++) {
						String asText = value.get(i).asText();
						if (Utils.hasInfo(asText))
							res.addLiteral(language, asText);
					}
					if (Language.DEFAULT.equals(language)){
						for (int i = 0; i < value.size(); i++) {
							String asText = value.get(i).asText();
							if (Utils.hasInfo(asText))
								res.addSmartLiteral(asText);
						}
					}
				} else {
					List<String> asString = asStringArray(value);
					for (int i = 0; i < asString.size(); i++) {
						res.addSmartLiteral(asString.get(i), suggestedLanguages);
					}
					if (!next.getKey().equals("@resource"))
						log.info("Unknown Format!!! " + next.toString());
				}
			}
			return res.fillDEF();
		}
		return null;
	}

	public static MultiLiteral asMultiLiteral(JsonNode node, Language... suggestedLanguages) {
		return readMultiLiteral(new MultiLiteral(), node, suggestedLanguages);
	}

	public static Literal readLiteral(Literal res, JsonNode node, Language... suggestedLanguages) {
		if (node != null && !node.isMissingNode()) {
			if (node.isTextual()) {
				res.addSmartLiteral(node.asText());
				return res.fillDEF();
			}
			if (node.isArray()) {
				res.addSmartLiteral(node.get(0).asText(), suggestedLanguages);
				return res.fillDEF();
			}
			for (Iterator<Entry<String, JsonNode>> iterator = node.fields(); iterator.hasNext();) {
				Entry<String, JsonNode> next = iterator.next();
				Language language = Language.getLanguage(next.getKey());
				JsonNode value = next.getValue();
				if (language != null){
					if (value.isArray())
					res.addLiteral(language, value.get(0).asText());
					else
						res.addLiteral(language, value.asText());
					
				}
				else
					res.addSmartLiteral(asString(value), suggestedLanguages);
			}
			return res.fillDEF();
		}
		return null;
	}

	public static Literal asLiteral(JsonNode node, Language... suggestedLanguages) {
		return readLiteral(new Literal(), node);
	}

	public static LiteralOrResource asLiteralOrResource(JsonNode node, Language... suggestedLanguages) {
		return (LiteralOrResource) readLiteral(new LiteralOrResource(), node, suggestedLanguages);
	}

	public static MultiLiteralOrResource asMultiLiteralOrResource(JsonNode node, Language... suggestedLanguages) {
		return (MultiLiteralOrResource) readMultiLiteral(new MultiLiteralOrResource(), node, suggestedLanguages);
	}

	public static List<WithDate> asWithDateArray(List<String> list) {
		ArrayList<WithDate> res = new ArrayList<>();
		if (Utils.hasInfo(list)) {
			for (String string : list) {
				if (Utils.hasInfo(string)){
					res.add(new WithDate(string));
				}
			}
		}
		return res;
	}

	public static List<String> asStringArray(Collection<JsonNode> node) {
		List<String> res = new ArrayList<>();
		for (JsonNode n : node) {
			List<String> a = asStringArray(n);
			if (Utils.hasInfo(a))
				res.addAll(a);
		}
		return res;
	}

	public static List<String> asStringArray(JsonNode node) {
		if (node != null && !node.isMissingNode()) {
			ArrayList<String> res = new ArrayList<>();
			if (node.isArray()) {
				for (int i = 0; i < node.size(); i++) {
					res.add(node.get(i).asText());
				}
			} else if (node.isTextual()) {
				res.add(node.asText());
			} else {
				boolean flag = false;
				for (Iterator<Entry<String, JsonNode>> iterator = node.fields(); iterator.hasNext();) {
					Entry<String, JsonNode> next = iterator.next();
					JsonNode value = next.getValue();
					res.addAll(asStringArray(value));
					flag = true;
				}
				if (!flag)
					res.add(node.asText());
			}
			return res;
		}
		return null;
	}

}
