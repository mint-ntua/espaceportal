package model.basicDataTypes;

import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.utils.IndexType;

import db.converters.ProvenanceInfoConverter;

@Converters(ProvenanceInfoConverter.class)
public class ProvenanceInfo {

	public enum Sources {
		Mint("Mint"), Europeana("Europeana"), UploadedByUser("UploadedByUser"),
		BritishLibrary("BritishLibrary", "The British Library"),
		InternetArchive("InternetArchive","Internet Archive"), DDB("DDB","Deutsche Digitale Bibliothek"),
		DigitalNZ("DigitalNZ"), DPLA("DPLA","Digital Public Library of America"),
		EFashion("EFashion"),
		YouTube("Youtube"),
		NLA("NLA","National Library of Australia"),
		WITHin("WITHin"),
		DBPedia("DBPedia", "DBPedia"),
		Rijksmuseum("Rijksmuseum","Rijksmuseum"), Historypin("Historypin"), WITHinASpace("WITHinASpace");


		private final String text;
		private final String ID;

		private Sources( String id, String text) {
			this.text = text;
			this.ID = id;
		}

		private Sources(String textid) {
			this.text = textid;
			this.ID = textid;
		}

		@Override
		public String toString() {
			return ID;
		}

		public String getText() {
			return text;
		}

		public String getID() {
			return ID;
		}

		public static Sources getSourceByID(String id){
			for (Sources e : Sources.values()) {
				if (e.getID().equals(id)){
					return e;
				}
			}
			return null;
		}


	}

	private String provider;
	private String uri;
	private String resourceId;

	public ProvenanceInfo() {
	}

	public ProvenanceInfo(String provider) {
		this.provider = provider;
	}

	public ProvenanceInfo(String provider, String uri, String recordId) {
		this.provider = provider;
		this.resourceId = recordId;
		this.uri = uri;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String recordId) {
		this.resourceId = recordId;
	}

	// you can have entries for WITH records with provider "WITH" and
	// recordId the ObjectId of the annotated Record
}
