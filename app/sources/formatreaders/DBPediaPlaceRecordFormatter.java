package sources.formatreaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import model.EmbeddedMediaObject;
import model.EmbeddedMediaObject.MediaVersion;
import model.EmbeddedMediaObject.WithMediaRights;
import model.basicDataTypes.Language;
import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.MultiLiteralOrResource;
import model.basicDataTypes.ProvenanceInfo;
import model.basicDataTypes.ProvenanceInfo.Sources;
import model.basicDataTypes.WithDate;
import model.resources.PlaceObject;
import model.resources.PlaceObject.PlaceData;
import model.resources.CulturalObject;
import model.resources.CulturalObject.CulturalObjectData;
import model.resources.RecordResource;
import play.Logger;
import sources.FilterValuesMap;
import sources.core.Utils;
import sources.utils.JsonContextRecord;
import sources.utils.StringUtils;

public class DBPediaPlaceRecordFormatter extends PlaceRecordFormatter {

	public DBPediaPlaceRecordFormatter() {
		super(FilterValuesMap.getMap(Sources.DBPedia));
		object = new PlaceObject();
	}

	@Override
	public PlaceObject fillObjectFrom(JsonContextRecord rec) {
//		Language[] language = new Language[] {Language.EN };
//		
//		language = getLanguagesFromText(rec.getStringValue("title"), 
//				rec.getStringValue("longTitle"));
//		rec.setLanguages(language);

		PlaceData model = (PlaceData) object.getDescriptiveData();
		
//		model.setDclanguage(StringUtils.getLiteralLanguages(language));
		
		model.setLabel(rec.getMultiLiteralValue("label"));
		model.setDescription(rec.getMultiLiteralValue("abstract"));
//		model.setIsShownBy(rec.getLiteralOrResourceValue("edmIsShownBy"));
//		model.setIsShownAt(rec.getLiteralOrResourceValue("edmIsShownAt"));
		// model.setYear(Integer.parseInt(rec.getStringValue("year")));
//		model.setDccreator(rec.getMultiLiteralOrResourceValue("principalOrFirstMaker"));
		
		String id = rec.getStringValue("uri");
		object.addToProvenance(new ProvenanceInfo(Sources.DBPedia.toString(), id , id));

		List<String> subject =  rec.getStringArrayValue("subject");
		if (subject != null && subject.size() > 0) {
			MultiLiteralOrResource ct = new MultiLiteralOrResource();
			ct.addURI(subject);
			model.setKeywords(ct);
		}
		
		String uri3 = rec.getStringValue("thumbnail");
		if (Utils.hasInfo(uri3)){
			EmbeddedMediaObject med = new EmbeddedMediaObject();
			med.setUrl(uri3);
//			medThumb.setWidth(rec.getIntValue("headerImage.width"));
//			medThumb.setHeight(rec.getIntValue("headerImage.height"));
//			medThumb.setType(type);
//			if (Utils.hasInfo(rights))
			med.setOriginalRights(new LiteralOrResource("http://creativecommons.org/publicdomain/zero/1.0/deed.en"));
			med.setWithRights(WithMediaRights.Public);
			
			object.addMedia(MediaVersion.Thumbnail, med);
		}

		String uri2 = rec.getStringValue("depiction");
		if (Utils.hasInfo(uri2)){
			EmbeddedMediaObject med = new EmbeddedMediaObject();
			med.setUrl(uri2);
//			medThumb.setWidth(rec.getIntValue("headerImage.width"));
//			medThumb.setHeight(rec.getIntValue("headerImage.height"));
//			medThumb.setType(type);
//			if (Utils.hasInfo(rights))
			med.setOriginalRights(new LiteralOrResource("http://creativecommons.org/publicdomain/zero/1.0/deed.en"));
			med.setWithRights(WithMediaRights.Public);
			
			object.addMedia(MediaVersion.Original, med);
		}
		
		List<String> country =  rec.getStringArrayValue("country");
		if (country != null && country.size() > 0) {
			MultiLiteralOrResource ct = new MultiLiteralOrResource();
			ct.addURI(country);
			model.setNation(ct);
		}
		
		return object;
	}

}
