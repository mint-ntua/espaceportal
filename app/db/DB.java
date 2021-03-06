package db;

import java.util.HashMap;
import java.util.Map;

import model.ApiKey;
import model.annotations.Annotation;
import model.resources.RecordResource;
import model.resources.ThesaurusObject;
import model.resources.WithResource;
import model.resources.collection.CollectionObject;
import model.usersAndGroups.User;
import model.usersAndGroups.UserGroup;
import model.usersAndGroups.UserOrGroup;
import notifications.Notification;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import play.Logger;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import db.converters.AccessEnumConverter;
import db.converters.MultiLiteralOrResourceConverter;
import db.converters.MultiLiteralConverter;


// get the DAOs from here
// the EntityManagerFactory is here
public class DB {
	private static Map<String, DAO<?>> daos = new HashMap<String, DAO<?>>();
	private static MediaObjectDAO mediaObjectDAO;
	private static MongoClient mongo;
	private static Datastore ds;
	private static Morphia morphia;
	private static GridFS gridfs;
	private static Config conf;

	static private final Logger.ALogger log = Logger.of(DB.class);

	public static GridFS getGridFs() {
		if (gridfs == null) {
			try {
				String dbname = getConf().getString("mongo.dbname");
				gridfs = new GridFS(getMongo().getDB(dbname));
			} catch (Exception e) {
				log.error("Cannot create GridFS!", e);
			}
		}
		return gridfs;
	}

	public static Config getConf() {
		if (conf == null) {
			conf = ConfigFactory.load();
		}
		return conf;
	}

	public static MongoClient getMongo() {
		if (mongo == null) {
			try {
				String host = getConf().getString("mongo.host");
				int port = getConf().getInt("mongo.port");
				mongo = new MongoClient(host, port);
				if (getConf().hasPath("mongo.erase")
						&& getConf().getBoolean("mongo.erase")) {
					mongo.dropDatabase(getConf().getString("mongo.dbname"));
				}
			} catch (Exception e) {
				log.error("Cannot create Mongo client", e);
			}
		}
		return mongo;
	}

	public static Morphia getMorphia() {
		if (morphia == null) {
			morphia = new Morphia();
			// this method is not working, have to find why!!
			//morphia.mapPackage("model");
			//morphia.mapPackage("model.resources");
			//morphia.mapPackage("model.basicDataTypes");
			//morphia.mapPackage("model.usersAndGroups");
			//morphia.map(User.class);
			morphia.getMapper().getConverters()
				.addConverter(new MultiLiteralOrResourceConverter());
			morphia.getMapper().getConverters()
			.addConverter(new MultiLiteralConverter());
			morphia.getMapper().getConverters()
				.addConverter(new AccessEnumConverter());
			//Mapper mapper = morphia.getMapper();
		    //mapper.getOptions().setObjectFactory(new CustomMorphiaObjectFactory());
		}
		return morphia;
	}

	public static Datastore getDs() {
		if (ds == null) {
			try {
				ds = getMorphia().createDatastore(getMongo(),
						getConf().getString("mongo.dbname"));
				ds.setDefaultWriteConcern(WriteConcern.ACKNOWLEDGED);
				//ds.ensureIndexes();
			} catch (Exception e) {
				log.error("Cannot create Datastore!", e);
				System.exit(-1);
			}

			try {
				ds.ensureIndexes(Notification.class);
				ds.ensureIndexes(CollectionObject.class);
				ds.ensureIndexes(User.class);
				ds.ensureIndexes(UserGroup.class);
				ds.ensureIndexes(WithResource.class);
			} catch(Exception e) {
				log.error("Error initializing Mongo indexes!", e);
			}
		}
		return ds;
	}

	public static String getJson(Object o) {
		return getMorphia().getMapper().toDBObject(o).toString();
	}

	public static UserDAO getUserDAO() {
		return (UserDAO) getDAO(User.class);
	}

	public static ApiKeyDAO getApiKeyDAO() {
		return (ApiKeyDAO) getDAO(ApiKey.class);
	}

	public static UserGroupDAO getUserGroupDAO() {
		return (UserGroupDAO) getDAO(UserGroup.class);
	}

	public static ThesaurusObjectDAO getThesaurusDAO() {
		return (ThesaurusObjectDAO) getDAO(ThesaurusObject.class);
	}

	/*
	 * Implementation of the new model DAO classes
	 */

	public static CollectionObjectDAO getCollectionObjectDAO() {
		return (CollectionObjectDAO) getDAO(CollectionObject.class);
	}

	public static MediaObjectDAO getMediaObjectDAO() {
		if (mediaObjectDAO == null)
			mediaObjectDAO = new MediaObjectDAO();
		return mediaObjectDAO;
	}


	/*
	 * The rest are going to be used in very special cases
	 * in the far future.
	 */

	public static RecordResourceDAO getRecordResourceDAO() {
		return (RecordResourceDAO) getDAO(RecordResource.class);
	}

	public static AnnotationDAO getAnnotationDAO() {
		return (AnnotationDAO) getDAO(Annotation.class);
	}

	public static WithResourceDAO<WithResource> getWithResourceDAO() {
		DAO<?> dao = daos.get("WithResource");
		if (dao == null) {
			try {
				dao = new WithResourceDAO(WithResource.class);
				daos.put("WithResource", dao);
			} catch (Exception e) {
				log.error("Can't instantiate DAO for WithResource", e);
			}
		}
		return (WithResourceDAO<WithResource>) dao;
	}

	public static NotificationDAO getNotificationDAO() {
		return (NotificationDAO) getDAO(Notification.class);
	}

	/*
	public static RecordResourceDAO.AgentObjectDAO getAgentObjectDAO() {
		return (RecordResourceDAO.AgentObjectDAO)
				getDao(AgentObject.class, RecordResource.class);
	}

	public static RecordResourceDAO.CulturalObjectDAO getCulturalObjectDAO() {
		return (RecordResourceDAO.CulturalObjectDAO)
				getDao(CulturalObject.class, RecordResource.class);
	}

	public static RecordResourceDAO.EuscreenObjectDAO getEuscreenObjectDAO() {
		return (RecordResourceDAO.EuscreenObjectDAO)
				getDao(CulturalObject.class, RecordResource.class);
	}

	public static RecordResourceDAO.EventObjectDAO getEventObjectDAO() {
		return (RecordResourceDAO.EventObjectDAO)
				getDao(EventObject.class, RecordResource.class);
	}

	public static RecordResourceDAO.PlaceObjectDAO getPlaceObjectDAO() {
		return (RecordResourceDAO.PlaceObjectDAO)
				getDao(PlaceObject.class, RecordResource.class);
	}

	public static RecordResourceDAO.TimespanObjectDAO getTimespanObjectDAO() {
		return (RecordResourceDAO.TimespanObjectDAO)
				getDao(TimespanObject.class, RecordResource.class);
	}
   */

	/**
	 * Signleton DAO for all the entities
	 * Parametrized for embedded DAO classes.
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	/*
	private static <T> DAO<?> getDao(Class<T> clazz, Class<?> parentClazz) {
		DAO<?> dao = daos.get(clazz.getSimpleName());
		if(dao == null)  {
			try {
				String daoClassName;
				if (parentClazz == null)
					daoClassName = "db." + clazz.getSimpleName() + "DAO";
				else
					daoClassName = "db." + parentClazz.getSimpleName() + "DAO."
									+ clazz.getSimpleName() + "DAO";
				Class<?> daoClass = Class.forName(daoClassName);
				dao = (DAO<?>) daoClass.newInstance();
				daos.put(clazz.getSimpleName(), dao);
			} catch(Exception e) {
				log.error("Can't instantiate DAO for " + clazz.getName(), e);
			}
		}
		return dao;
	}
	*/
	/**
	 * Singleton DAO class for all the models
	 *
	 * @param clazz
	 * @return
	 */

	private static DAO<?> getDAO(Class<?> clazz) {
		DAO<?> dao = daos.get(clazz.getSimpleName());
		if (dao == null) {
			try {
				String daoClassName = "db." + clazz.getSimpleName() + "DAO";
				Class<?> daoClass = Class.forName(daoClassName);
				dao = (DAO<?>) daoClass.newInstance();
				daos.put(clazz.getSimpleName(), dao);
			} catch (Exception e) {
				log.error("Can't instantiate DAO for " + clazz.getName(), e);
			}
		}
		return dao;
	}

}
