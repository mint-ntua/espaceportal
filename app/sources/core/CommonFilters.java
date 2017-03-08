package sources.core;

public enum CommonFilters {
		TYPE("media.type","Media Type"),
		PROVIDER("provider","Provider"),
		CREATOR("dccreator.default","Creator"),
		RIGHTS("media.withRights","Media Rights"),
		COUNTRY("dctermsspatial.default","Spatial"),
		YEAR("dates","Dates"),
		CONTRIBUTOR("dccontributor.default","Contributor"),
		DATA_PROVIDER("dataProvider","Data Provider"),
		MIME_TYPE("MIME_TYPE","Mime Type"),
		IMAGE_SIZE("IMAGE_SIZE","Image Size"),
		IMAGE_COLOUR("IMAGE_COLOUR","Image Color"),
		COLOURPALETE("COLOURPALETE","Color Palete")
		
		;

		private final String text;
		private final String id;

		private CommonFilters(final String id) {
	        this.text = id;
	        this.id = id;
	    }

	    private CommonFilters(final String id, String text) {
	    	this.id = id;
	    	this.text = text;
	    }

		@Override
	    public String toString() {
	        return text;
	    }

		public String getText() {
			return text;
		}

		public String getId() {
			return id;
		}



	/*
	//TODO: remove duplication
	public static final String TYPE_NAME = "Type";
	public static final String PROVIDER_ID = "provider";
	public static final String PROVIDER_NAME = "Provider";
	public static final String CREATOR_ID = "creator";
	public static final String CREATOR_NAME = "Creator";
	public static final String RIGHTS_ID = "rights";

	public static final String RIGHTS_NAME = "Content Usage";
	public static final String COUNTRY_ID = "country";
	public static final String COUNTRY_NAME = "Country";
	public static final String YEAR_NAME = "Year";
	public static final String YEAR_ID = "year";
	public static final String CONTRIBUTOR_ID = "contributor";
	public static final String CONTRIBUTOR_NAME = "Contributor";
	public static final String DATAPROVIDER_ID = "data_provider";
	public static final String DATAPROVIDER_NAME = "Data Provider";

	public static final String COMESFROM_ID = "comesFrom";
	public static final String COMESFROM_NAME = "Comes From";


	public static final String AVAILABILITY_ID = "availability";
	public static final String AVAILABILITY_NAME = "Availability";

	public static final String REUSABILITY_ID = "reusability";
	public static final String REUSABILITY_NAME = "Reusability";*/
}
