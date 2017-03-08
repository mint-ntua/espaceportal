package sources.core;

public class RecordJSONMetadata {

	public enum Format {
		JSON_WITH,
		JSON_UNKNOWN, JSONLD_UNKNOWN, XML_UNKNOWN, 
		JSON_EDM, JSONLD_EDM, XML_EDM,
		JSONLD_DPLA,
		JSON_NLA, XML_NLA, 
		JSON_DNZ, XML_DNZ,
		JSON_RDF,
		JSON_YOUTUBE, JSON_RIJ, JSON_Historypin
	}

	private String jsonContent;
	private Format format;

	public RecordJSONMetadata(Format format, String jsonContent) {
		this.jsonContent = jsonContent;
		this.format = format;
	}

	public String getJsonContent() {
		return jsonContent;
	}
	
	public void setJsonContent(String jsonContent) {
		this.jsonContent = jsonContent;
	}

	public boolean hasFormat(Format format){
		return this.format.equals(format);
	}

	public String getFormat() {
		return (String.valueOf(format).replaceAll("_", "-"));
	}

}
