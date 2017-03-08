package sources.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import model.resources.CulturalObject;
import play.libs.Json;
import sources.formatreaders.CulturalRecordFormatter;
import sources.formatreaders.EuropeanaItemRecordFormatter;

public class SimpleTest {
	public static void main(String[] args) throws IOException {
		String text = FileUtils.readFileToString(new File("record.json"));
		CulturalRecordFormatter rec = new EuropeanaItemRecordFormatter();
		CulturalObject obj = rec.readObjectFrom(new JsonContextRecord(text));
		System.out.println(Json.toJson(obj));
	}
}
