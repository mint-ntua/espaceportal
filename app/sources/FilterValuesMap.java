package sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import model.EmbeddedMediaObject.WithMediaRights;
import model.EmbeddedMediaObject.WithMediaType;
import model.basicDataTypes.ProvenanceInfo.Sources;
import sources.core.CommonFilters;
import sources.core.ESpaceSources;
import sources.core.MapsConfig;
import sources.core.QueryModifier;
import utils.ListUtils;

public class FilterValuesMap {

	private static HashMap<Sources, FilterValuesMap> map;
	private HashMap<String, List<Object>> specificvalues;
	// private HashMap<String, List<Pair<String>>> queryTexts;
	private HashMap<String, List<Object>> commonvalues;
	private HashMap<String, Function<List<String>, QueryModifier>> writters;

	public FilterValuesMap() {
		map = new HashMap<>();
		specificvalues = new HashMap<>();
		commonvalues = new HashMap<>();
		// queryTexts = new HashMap<String, List<Pair<String>>>();
		writters = new HashMap<>();
	}

	private String getKey(String filterID, Object value) {
		return filterID + "-" + value.toString();
	}

	private <T> List<T> getOrset(HashMap<String, List<T>> map, String key, boolean addNew) {
		List<T> res;
		if (!map.containsKey(key)) {
			// check regular expr;
			res = new ArrayList<T>();
			for (String kk : map.keySet()) {
				if (key.matches(kk)) {
					res = map.get(kk);
					addNew = false; // for sure i am not adding a new value
				}
			}
			// not found
			if (addNew)
				map.put(key, res);
		} else {
			res = map.get(key);
		}
		return res;
	}

	private <T> List<T> getOrset(HashMap<String, List<T>> map, String key) {
		return getOrset(map, key, true);
	}

	public void addMap(String filterID, Object commonValue, String... specificValue) {
		getOrset(specificvalues, getKey(filterID, commonValue)).addAll(Arrays.asList(specificValue));
		for (String string : specificValue) {
			getOrset(commonvalues, getKey(filterID, string)).add(commonValue);
		}
		// getOrset(queryTexts, getKey(filterID, commonValue)).add(queryText);
	}
	
	public void addMap(String filterID, Object commonValue, List<String> specificValue) {
		getOrset(specificvalues, getKey(filterID, commonValue)).addAll(specificValue);
		for (String string : specificValue) {
			getOrset(commonvalues, getKey(filterID, string)).add(commonValue);
		}
		// getOrset(queryTexts, getKey(filterID, commonValue)).add(queryText);
	}

	public List<Object> translateToCommon(String filterID, String specificValue) {
		if (specificValue != null) {
			String matchexpr = getKey(filterID, specificValue);
			List<Object> v = new ArrayList<>();			
			for (String kk : commonvalues.keySet()) {
				if (matchexpr.matches(kk) || matchexpr.equals(kk)) {
					// String k = getKey(filterID, specificValue);
					List<Object> orset = getOrset(commonvalues, kk, false);
					v.addAll(orset);
					return v;
				}
			}
			if (v.isEmpty()) {
				v.add(specificValue);
			}
			return v;
		}
		return null;
	}

	public List<Object> translateToSpecific(String filterID, String... commonValue) {
		return translateToSpecific(filterID, Arrays.asList(commonValue));
	}

	public List<Object> translateToSpecific(String filterID, List<String> commonValue) {
		if (commonValue != null) {
			ArrayList<Object> res = new ArrayList<>();
			for (String string : commonValue) {
				String k = getKey(filterID, string);
				List<Object> v = getOrset(specificvalues, k, false);
				if (v.isEmpty()) {
					v.add(string);
				}
				res.addAll(v);
			}
			return res;
		}
		return null;
	}

	public List<QueryModifier> translateToQuery(String filterID, List<String> commonValue) {
		if (commonValue != null) {
			List<QueryModifier> res = new ArrayList<>();
			List<Object> values = translateToSpecific(filterID, commonValue);
			Function<List<String>, QueryModifier> w = writters.get(filterID);
			if (w != null)
				res.add(w.apply(ListUtils.transform(values, (Object x)-> x.toString())));
			return res;
		}
		return null;
	}

	public void addDefaultWriter(String filterId, Function<List<String>, QueryModifier> function) {
		writters.put(filterId, function);
	}

	public Boolean containsFilter(String filterID) {
		return writters.containsKey(filterID);
	}
	
	
	
	
	
	private void addMapping(String id, Object obj, String... string) {
		addMap(id, obj, string);
	}
	
	public static FilterValuesMap getMap(Sources source){
		FilterValuesMap ms = map.get(source);
		if (ms==null){
			ms = MapsConfig.buildFilterValuesMap(source);
			map.put(source, ms);
		}
		return ms;
	}
	


	private void fillDBPedia() {
//		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, "Image", "Photograph",
//				"Poster, chart, other");
//		addMapping(CommonFilters.TYPE.getId(), WithMediaType.VIDEO, "Video");
//		addMapping(CommonFilters.TYPE.getId(), WithMediaType.AUDIO, "Sound", "Sheet music");
//		addMapping(CommonFilters.TYPE.getId(), WithMediaType.TEXT, "Books", "Article");
	}
	
	private void fillFlickr() {
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, "photo");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.VIDEO, "video");

		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RR, FlickrSpaceSource.getLicence("0"));
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative_Not_Commercial, FlickrSpaceSource.getLicence("3"),
				BritishLibrarySpaceSource.getLicence("2"), FlickrSpaceSource.getLicence("1"));
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Modify, FlickrSpaceSource.getLicence("6"));
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative, FlickrSpaceSource.getLicence("1"), FlickrSpaceSource.getLicence("2"),
				BritishLibrarySpaceSource.getLicence("3"), FlickrSpaceSource.getLicence("4"), FlickrSpaceSource.getLicence("5"), FlickrSpaceSource.getLicence("6"));
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.UNKNOWN, FlickrSpaceSource.getLicence("7"));
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Public, FlickrSpaceSource.getLicence("9"), FlickrSpaceSource.getLicence("10"));

	}
	private void fillNLA() {
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, "Image", "Photograph",
				"Poster, chart, other");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.VIDEO, "Video");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.AUDIO, "Sound", "Sheet music");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.TEXT, "Books", "Article");
	}

	private void fillDDB() {
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, "mediatype_002");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.AUDIO, "mediatype_001");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.TEXT, "mediatype_003", "mediatype_004");		
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.VIDEO, "mediatype_005");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.OTHER, "mediatype_006","mediatype_007","mediatype_008");
		
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative, ".*creative.*");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative_Not_Commercial, ".*creative(?!.*nc).*");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Modify, ".*creative(?!.*nd).*");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RR, ".*rr-.*",".*rv-fz.*");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.UNKNOWN, ".*unknown.*");
		
	}
	
	
	private void fillDPLA() {
		/**
		 * TODO give it a try to the rights
		 */

//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Commercial, ".*creative(?!.*nc).*");
//		// ok RIGHTS:*creative* AND NOT RIGHTS:*nd*
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Modify, ".*creative(?!.*nd).*");
//
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative_Not_Commercial, ".*creative.*nc.*",
//				".*non-commercial.*");
//
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RRPA, ".*rr-p.*");
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RRRA, ".*rr-r.*");
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RRFA, ".*rr-f.*");
//
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RRFA, ".*unknown.*");
//
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative_Not_Modify, ".*creative.*nd.*");
//
//		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative, ".*(creative).*");

		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, "image");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.VIDEO, "moving image");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.AUDIO, "sound");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.TEXT, "text");
	}
	
	private void fillDNZ() {

		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, "Images");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.AUDIO, "Audio");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.VIDEO, "Videos");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.TEXT, "Books",
				"Articles","Newspapers",
				"Research papers",
				"Manuscripts");

		// addMapping(CommonFilters.RIGHTS.name(),
		// RightsValues.Creative_Commercial,
		// "");
		// ok RIGHTS:*creative* AND NOT RIGHTS:*nd*
		// addMapping(CommonFilters.RIGHTS.name(), RightsValues.Creative_Modify,
		// ".*creative(?!.*nd).*");

		// addMapping(CommonFilters.RIGHTS.name(),
		// RightsValues.Creative_Not_Commercial,
		// "http://creativecommons.org/licenses/by-nc/3.0/nz/",
		// "http://creativecommons.org/licenses/by-nc-sa/3.0/",
		// "This work is licensed under a Creative Commons
		// Attribution-Noncommercial 3.0 New Zealand License");
		//
		// addMapping(CommonFilters.RIGHTS.name(), RightsValues.UNKNOWN, "No
		// known
		// copyright restrictions\nCopyright Expired",
		// "No known copyright restrictions");
		// addMapping(CommonFilters.RIGHTS.name(), RightsValues.RR, "All rights
		// reserved");

		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Creative, "Share");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Modify, "Modify");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.Commercial, "Use commercially");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.UNKNOWN, "Unknown");
		addMapping(CommonFilters.RIGHTS.getId(), WithMediaRights.RR, "All rights reserved");
	}

	

	private void fillRijks() {
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.IMAGE, 
				"drawing","painting","photograph","documentary photographs");
		addMapping(CommonFilters.TYPE.getId(), WithMediaType.TEXT, 
				"book","poem","text sheet","print","manuscript");
	}

}
