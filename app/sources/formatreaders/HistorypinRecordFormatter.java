package sources.formatreaders;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import jdk.nashorn.internal.runtime.regexp.RegExp;
import model.EmbeddedMediaObject;
import model.EmbeddedMediaObject.MediaVersion;
import model.EmbeddedMediaObject.WithMediaRights;
import model.EmbeddedMediaObject.WithMediaType;
import model.basicDataTypes.Language;
import model.basicDataTypes.LiteralOrResource;
import model.basicDataTypes.MultiLiteralOrResource;
import model.basicDataTypes.ProvenanceInfo;
import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.CulturalObject;
import model.resources.CulturalObject.CulturalObjectData;
import sources.FilterValuesMap;
import sources.core.CommonFilters;
import sources.core.Utils;
import sources.utils.JsonContextRecord;
import sources.utils.StringUtils;

public class HistorypinRecordFormatter extends CulturalRecordFormatter {

	public HistorypinRecordFormatter() {
		super(FilterValuesMap.getMap(Sources.Historypin));
		object = new CulturalObject();
	}

	@Override
	public CulturalObject fillObjectFrom(JsonContextRecord rec) {
		CulturalObjectData model = (CulturalObjectData) object.getDescriptiveData();
		

		Language[] language = null;
//		List<String> langs = rec.getStringArrayValue(false,"language","dcLanguage");
//		if (Utils.hasInfo(langs)){
//			language = new Language[langs.size()];
//			for (int i = 0; i < langs.size(); i++) {
//				language[i] = Language.getLanguage(langs.get(i));
//			}
////			System.out.println(Arrays.toString(language));
//		}
		if (!Utils.hasInfo(language)){
			language = getLanguagesFromText(rec.getStringValue("desc"),
											rec.getStringValue("caption"),
											rec.getStringValue("dcSubject"));
		}
		
//		model.setDctermsspatial(rec.getMultiLiteralOrResourceValue("dctermsSpatial"));
//		model.setCountry(rec.getMultiLiteralOrResourceValue("country"));;
//		model.setCoordinates(StringUtils.getPoint(rec.getStringValue("edmPlaceLatitude"),rec.getStringValue("edmPlaceLongitude")));
		model.setDclanguage(StringUtils.getLiteralLanguages(language));
		model.setLabel(rec.getMultiLiteralValue("caption"));
		model.setDescription(rec.getMultiLiteralValue("desc"));
//		model.setAltLabels(rec.getMultiLiteralValue("altLabel"));
		model.setIsShownBy(rec.getLiteralOrResourceValue("media_url"));
//		model.setIsShownAt(rec.getLiteralOrResourceValue("media_url"));
//		model.setDates(rec.getWithDateArrayValue("year"));
		model.setDccreator(rec.getMultiLiteralOrResourceValue("user.name"));
		String url = rec.getStringValue("user.url");
		if (Utils.hasInfo(url))
		model.getDccreator().addLiteral("http://www.historypin.org/en"+url);
		model.setDccontributor(rec.getMultiLiteralOrResourceValue("dcContributor"));
		model.setKeywords(rec.getMultiLiteralOrResourceValue("dcSubjectLangAware"));
//		object.addToProvenance(new ProvenanceInfo(rec.getStringValue("dataProvider"), model.getIsShownAt()==null?null:model.getIsShownAt().getURI(),null));
//		object.addToProvenance(new ProvenanceInfo(rec.getStringValue("provider")));
		String recID = rec.getStringValue("id");
		String uri = "http://www.historypin.org"+rec.getStringValue("url");
		object.addToProvenance(
				new ProvenanceInfo(Sources.Historypin.toString(), uri, recID));
//		List<String> rights = rec.getStringArrayValue("rights");
		List<Object> translateToCommon = getValuesMap().translateToCommon(CommonFilters.TYPE.getId(), rec.getStringValue("type"));
		WithMediaType type = (WithMediaType.getType(translateToCommon.get(0).toString())) ;
		WithMediaRights withRights = WithMediaRights.UNKNOWN;
		String uri3 = "http:"+rec.getStringValue("image");
		String uri2 = model.getIsShownBy()==null?null:model.getIsShownBy().getURI();
		if (Utils.hasInfo(uri3)){
			EmbeddedMediaObject medThumb = new EmbeddedMediaObject();
			uri3 = Utils.decodeURL(uri3);
			medThumb.setUrl(uri3);
			medThumb.setType(type);
//			if (Utils.hasInfo(rights))
//			medThumb.setOriginalRights(new LiteralOrResource(rights.get(0)));
			medThumb.setWithRights(withRights);
			object.addMedia(MediaVersion.Thumbnail, medThumb);
		}
		if (Utils.hasInfo(uri2)){
			EmbeddedMediaObject med = new EmbeddedMediaObject();
			med.setUrl(uri2);
//			if (Utils.hasInfo(rights))
//			med.setOriginalRights(new LiteralOrResource(rights.get(0)));
			med.setWithRights(withRights);
			med.setType(type);
			object.addMedia(MediaVersion.Original, med);
		}
		return object;
	}

}
