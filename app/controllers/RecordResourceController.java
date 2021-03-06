package controllers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import model.DescriptiveData;
import model.annotations.Annotation;
import model.annotations.Annotation.AnnotationAdmin;
import model.annotations.Annotation.AnnotationScore;
import model.annotations.ContextData;
import model.annotations.ContextData.ContextDataType;
import model.annotations.selectors.PropertyTextFragmentSelector;
import model.annotations.targets.AnnotationTarget;
import model.basicDataTypes.Language;
import model.basicDataTypes.MultiLiteral;
import model.resources.RecordResource;
import model.resources.collection.CollectionObject;
import model.resources.collection.CollectionObject.CollectionAdmin;
import model.usersAndGroups.User;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;
import play.data.validation.Validation;
import play.libs.F.Option;
import play.libs.Json;
import play.mvc.Result;
import utils.Tuple;
import annotators.Annotator;
import annotators.AnnotatorConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import db.DB;

import java.util.Random;
import java.util.HashSet;

import play.libs.F.Some;
/**
 * @author mariaral
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RecordResourceController extends WithResourceController {

	public static final ALogger log = Logger.of(RecordResourceController.class);

	/**
	 * Retrieve a resource metadata. If the format is defined the specific
	 * serialization of the object is returned
	 *
	 * @param id
	 *            the resource id
	 * @param format
	 *            the resource serialization
	 * @return the resource metadata
	 */
	public static Result getRecordResource(String id, Option<String> format, String profile, Option<String> locale) {
		ObjectNode result = Json.newObject();
		try {
			RecordResource record = DB.getRecordResourceDAO().get(
					new ObjectId(id));
			Result response = errorIfNoAccessToRecord(Action.READ,
					new ObjectId(id));
			if (!response.toString().equals(ok().toString()))
				return response;
			else {
				// filter out all context annotations refering to collections to
				// which the user has no read access rights
				//filterContextData(record);
				if (format.isDefined()) {
					String formats = format.get();
					if (formats.equals("contentOnly")) {
						return ok(Json.toJson(record.getContent()));
					} else {
						if (formats.equals("noContent")) {
							record.getContent().clear();
							RecordResource profiledRecord = record.getRecordProfile(profile);
							filterResourceByLocale(locale, profiledRecord);
							return ok(Json.toJson(profiledRecord));
						} else if (record.getContent() != null
								&& record.getContent().containsKey(formats)) {
							return ok(record.getContent().get(formats)
									.toString());
						} else {
							result.put("error",
									"Resource does not contain representation for format "
											+ formats);
							return play.mvc.Results.notFound(result);
						}
					}
				} else
					return ok(Json.toJson(record));
			}
		} catch (Exception e) {
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}

	/**
	 * Edits the WITH resource according to the JSON body. For every field
	 * mentioned in the JSON body it either edits the existing one or it adds it
	 * (in case it doesn't exist)
	 *
	 * @param id
	 * @return the edited resource
	 */
	// TODO check restrictions (unique fields e.t.c)
	// TODO: edit contextData separately ONLY for collections for which the user
	// has access
	public static Result editRecordResource(String id) {
		ObjectNode error = Json.newObject();
		ObjectId recordDbId = new ObjectId(id);
		JsonNode json = request().body().asJson();
		try {
			if (json == null) {
				error.put("error", "Invalid JSON");
				return badRequest(error);
			} else {
				Result response = errorIfNoAccessToRecord(Action.EDIT,
						new ObjectId(id));
				if (!response.toString().equals(ok().toString()))
					return response;
				else {
					// TODO Check the JSON
					DB.getRecordResourceDAO().editRecord("", recordDbId, json);
					return ok("Record edited.");
				}
			}
		} catch (Exception e) {
			error.put("error", e.getMessage());
			return internalServerError(error);
		}
	}

	public static Result editContextData(String collectionId) throws Exception {
		ObjectNode error = Json.newObject();
		ObjectNode json = (ObjectNode) request().body().asJson();
		if (json == null) {
			error.put("error", "Invalid JSON");
			return badRequest(error);
		} else {
			String contextDataType = null;
			if (json.has("contextDataType")) {
				contextDataType = json.get("contextDataType").asText();
				ContextDataType dataType;
				if (contextDataType != null
						& (dataType = ContextDataType.valueOf(contextDataType)) != null) {
					Class clazz;
					try {
						int position = ((ObjectNode) json.get("target")).remove("position").asInt();
						if (dataType.equals(ContextDataType.ExhibitionData)) {
							clazz = Class.forName("model.annotations."
									+ contextDataType);
							ContextData newContextData = (ContextData) Json
									.fromJson(json, clazz);
							ObjectId collectionDbId = new ObjectId(collectionId);
							// int position =
							// newContextData.getTarget().getPosition();
							if (collectionId != null
									&& DB.getCollectionObjectDAO()
											.existsEntity(collectionDbId)) {
								// filterContextData(record);
								Result response = errorIfNoAccessToCollection(
										Action.EDIT, collectionDbId);
								if (!response.toString()
										.equals(ok().toString()))
									return response;
								else {
									DB.getCollectionObjectDAO()
											.updateContextData(collectionDbId, newContextData,
													position);

								}
							}
						}
						return ok("Edited context data.");
					} catch (ClassNotFoundException e) {
						log.error("",e);
						return internalServerError(error);
					}
				} else
					return badRequest("Context data type should be one of supported types: ExhibitionData.");
			} else
				return badRequest("The contextDataType should be defined.");
		}
	}

	// TODO: Are we checking rights for every record alone?
	public static Result list(String collectionId, int start, int count) {
		List<RecordResource> records = DB.getRecordResourceDAO()
				.getByCollectionBetweenPositions(new ObjectId(collectionId),
						start, start + count);
		return ok(Json.toJson(records));
	}

	// TODO: Remove favorites

	/**
	 * @return
	 */
	public static Result getFavorites() {
		ObjectNode result = Json.newObject();
		if (loggedInUser() == null) {
			return forbidden();
		}
		ObjectId userId = new ObjectId(loggedInUser());
		CollectionObject favorite;
		ObjectId favoritesId;
		if ((favorite = DB.getCollectionObjectDAO().getByOwnerAndLabel(userId,
				null, "_favorites")) == null) {
			favoritesId = CollectionObjectController.createFavorites(userId);
		} else {
			favoritesId = favorite.getDbId();
		}
		List<RecordResource> records = DB.getRecordResourceDAO()
				.getByCollection(favoritesId);
		if (records == null) {
			result.put("error", "Cannot retrieve records from database");
			return internalServerError(result);
		}
		ArrayNode recordsList = Json.newObject().arrayNode();
		for (RecordResource record : records) {
			recordsList.add(record.getAdministrative().getExternalId());
		}
		return ok(recordsList);
	}

	public static ObjectNode validateRecord(RecordResource record) {
		ObjectNode result = Json.newObject();
		Set<ConstraintViolation<RecordResource>> violations = Validation
				.getValidator().validate(record);
		if (!violations.isEmpty()) {
			ArrayNode properties = Json.newObject().arrayNode();
			for (ConstraintViolation<RecordResource> cv : violations) {
				properties.add(Json.parse("{\"" + cv.getPropertyPath()
						+ "\":\"" + cv.getMessage() + "\"}"));
			}
			result.put("error", properties);
			return result;
		} else {
			return null;
		}
	}
	
	public static Result annotateRecord(String recordId) {
		ObjectNode result = Json.newObject();
		try {
//			Result response = errorIfNoAccessToRecord(Action.EDIT, new ObjectId(recordId));
//			if (!response.toString().equals(ok().toString())) {
//				return response;
//			} else {
				JsonNode json = request().body().asJson();
				
				List<AnnotatorConfig> annConfigs = AnnotatorConfig.createAnnotationConfigs(json);
				ObjectId user = WithController.effectiveUserDbId();
				
				annotateRecord(recordId, user, annConfigs);
				
				return ok(result);
//			}				
		} catch (Exception e) {
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}

	public static void annotateRecord(String recordId, ObjectId user, List<AnnotatorConfig> annConfigs) throws Exception {
		String[] fields = new String[] {"description", "label"};
		
		RecordResource record = DB.getRecordResourceDAO().get(new ObjectId(recordId));
		DescriptiveData dd = record.getDescriptiveData();
		
		for (String p : fields) {
			Method method = dd.getClass().getMethod("get" + p.substring(0,1).toUpperCase() + p.substring(1));
		
			Object res = method.invoke(dd);
			if (res != null && res instanceof MultiLiteral) {
				MultiLiteral value = (MultiLiteral)res;

				for (Language lang : value.getLanguages()) {
					if (lang == Language.UNKNOWN || lang == Language.DEFAULT) {
						continue;
					}
					
					for (String text : value.get(lang)) {
						AnnotationTarget target = new AnnotationTarget();
						target.setRecordId(record.getDbId());
						
						PropertyTextFragmentSelector selector = new PropertyTextFragmentSelector();
						selector.setOrigValue(text);
						selector.setOrigLang(lang);
						selector.setProperty(p);
						
						target.setSelector(selector);
						
						for (AnnotatorConfig annConfig : annConfigs) {
							Annotator annotator = Annotator.getAnnotator(annConfig.getAnnotatorClass(), lang);
							if (annotator != null) {
								try {
									for (Annotation ann : annotator.annotate(text, target, annConfig.getProps())) {
										AnnotationController.addAnnotation(ann, user);
									}
								} catch (Exception e) {
									e.printStackTrace();
									log.error(e.getMessage());
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static Result deleteRejectedAnnotations(String rid) {
		ObjectNode result = Json.newObject();
		try {
//			Result response = errorIfNoAccessToRecord(Action.EDIT, new ObjectId(rid));
//			if (!response.toString().equals(ok().toString())) {
//				return response;
//			} else {
				ObjectId userId = WithController.effectiveUserDbId();
					
				for (Annotation annotation : DB.getAnnotationDAO().getUserAnnotations(userId, new ObjectId(rid), Arrays.asList("annotators", "score.rejectedBy"))) {
					AnnotationScore as = annotation.getScore();
					if (as != null) {
						ArrayList<ObjectId> rej = as.getRejectedBy();
						if (rej != null) {
							for (ObjectId id : rej) {
								if (id.equals(userId)) {
									ArrayList<AnnotationAdmin> annotators = annotation.getAnnotators();
									AnnotationAdmin annotator = null;
									for (AnnotationAdmin a : annotators) {
										if (a.getWithCreator().equals(userId)) {
											annotator = a;
										}
									}
			
									ObjectId annId = annotation.getDbId();
									if (annotators.size() == 1) {
										DB.getAnnotationDAO().deleteAnnotation(annId);
									} else {
										DB.getAnnotationDAO().removeAnnotators(annotation.getDbId(), Arrays.asList(annotator));
									}
								}
							}
						}
					}
				}
				
				return ok();
//			}				
		} catch (Exception e) {
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}
	
	public static Result getRandomRecords(String groupId, int batchCount) {
		ObjectId group = new ObjectId(groupId);
		CollectionAndRecordsCounts collectionsAndCount = getCollsAndCountAccessiblebyGroup(group);
		//int collectionCount = collectionsAndCount.collectionsRecordCount.size();
		//generate batchCount unique numbers from 0 to totalRecordsCount
		Random rng = new Random();
		Set<Integer> randomNumbers = new HashSet<Integer>();
		List<RecordResource> records = new ArrayList<RecordResource>();
		while (randomNumbers.size() < batchCount)
		{
		    Integer next = rng.nextInt(collectionsAndCount.totalRecordsCount);
		    randomNumbers.add(next);
		}
		for (Integer random: randomNumbers) {
			int colPosition = -1;
			int previousRecords = 0;
			int recordCount = 0;
			while (random > previousRecords) {
				recordCount = collectionsAndCount.collectionsRecordCount.get(++colPosition).y;
				previousRecords += recordCount;
			}
			int recordPosition = random - (previousRecords - recordCount) - 1;
			CollectionObject collection = DB.getCollectionObjectDAO().getById(
					collectionsAndCount.collectionsRecordCount.get(colPosition).x, Arrays.asList("collectedResources"));
			ContextData contextData = (ContextData) collection.getCollectedResources().get(recordPosition);
			ObjectId recordId = contextData.getTarget().getRecordId();
			records.add(DB.getRecordResourceDAO().getById(recordId));
		}
		ArrayNode recordsList = Json.newObject().arrayNode();
		for (RecordResource record : records) {
			Some<String> locale = new Some(Language.DEFAULT.toString());
			RecordResource profiledRecord = record.getRecordProfile(Profile.MEDIUM.toString());
			filterResourceByLocale(locale, profiledRecord);
			recordsList.add(Json.toJson(profiledRecord));
		}
		return ok(recordsList);
	}
	
	static CollectionAndRecordsCounts getCollsAndCountAccessiblebyGroup(ObjectId groupId) {
		List<CollectionObject> collections = DB.getCollectionObjectDAO().getAccessibleByGroupAndPublic(groupId);
		CollectionAndRecordsCounts colAndRecs = new CollectionAndRecordsCounts();
		for (CollectionObject col: collections) {
			int entryCount = ((CollectionAdmin) col.getAdministrative()).getEntryCount();
			colAndRecs.collectionsRecordCount.add(new Tuple(col.getDbId(), entryCount));
			colAndRecs.totalRecordsCount += entryCount;
		}
		return colAndRecs;
	}
	
	public static class CollectionAndRecordsCounts {
		List<Tuple<ObjectId, Integer>> collectionsRecordCount = new ArrayList<Tuple<ObjectId, Integer>>();
		int totalRecordsCount = 0;
	}
	
	public static Result getAnnotations(String id) {
		ObjectNode result = Json.newObject();
		try {
			RecordResource record = DB.getRecordResourceDAO().get(new ObjectId(id));
			Result response = errorIfNoAccessToRecord(Action.READ,
					new ObjectId(id));
			if (!response.toString().equals(ok().toString()))
				return response;
			else {
				ArrayNode array = result.arrayNode();
				record.fillAnnotations();
				
				List<Annotation> anns = record.getAnnotations();
				
				for (Annotation ann : anns) {
					JsonNode json = Json.toJson(ann);
					for (Iterator<JsonNode> iter = json.get("annotators").elements(); iter.hasNext();) {
						JsonNode annotator = iter.next();
						
						String userId = annotator.get("withCreator").asText();
						
						User user = DB.getUserDAO().getById(new ObjectId(userId));
						((ObjectNode)annotator).put("username", user.getUsername());
						
					}
					array.add(json);
				}
				return ok(array);
			}				
		} catch (Exception e) {
			result.put("error", e.getMessage());
			return internalServerError(result);
		}
	}
}
