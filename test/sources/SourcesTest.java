package sources;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import model.resources.CulturalObject;
import play.libs.Json;
import sources.formatreaders.CulturalRecordFormatter;
import sources.formatreaders.DDBItemRecordFormatter;
import sources.formatreaders.EuropeanaItemRecordFormatter;
import sources.formatreaders.RijksmuseumItemRecordFormatter;
import sources.utils.JsonContextRecord;

public class SourcesTest {

	@Test
	public void test() throws IOException {
		String text = FileUtils.readFileToString(new File("record.json"));
		CulturalRecordFormatter rec = new EuropeanaItemRecordFormatter();
		CulturalObject obj = rec.readObjectFrom(new JsonContextRecord(text));
		System.out.println(Json.toJson(obj));
	}

}
