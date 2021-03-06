package db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import model.DescriptiveData;
import model.EmbeddedMediaObject;
import model.EmbeddedMediaObject.MediaVersion;
import model.annotations.ContextData;
import model.annotations.ContextData.ContextDataBody;
import model.basicDataTypes.Language;
import model.basicDataTypes.ProvenanceInfo;
import model.basicDataTypes.WithAccess;
import model.basicDataTypes.WithAccess.Access;
import model.basicDataTypes.WithAccess.AccessEntry;
import model.resources.collection.CollectionObject;
import model.resources.WithResource;
import model.usersAndGroups.User;

import org.bson.types.ObjectId;
import org.elasticsearch.common.lang3.ArrayUtils;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.CriteriaContainer;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import controllers.WithController.Action;
import utils.Tuple;

import com.mongodb.BasicDBObject;

import controllers.CollectionObjectController;

/*
 * The class consists of methods that can be both query
 * a CollectionObject or a RecordResource_ (CollectionObject,
 * CulturalObject, WithResource etc).
 *
 * Special methods referring to one of these entities go to the
 * specific DAO class.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class WithResourceDAO<T extends WithResource> extends DAO<T> {

	/*
	 * The value of the entity class is either CollectionObject.class or
	 * RecordResource.class
	 */
	public WithResourceDAO(Class<T> entityClass) {
		super(entityClass);
	}

	public Query<T> createColIdElemMatchQuery(ObjectId colId) {
		Query<T> q = this.createQuery();
		BasicDBObject colIdQuery = new BasicDBObject();
		colIdQuery.put("collectionId", colId);
		BasicDBObject elemMatch1 = new BasicDBObject();
		elemMatch1.put("$elemMatch", colIdQuery);
		q.filter("collectedIn", elemMatch1);
		return q;
	}

	/**
	 * Given a list of ObjectId's (dbId's) return the specified resources
	 *
	 * @param ids
	 * @return
	 */
	public List<T> getByIds(List<ObjectId> ids) {
		Query<T> colQuery = this.createQuery().field("_id").hasAnyOf(ids);
		return find(colQuery).asList();
	}

	/**
	 * List all CollectionObjects with the title provided for the language
	 * specified
	 *
	 * @param title
	 * @return
	 */
	public List<T> getByLabel(String lang, String title) {
		if (lang == null)
			lang = Language.DEFAULT.toString();
		Query<T> q = this.createQuery().disableValidation()
				.field("descriptiveData.label" + lang).contains(title);
		return this.find(q).asList();
	}

	public List<T> getByLabel(Language lang, String title) {
		Query<T> q = this.createQuery().disableValidation()
				.field("descriptiveData.label." + lang.getDefaultCode()).equal(title);// .contains(title);
		return this.find(q).asList();
	}

	/**
	 * Get a user's CollectionObject according to the title given
	 *
	 * @param creatorId
	 * @param title
	 * @return
	 */
	public T getByOwnerAndLabel(ObjectId creatorId, String lang, String title) {
		if (lang == null)
			lang = "default";
		Query<T> q = this.createQuery().field("administrative.withCreator")
				.equal(creatorId).field("descriptiveData.label." + lang)
				.equal(title);
		return this.findOne(q);
	}

	public boolean existsForOwnerAndLabel(ObjectId creatorId, String lang,
			List<String> title) {
		if (lang == null)
			lang = "default";
		Query<T> q = this.createQuery().field("administrative.withCreator")
				.equal(creatorId).field("descriptiveData.label." + lang)
				.equal(title);
		return this.exists(q);
	}

	public boolean existsOtherForOwnerAndLabel(ObjectId creatorId, String lang,
			List<String> title, ObjectId excludedId) {
		if (lang == null)
			lang = "default";
		Query<T> q = this.createQuery().field("administrative.withCreator")
				.equal(creatorId).field("descriptiveData.label." + lang)
				.equal(title).field("_id").notEqual(excludedId);
		return this.exists(q);
	}

	/**
	 * Get all CollectionObject using the creator's/owner's id.
	 *
	 * @param creatorId
	 * @param offset
	 * @param count
	 * @return
	 */
	public List<T> getByCreator(ObjectId creatorId, int offset, int count) {
		Query<T> q = this.createQuery().field("administrative.withCreator")
				.equal(creatorId).offset(offset).limit(count);
		return this.find(q).asList();
	}

	/**
	 * Get the first CollectionObject that a user has created using the
	 * creator's/owner's id. We are using MongoDB's paging.
	 *
	 * @param id
	 * @return
	 */
	public List<T> getFirstResourceByCreator(ObjectId id) {
		return getByCreator(id, 0, 1);
	}

	/**
	 * Retrieve the owner/creator of a Resource using collection's dbId
	 *
	 * @param id
	 * @return
	 */
	public User getOwner(ObjectId id) {
		Query<T> q = this.createQuery().field("_id").equal(id)
				.retrievedFields(true, "administrative.withCreator");
		return ((WithResource) findOne(q)).getWithCreator();
	}

	/**
	 * Retrieve a resource if the provenanceChain contains the providerName
	 *
	 * @param sourceName
	 * @return
	 */
	public List<T> getByProvider(String providerName) {
		Query<T> q = this.createQuery();
		BasicDBObject provQuery = new BasicDBObject();
		provQuery.put("provider", providerName);
		BasicDBObject elemMatch = new BasicDBObject();
		elemMatch.put("$elemMatch", provQuery);
		q.filter("provenance", elemMatch);
		return this.find(q).asList();
	}

	/**
	 * Return the number of resources that belong to a source
	 *
	 * @param sourceId
	 * @return
	 */
	public long countBySource(String sourceName) {
		Query<T> q = this.createQuery();
		BasicDBObject provQuery = new BasicDBObject();
		provQuery.put("provider", sourceName);
		BasicDBObject elemMatch = new BasicDBObject();
		elemMatch.put("$elemMatch", provQuery);
		q.filter("provenance", elemMatch);
		return this.find(q).countAll();
	}

	public void updateProvenance(ObjectId id, Integer index, ProvenanceInfo info) {
		Query<T> q = this.createQuery().field("_id").equal(id);
		UpdateOperations<T> updateOps = this.createUpdateOperations()
				.disableValidation();
		updateOps.set("provenance." + index, info);
		this.updateFirst(q, updateOps);
	}

	public void updateMedia(ObjectId id, int index, MediaVersion version,
			EmbeddedMediaObject media) {
		Query<T> q = this.createQuery().field("_id").equal(id);
		UpdateOperations<T> updateOps = this.createUpdateOperations()
				.disableValidation();
		updateOps.set("media." + index + "." + version, media);
		this.updateFirst(q, updateOps);
	}

	public boolean isPublic(ObjectId id) {
		Query<T> q = this.createQuery().field("_id").equal(id).limit(1);
		q.field("administrative.isPublic").equal(true);
		return (find(q).asList().size() == 0 ? false : true);
	}

	/**
	 * Create a Mongo access query criteria
	 *
	 * @param userAccess
	 * @return
	 */
	public Criteria formAccessLevelQuery(Tuple<ObjectId, Access> userAccess,
			QueryOperator operator) {
		int ordinal = userAccess.y.ordinal();
		BasicDBObject accessQuery = new BasicDBObject();
		accessQuery.put("user", userAccess.x);
		BasicDBObject oper = new BasicDBObject();
		oper.put(operator.toString(), ordinal);
		accessQuery.append("level", oper);
		return this.createQuery().criteria("administrative.access.acl")
				.hasThisElement(accessQuery);
	}

	protected CriteriaContainer loggedInUserWithAtLeastAccessQuery(
			List<ObjectId> loggedInUserEffIds, Access access) {
		List<Criteria> criteria = new ArrayList<Criteria>();// new
															// Criteria[loggedInUserEffIds.size()+1];
		for (int i = 0; i < loggedInUserEffIds.size(); i++) {
			criteria.add(formAccessLevelQuery(
					new Tuple(loggedInUserEffIds.get(i), access),
					QueryOperator.GTE));
		}
		if (access.ordinal() < 2)
			criteria.add(this.createQuery()
					.criteria("administrative.access.isPublic").equal(true));
		return this.createQuery().or(
				criteria.toArray(new Criteria[criteria.size()]));
	}

	protected CriteriaContainer atLeastAccessCriteria(
			List<Tuple<ObjectId, Access>> filterByUserAccess) {
		Criteria[] criteria = new Criteria[0];
		for (Tuple<ObjectId, Access> userAccess : filterByUserAccess) {
			criteria = ArrayUtils.addAll(criteria,
					formAccessLevelQuery(userAccess, QueryOperator.GTE));
		}
		return this.createQuery().or(criteria);
	}

	/**
	 * Create a basic Mongo query with withCreator field matching, offset, limit
	 * and criteria.
	 *
	 * @param criteria
	 * @param creator
	 * @param offset
	 * @param count
	 * @return
	 */
	protected Query<T> formCreatorQuery(CriteriaContainer[] criteria,
			ObjectId creator, int offset, int count) {
		Query<T> q = this.createQuery().offset(offset).limit(count + 1);
		if (creator != null)
			q.field("administrative.withCreator").equal(creator);
		if (criteria.length > 0)
			q.and(criteria);
		return q;
	}

	public boolean hasAccess(List<ObjectId> effectiveIds, Action action,
			ObjectId resourceId) {
		CriteriaContainer criteria = loggedInUserWithAtLeastAccessQuery(
				effectiveIds, actionToAccess(action));
		Query<T> q = this.createQuery();
		q.field("_id").equal(resourceId);
		q.retrievedFields(true, "_id");
		q.limit(1);
		q.or(criteria);
		return (this.findIds(q).size() == 0 ? false : true);
	}

	public Access actionToAccess(Action action) {
		return Access.values()[action.ordinal() + 1];
	}

	public void updateResourceRights(WithAccess access, ObjectId resourceId) {
		Query<T> q = this.createQuery().field("_id").equal(resourceId);
		UpdateOperations<T> updateOps = this.createUpdateOperations()
				.disableValidation();
		updateOps.set("administrative.access", access);
		this.update(q, updateOps);
	}

	public void updateWithURI(ObjectId resourceId, String uri) {
		Query<T> q = this.createQuery().field("_id").equal(resourceId);
		UpdateOperations<T> updateOps = this.createUpdateOperations()
				.disableValidation();
		updateOps.set("administrative.withURI", uri);
		this.update(q, updateOps);
	}

	public void changeAccess(ObjectId resourceId, ObjectId userId,
			Access newAccess) {
		Query<T> q = this.createQuery().field("_id").equal(resourceId);
		ArrayList<String> retrievedFields = new ArrayList<String>();
		retrievedFields.add("administrative.access");
		T resource = this.findOne(q.retrievedFields(true,
				retrievedFields.toArray(new String[retrievedFields.size()])));
		WithAccess access = resource.getAdministrative().getAccess();
		int index = 0;
		UpdateOperations<T> updateOps = this.createUpdateOperations()
				.disableValidation();
		for (AccessEntry entry : access.getAcl()) {
			if (entry.getUser().equals(userId)) {
				// userExists = true;
				break;
			}
			index += 1;
		}
		if (index < access.getAcl().size())
			if (!newAccess.equals(Access.NONE))
				updateOps.set("administrative.access.acl." + index,
						new AccessEntry(userId, newAccess));
			else
				updateOps.removeAll("administrative.access.acl",
						new AccessEntry(userId, null));
		else {
			updateOps.add("administrative.access.acl", new AccessEntry(userId,
					newAccess));
		}
		this.update(this.createQuery().field("_id").equal(resourceId),
				updateOps);
	}

	public WithAccess mergeRights(List<WithAccess> parentColAccess,
			ObjectId recordWithCreator, boolean recordIsPublic) {
		WithAccess newRecordAccess = new WithAccess();
		newRecordAccess.setIsPublic(recordIsPublic);
		// initially fill in rewRecordData with most liberal rights of parent
		// collections
		for (WithAccess colAccess : parentColAccess) {
			if (colAccess.getIsPublic())
				newRecordAccess.setIsPublic(true);
			for (AccessEntry colEntry : colAccess.getAcl()) {
				if (!newRecordAccess.containsUser(colEntry.getUser())
						&& (colEntry.getLevel() != Access.NONE))
					newRecordAccess.addToAcl(colEntry);
				for (AccessEntry recEntry : newRecordAccess.getAcl()) {
					if (recEntry.getUser().equals(colEntry.getUser()))
						if (colEntry.getLevel().ordinal() > recEntry.getLevel()
								.ordinal())
							recEntry.setLevel(colEntry.getLevel());
				}
			}
		}
		// withCreator of record should always maintain OWN rights (?)
		if (recordWithCreator != null)
			newRecordAccess.addToAcl(recordWithCreator, Access.OWN);
		return newRecordAccess;
	}

	public List<ObjectId> getParentCollections(ObjectId resourceId) {
		T record = this.getById(resourceId,
				new ArrayList<String>(Arrays.asList("collectedIn")));
		List<ObjectId> parentCollections = new ArrayList<ObjectId>();
		for (ObjectId ci : (List<ObjectId>) record.getCollectedIn()) {
			parentCollections.add(ci);
		}
		return parentCollections;
	}

	/**
	 * Return the total number of likes for a resource.
	 *
	 * @param id
	 * @return
	 */
	public int getTotalLikes(ObjectId id) {
		Query<T> q = this.createQuery().field("_id").equal(id)
				.retrievedFields(true, "usage.likes");
		return ((WithResource) this.findOne(q)).getUsage().getLikes();
	}

	public List<Integer> getPositionsInCollection(ObjectId recordId,
			ObjectId collectionId) {
		CollectionObject collection = DB.getCollectionObjectDAO().getById(
				collectionId,
				Arrays.asList("collectedResources"));
		List<Integer> positions = new ArrayList<Integer>();
		int i = 0;
		for (ContextData<ContextDataBody> collectedResources : (List<ContextData>) collection
				.getCollectedResources()) {
			if (collectedResources.getTarget().getRecordId().equals(recordId))
				positions.add(i);
			i++;
		}
		return positions;
	}

	/**
	 * @param extId
	 * @return
	 */
	public T getByExternalId(String extId) {
		Query<T> q = this.createQuery().field("administrative.externalId")
				.equal(extId);
		return this.findOne(q);
	}

	/**
	 * This method is to update the 'public' field on all the records of a
	 * collection. By default update method is invoked to all documents of a
	 * collection.
	 *
	 **/
	public void setFieldValueOfCollectedResource(ObjectId colId,
			String fieldName, String value) {
		Query<T> q = createColIdElemMatchQuery(colId);
		UpdateOperations<T> updateOps = this.createUpdateOperations();
		updateOps.set(fieldName, value);
		this.update(q, updateOps);
	}

	public void updateContent(ObjectId recId, String format, String content) {
		Query<T> q = this.createQuery().field("_id").equal(recId);
		UpdateOperations<T> updateOps = this.createUpdateOperations();
		updateOps.set("content." + format, content);
		this.update(q, updateOps);
	}

	public void updateDescriptiveData(ObjectId recId, DescriptiveData data) {
		Query<T> q = this.createQuery().field("_id").equal(recId);
		UpdateOperations<T> updateOps = this.createUpdateOperations();
		updateOps.set("descriptiveData", data);
		this.update(q, updateOps);
	}

	public void updateEmbeddedMedia(ObjectId recId,
			List<HashMap<MediaVersion, EmbeddedMediaObject>> media) {
		Query<T> q = this.createQuery().field("_id").equal(recId);
		UpdateOperations<T> updateOps = this.createUpdateOperations();
		updateOps.set("media", media);
		this.update(q, updateOps);
	}

	public void updateProvenance(ObjectId recId, List<ProvenanceInfo> provenance) {
		Query<T> q = this.createQuery().field("_id").equal(recId);
		UpdateOperations<T> updateOps = this.createUpdateOperations();
		updateOps.set("provenance", provenance);
		this.update(q, updateOps);
	}

	/**
	 * Increment likes for this specific resource
	 *
	 * @param externalId
	 */
	public void incrementLikes(ObjectId dbId) {
		incField("usage.likes", dbId);
	}

	/**
	 * Decrement likes for this specific resource
	 *
	 * @param dbId
	 */
	public void decrementLikes(ObjectId dbId) {
		decField("usage.likes", dbId);
	}

}
