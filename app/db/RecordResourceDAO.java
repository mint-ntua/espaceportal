package db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import model.EmbeddedMediaObject;
import model.EmbeddedMediaObject.MediaVersion;
import model.annotations.Annotation;
import model.annotations.ContextData;
import model.annotations.ContextData.ContextDataBody;
import model.basicDataTypes.WithAccess;
import model.basicDataTypes.WithAccess.Access;
import model.basicDataTypes.WithAccess.AccessEntry;
import model.resources.RecordResource;
import model.resources.collection.CollectionObject;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import play.Logger;
import play.Logger.ALogger;
import sources.core.ParallelAPICall;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;

import controllers.WithController.Action;
import elastic.ElasticEraser;

/*
 * This class is the aggregator of methods
 * generically referring to *Object entities. We may assume
 * that these entities represent a Record of a Collection more or less.
 *
 * Type T is used in order for Morphia to know in which type is going
 * deserialize the object retrieved fro Mongo. So we have to options
 *
 * 1. Either pass WithResource when instansiating so that all entities
 * handled as WithResources.
 *
 * 2. Every time create a new DAO class associated with the explicit class
 * that I want to retieve.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class RecordResourceDAO extends WithResourceDAO<RecordResource> {
	public static final ALogger log = Logger.of(RecordResourceDAO.class);

	public RecordResourceDAO() {
		super(RecordResource.class);
	}

	public List<RecordResource> getByCollectionBetweenPositions(
			ObjectId collectionId, int lowerBound, int upperBound) {
		if (upperBound < lowerBound)
			return new ArrayList<RecordResource>();
		CollectionObject collection = DB.getCollectionObjectDAO()
				.getSliceOfCollectedResources(collectionId, lowerBound,
						upperBound - lowerBound);
		Query<RecordResource> q = this.createQuery();
		return getRecords(collection.getCollectedResources(), q);
	}

	public List<RecordResource> getByCollectionBetweenPositionsAndSort(
			ObjectId collectionId, int lowerBound, int upperBound,
			String sortingCriteria) {
		if (upperBound < lowerBound)
			return new ArrayList<RecordResource>();
		CollectionObject collection = DB.getCollectionObjectDAO()
				.getSliceOfCollectedResources(collectionId, lowerBound,
						upperBound - lowerBound);
		Query<RecordResource> q = this.createQuery().order(sortingCriteria);
		return getRecords(collection.getCollectedResources(), q);
	}

	/**
	 * Retrieve records from specific collection whose position is between
	 * lowerBound and upperBound. If a record appears n times in a collection
	 * (in different positions), n copies will appear in the returned list, in
	 * the respective positions.
	 *
	 * @param collection
	 *            , lowerBound, upperBound
	 * @return
	 */
	public List<RecordResource> getRecords(
			List<ContextData<ContextDataBody>> collectedResources,
			Query<RecordResource> q) {
		if (collectedResources == null || collectedResources.isEmpty())
			return new ArrayList<RecordResource>();
		List<ContextData<ContextDataBody>> contextData = collectedResources;
		List<ObjectId> recordIds = (List<ObjectId>) CollectionUtils.collect(
				contextData, new BeanToPropertyValueTransformer(
						"target.recordId"));
		q.field("_id").in(recordIds);
		List<RecordResource> records = this.find(q).asList();
		List<RecordResource> orderedRecords = new ArrayList<RecordResource>();
		for (ObjectId recordId : recordIds) {
			for (RecordResource record : records) {
				if (record.getDbId().equals(recordId)) {
					orderedRecords.add(record);
					break;
				}
			}
		}
		return orderedRecords;
	}
	
	public List<RecordResource> getByCollectionIds(ObjectId colId, List<String> ids) {
		Query<RecordResource> q = this.createQuery();
		return getByCollectionIds(colId, ids, q);
	}

	public List<RecordResource> getByCollectionIds(ObjectId colId,
			List<String> ids, Query<RecordResource> q) {
		
		List<ObjectId> oids = new ArrayList<>();
		for (String s : ids) {
			oids.add(new ObjectId(s));
		}
		
		BasicDBObject geq = new BasicDBObject();
		geq.put("$in", oids);

		q.filter("collectedIn", colId);
		q.filter("_id", geq);
		
		List<RecordResource> resources = this.find(q).asList();
		
		return resources;
				
	}

	public List<RecordResource> getByCollection(ObjectId collectionId) {
		CollectionObject collection = DB.getCollectionObjectDAO().getById(
				collectionId,
				new ArrayList<String>(Arrays.asList("collectedResources")));
		Query<RecordResource> q = this.createQuery();
		return getByCollection(collection.getCollectedResources(), q);
	}

	public List<RecordResource> getByCollection(ObjectId collectionId,
			List<String> retrievedFields) {
		CollectionObject collection = DB.getCollectionObjectDAO().getById(
				collectionId,
				new ArrayList<String>(Arrays.asList("collectedResources")));
		Query<RecordResource> q = this.createQuery().retrievedFields(true,
				retrievedFields.toArray(new String[retrievedFields.size()]));
		return getByCollection(collection.getCollectedResources(), q);
	}

	public List<RecordResource> getAnnotatedRecords(ObjectId userId,
			int offset, int count) {
		List<Annotation> annotations = DB.getAnnotationDAO()
				.getUserAnnotations(userId, Arrays.asList("target.recordId"));
		if (annotations.isEmpty())
			return new ArrayList<RecordResource>();
		List<ObjectId> recordIds = (List<ObjectId>) CollectionUtils.collect(
				annotations, new BeanToPropertyValueTransformer(
						"target.recordId"));
		Query<RecordResource> q = this.createQuery().field("_id").in(recordIds)
				.offset(offset).limit(count);
		List<RecordResource> records = this.find(q).asList();
		return records;
	}

	/**
	 * Retrieve all records that belong to that collection. If a record is
	 * included several times in a collection, it will only appear a single time
	 * in the returned list
	 *
	 * @param collection
	 * @return
	 */
	public List<RecordResource> getByCollection(
			List<ContextData<ContextDataBody>> collectedResources,
			Query<RecordResource> q) {
		try {
			List<ObjectId> recordIds = (List<ObjectId>) CollectionUtils
					.collect(collectedResources,
							new BeanToPropertyValueTransformer(
									"target.recordId"));
			q.field("_id").in(recordIds);
			return this.find(q).asList();
		} catch (Exception e) {
			return new ArrayList<RecordResource>();
		}
	}

	/**
	 * sorts the result considering {@link sortingFiled}
	 * 
	 * @see {@code ResourceRecord.getByCollection}
	 */
	public List<RecordResource> getByCollectionAndSort(
			List<ContextData<ContextDataBody>> collectedResources,
			Query<RecordResource> q, String sortingField) {
		try {
			List<ObjectId> recordIds = (List<ObjectId>) CollectionUtils
					.collect(collectedResources,
							new BeanToPropertyValueTransformer(
									"target.recordId"));
			q.field("_id").in(recordIds).order(sortingField);
			return this.find(q).asList();
		} catch (Exception e) {
			return new ArrayList<RecordResource>();
		}
	}

	public void updateRecordUsageCollectedAndRights(ObjectId collectionId,
			WithAccess access, ObjectId recordId, boolean isPublic) {
		Query<RecordResource> q = this.createQuery().field("_id")
				.equal(recordId);
		UpdateOperations<RecordResource> recordUpdate = this
				.createUpdateOperations();
		// TODO: what if already entry with the same colId-position? have to
		// remove first to avoid duplicates.
		recordUpdate.add("collectedIn", collectionId);
		if (access != null)
			recordUpdate.set("administrative.access", access);
		if (isPublic)
			recordUpdate.set("administrative.access.isPublic", true);
		if (DB.getCollectionObjectDAO().isFavorites(collectionId))
			recordUpdate.inc("usage.likes");
		else
			recordUpdate.inc("usage.collected");
		this.update(q, recordUpdate);
	}

	// TODO: has to be atomic as a whole
	// uses findAndModify for entryCount of respective collection
	public void addToCollection(ObjectId recordId, ObjectId collectionId,
			int position, boolean changeRecRights) {
		CollectionObject collection = DB.getCollectionObjectDAO()
				.addToCollection(collectionId, recordId, position, false);
		WithAccess newAccess = null;
		if (changeRecRights)
			newAccess = mergeParentCollectionRights(recordId, collectionId,
					collection.getAdministrative().getAccess());
		updateRecordUsageCollectedAndRights(collectionId, newAccess, recordId,
				collection.getAdministrative().getAccess().getIsPublic());
		DB.getCollectionObjectDAO().addCollectionMedia(collectionId, recordId,
				position);
	}

	public void appendToCollection(ObjectId recordId, ObjectId collectionId,
			boolean changeRecRights) {
		CollectionObject collection = DB.getCollectionObjectDAO()
				.addToCollection(collectionId, recordId, -1, true);
		WithAccess newAccess = null;
		if (changeRecRights)
			newAccess = mergeParentCollectionRights(recordId, collectionId,
					collection.getAdministrative().getAccess());
		updateRecordUsageCollectedAndRights(collectionId, newAccess, recordId,
				collection.getAdministrative().getAccess().getIsPublic());
		DB.getCollectionObjectDAO().addCollectionMedia(collectionId, recordId,
				collection.getCollectedResources().size());
	}

	// TODO Refactor
	public WithAccess mergeParentCollectionRights(ObjectId recordId,
			ObjectId newCollectionId, WithAccess newCollectionAccess) {
		RecordResource record = this.getById(
				recordId,
				new ArrayList<String>(Arrays.asList(
						"administrative.access.isPublic",
						"administrative.withCreator")));
		List<ObjectId> parentCollections = getParentCollections(recordId);
		List<WithAccess> parentColAccess = new ArrayList<WithAccess>();
		for (ObjectId colId : parentCollections) {
			if (colId.equals(newCollectionId))
				parentColAccess.add(newCollectionAccess);
			else {
				CollectionObject parentCollection = DB.getCollectionObjectDAO()
						.getById(
								colId,
								new ArrayList<String>(Arrays
										.asList("administrative.access")));
				parentColAccess.add(parentCollection.getAdministrative()
						.getAccess());
			}
		}
		// hope there aren't too many collections containing the resource
		// if the record is public, it should remain public. Acl rights are
		// determined by the collections the record belongs to
		return mergeRights(parentColAccess, record.getAdministrative()
				.getWithCreator(), record.getAdministrative().getAccess()
				.getIsPublic());
	}

	public boolean mergeParentCollectionPublicity(ObjectId recordId,
			boolean isPublic, ObjectId newColId) {
		boolean mergedIsPublic = isPublic;
		List<ObjectId> parentCollections = getParentCollections(recordId);
		for (ObjectId colId : parentCollections) {
			CollectionObject parentCollection = DB.getCollectionObjectDAO()
					.getById(
							colId,
							new ArrayList<String>(Arrays
									.asList("administrative.access")));
			if (parentCollection.getAdministrative().getAccess().getIsPublic())
				return true;
		}
		return mergedIsPublic;
	}

	public void updateMembersToMergedRights(ObjectId collectionId,
			AccessEntry newAccess, List<ObjectId> effectiveIds) {
		ArrayList<String> retrievedFields = new ArrayList<String>(
				Arrays.asList("_id", "administrative.access"));
		WithAccess colAccess = DB.getCollectionObjectDAO()
				.getById(collectionId, retrievedFields).getAdministrative()
				.getAccess();
		colAccess.addToAcl(newAccess);
		List<RecordResource> memberRecords = getByCollection(collectionId,
				retrievedFields);
		for (RecordResource r : memberRecords) {
			if (DB.getRecordResourceDAO().hasAccess(effectiveIds,
					Action.DELETE, r.getDbId())) {
				WithAccess mergedAccess = mergeParentCollectionRights(
						r.getDbId(), collectionId, colAccess);
				updateField(r.getDbId(), "administrative.access", mergedAccess);
			}
		}
	}

	public void updateMembersToMergedPublicity(ObjectId colId,
			boolean isPublic, List<ObjectId> effectiveIds) {
		ArrayList<String> retrievedFields = new ArrayList<String>(
				Arrays.asList("_id", "administrative.access"));
		List<RecordResource> memberRecords = getByCollection(colId,
				retrievedFields);
		for (RecordResource r : memberRecords) {
			if (DB.getRecordResourceDAO().hasAccess(effectiveIds,
					Action.DELETE, r.getDbId())) {
				boolean mergedPublicity = mergeParentCollectionPublicity(
						r.getDbId(), isPublic, colId);
				updateField(r.getDbId(), "administrative.access.isPublic",
						mergedPublicity);
			}
		}
	}

	public void updateRecordRightsUponRemovalFromCollection(ObjectId recordId,
			ObjectId collectionId) {
		RecordResource record = this.getById(
				recordId,
				new ArrayList<String>(Arrays.asList("collectedIn",
						"administrative.access.isPublic",
						"administrative.withCreator")));
		List<ObjectId> parentCollections = getParentCollections(recordId);
		parentCollections.remove(collectionId);
		List<WithAccess> parentColAccess = new ArrayList<WithAccess>();
		for (ObjectId parentId : parentCollections) {
			CollectionObject parentCollection;
			if ((parentCollection = DB.getCollectionObjectDAO().getById(
					parentId,
					new ArrayList<String>(Arrays
							.asList("administrative.access")))) != null)
				parentColAccess.add(parentCollection.getAdministrative()
						.getAccess());
		}
		WithAccess mergedAccess = mergeRights(parentColAccess, record
				.getAdministrative().getWithCreator(), record
				.getAdministrative().getAccess().getIsPublic());
		updateField(recordId, "administrative.access", mergedAccess);
	}

	public void updateMembersToNewAccess(ObjectId collectionId,
			ObjectId userId, Access newAccess, List<ObjectId> effectiveIds) {
		ArrayList<String> retrievedFields = new ArrayList<String>(
				Arrays.asList("_id"));
		List<RecordResource> memberRecords = getByCollection(collectionId,
				retrievedFields);
		for (RecordResource r : memberRecords) {
			if (DB.getRecordResourceDAO().hasAccess(effectiveIds,
					Action.DELETE, r.getDbId()))
				changeAccess(r.getDbId(), userId, newAccess);
		}
	}

	public void updateMembersToNewPublicity(ObjectId colId, boolean isPublic,
			List<ObjectId> effectiveIds) {
		ArrayList<String> retrievedFields = new ArrayList<String>(
				Arrays.asList("_id"));
		List<RecordResource> memberRecords = getByCollection(colId,
				retrievedFields);
		for (RecordResource r : memberRecords) {
			if (DB.getRecordResourceDAO().hasAccess(effectiveIds,
					Action.DELETE, r.getDbId()))
				DB.getRecordResourceDAO().updateField(r.getDbId(),
						"administrative.access.isPublic", isPublic);
		}
	}

	public void removeFromCollection(ObjectId recordId, ObjectId collectionId,
			int position, boolean first, boolean all) throws Exception {
		DB.getCollectionObjectDAO().removeFromCollection(collectionId,
				recordId, position, first, all);
		UpdateOperations<RecordResource> recordUpdate = this
				.createUpdateOperations();
		Query<RecordResource> q = this.createQuery().field("_id")
				.equal(recordId);
		recordUpdate.removeAll("collectedIn", Arrays.asList(collectionId));
		if (DB.getCollectionObjectDAO().isFavorites(collectionId))
			recordUpdate.dec("usage.likes");
		else
			recordUpdate.dec("usage.collected");
		this.update(q, recordUpdate);
		updateRecordRightsUponRemovalFromCollection(recordId, collectionId);
		removeRecordIfNotCollected(recordId);
	}

	private void removeRecordIfNotCollected(ObjectId recordId) {
		RecordResource record = this.getById(recordId);
		if ((record.getCollectedIn() == null)
				|| (record.getCollectedIn().size() == 0)) {
			this.makeTransient(record);
		}
	}

	public RecordResource getByCollectionAndPosition(ObjectId collectionId,
			int position) {
		List<ContextData<ContextDataBody>> collectedResources = DB
				.getCollectionObjectDAO()
				.getById(collectionId, Arrays.asList("collectedResources"))
				.getCollectedResources();
		if (collectedResources.size() <= position)
			return null;
		ObjectId recordId = collectedResources.get(position).getTarget()
				.getRecordId();
		return this.get(recordId);
	}

	public long countAnnotatedRecords(ObjectId collectionId) {
		long count = this.createQuery().disableValidation()
				.field("collectedIn").equal(collectionId)
				.field("annotationIds").exists().field("annotationIds").not()
				.sizeEq(0).countAll();
		return count;

	}

	public long countAnnotations(ObjectId collectionId) {
		int count = 0;
		Query<RecordResource> q = this.createQuery().disableValidation()
				.field("collectedIn").equal(collectionId)
				.field("annotationIds").exists().field("annotationIds").not()
				.sizeEq(0).retrievedFields(true, "annotationIds");
		List<RecordResource> records = this.find(q).asList();
		for (RecordResource record : records) {
			count += record.getAnnotationIds().size();
		}
		return count;
	}

	public List<RecordResource> getByMedia(String mediaUrl) {
		Query<RecordResource> q = this.createQuery().disableValidation()
				.field("media.Original.url").equal(mediaUrl);
		return this.find(q.retrievedFields(true, "_id")).asList();
	}

	public boolean existsSameExternaIdInCollection(String externalId,
			ObjectId collectionId) {
		Query<RecordResource> q = this.createQuery().disableValidation()
				.field("collectedIn").equal(collectionId);
		q.field("administrative.externalId").equal(externalId);
		return this.find(q.limit(1)).asList().size() == 0 ? false : true;
	}

	public void editRecord(String root, ObjectId dbId, JsonNode json) {
		Query<RecordResource> q = this.createQuery().field("_id").equal(dbId);
		UpdateOperations<RecordResource> updateOps = this
				.createUpdateOperations();
		updateFields(root, json, updateOps);
		updateOps.set("administrative.lastModified", new Date());
		this.update(q, updateOps);
	}

	public void addAnnotation(ObjectId recordId, ObjectId annotationId) {
		Query<RecordResource> q = this.createQuery().field("_id")
				.equal(recordId);
		UpdateOperations<RecordResource> updateOps = this
				.createUpdateOperations();
		updateOps.add("annotationIds", annotationId);
		this.update(q, updateOps);
	}

	public void removeAllRecordsFromCollection(ObjectId collectionId) {
		ArrayList<String> retrievedFields = new ArrayList<String>(
				Arrays.asList("_id", "collectedIn"));
		List<RecordResource> memberRecords = getByCollection(collectionId,
				retrievedFields);
		for (RecordResource record : memberRecords) {
			ObjectId recordId = record.getDbId();
			List<ObjectId> collectedIn = record.getCollectedIn();
			if (collectedIn.size() > 1) {
				UpdateOperations<RecordResource> updateOps = this
						.createUpdateOperations();
				Query<RecordResource> q = this.createQuery().field("_id")
						.equal(recordId);
				updateOps.removeAll("collectedIn", collectionId);
				// TODO See if it is collected in the same collection more than
				// once
				updateOps.dec("usage.collected");
				this.update(q, updateOps);
			} else
				deleteById(recordId);
		}

		/*
		 * Delete resources of these collection. Practically update field
		 * 'collectedIn' of the resource on the index.
		 */
		List<ObjectId> resourceIds = new ArrayList<ObjectId>();
		memberRecords.forEach((r) -> {
			resourceIds.add(r.getDbId());
		});
		Function<List<ObjectId>, Boolean> deleteResources = (List<ObjectId> ids) -> (ElasticEraser
				.deleteManyResources(ids));
		ParallelAPICall.createPromise(deleteResources, resourceIds);
	}

	public void removeAnnotation(ObjectId recordId, ObjectId annotationId) {
		UpdateOperations<RecordResource> recordUpdate = this
				.createUpdateOperations();
		Query<RecordResource> q = this.createQuery().field("_id")
				.equal(recordId);
		recordUpdate.removeAll("annotationIds", Arrays.asList(annotationId));
		this.update(q, recordUpdate);
	}

	public void findUrlsFromRecords(Set<String> urls) {
		log.info("Retrieving urls from the records");
		Iterator<RecordResource> recordIterator = DB.getRecordResourceDAO()
				.createQuery().iterator();
		int i = 1;
		while (recordIterator.hasNext()) {
			log.info("Getting the urls for #" + i++ + " record");
			RecordResource record = recordIterator.next();
			List<HashMap<MediaVersion, EmbeddedMediaObject>> mediaList = record
					.getMedia();
			for (HashMap<MediaVersion, EmbeddedMediaObject> media : mediaList) {
				Collection<EmbeddedMediaObject> mediaObjects = media.values();
				for (EmbeddedMediaObject mediaObject : mediaObjects) {
					urls.add(mediaObject.getUrl());
				}
			}
		}
	}

}
