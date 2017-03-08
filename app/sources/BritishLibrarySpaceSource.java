package sources;

import model.EmbeddedMediaObject.WithMediaRights;
import model.EmbeddedMediaObject.WithMediaType;
import model.basicDataTypes.ProvenanceInfo.Sources;
import model.resources.WithResource;
import sources.core.CommonFilterLogic;
import sources.core.CommonFilters;
import sources.core.CommonQuery;
import sources.core.SourceResponse;
import sources.core.Utils;
import sources.formatreaders.FlickrRecordFormatter;

public class BritishLibrarySpaceSource extends FlickrSpaceSource {
	public BritishLibrarySpaceSource() {
		super(Sources.BritishLibrary,"12403504@N02");
		formatreader = new FlickrRecordFormatter.BritishLibraryRecordFormatter();
	}
	
	@Override
	public SourceResponse getResults(CommonQuery q) {
		if (Utils.hasInfo(q.searchTerm) && q.searchTerm.equals("*"))
			q.searchTerm = "";
		return super.getResults(q);
	}

}
