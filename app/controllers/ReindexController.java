package controllers;

import java.util.function.Function;

import elastic.Elastic;
import elastic.ElasticReindexer;
import play.Logger;
import play.Logger.ALogger;
import play.libs.F.Promise;
import play.mvc.Result;
import sources.core.ParallelAPICall;

public class ReindexController extends WithController {

	public static final ALogger log = Logger.of(ReindexController.class);

	/*
	 * Api call to reindex all data from one index to another.
	 * Useful when changing mapping.
	 */
	public static Result changeIndice(String newIndice) {

		try {
			Function<String, Boolean> chind = ( (newInd) -> (ElasticReindexer.reindexOnANewIndice(Elastic.old_index, newInd)) );
			ParallelAPICall.createPromise(chind, newIndice);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}

		return ok();
	}

	/*
	 * Api call to index all document that are stored in Mongo
	 * but not indexed in ElasticSearch.
	 * When finished Mongo and Elastic have to be consistent.
	 */
	public static Result makeConsistent() {

		try {
			Promise<Boolean> p = Promise.promise(() -> ElasticReindexer.indexInconsistentDocs());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}

		return ok();
	}


	/*
	 * Api call to read all documents from Mongo and
	 * index them to Elastic.
	 * It's a very slow reindex process. Mongo and Elastic
	 * have to be consistent in the end.
	 */
	public static Result reindexAllResources() {

		try {
			Promise<Boolean> p = Promise.promise(() -> ElasticReindexer.reindexAllDbDocuments());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}

		return ok();
	}

	/*
	 * Api call to reindex all collection resources.
	 * Mongo and Elastic should be consistent talking
	 * about collection terms.
	 */
	public static Result reindexAllCollections() {

		try {
			Promise<Boolean> p = Promise.promise(() -> ElasticReindexer.reindexAllDbCollections());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}

		return ok();
	}


	/*
	 * Api call to reindex all thesaurus resources.
	 * Mongo and Elastic should be consistent talking
	 * about thesaurus terms.
	 */
	public static Result reindexAllThesaurus() {

		try {
			Promise<Boolean> p = Promise.promise(() -> ElasticReindexer.reindexAllDbThesaurus());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}

		return ok();
	}

}
