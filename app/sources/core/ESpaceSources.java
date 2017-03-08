package sources.core;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;
import sources.BritishLibrarySpaceSource;
import sources.DBPediaSpaceSource;
import sources.DDBSpaceSource;
import sources.DPLASpaceSource;
import sources.DigitalNZSpaceSource;
import sources.ElasticSource;
import sources.EuropeanaFashionSpaceSource;
import sources.EuropeanaSpaceSource;
import sources.FlickrSpaceSource;
import sources.HistorypinSpaceSource;
import sources.NLASpaceSource;
import sources.RijksmuseumSpaceSource;
import sources.WithSpaceSource;
import sources.WithinASpaceSource;
import sources.YouTubeSpaceSource;
/*import espace.core.sources.BritishLibrarySpaceSource;
import espace.core.sources.DDBSpaceSource;
import espace.core.sources.DPLASpaceSource;
import espace.core.sources.DigitalNZSpaceSource;
import espace.core.sources.EuropeanaFashionSpaceSource;
import espace.core.sources.NLASpaceSource;
import espace.core.sources.RijksmuseumSpaceSource;
import espace.core.sources.YouTubeSpaceSource;*/


public class ESpaceSources {

	public static List<ISpaceSource> esources;
	public static final ALogger log = Logger.of( ESpaceSources.class );

	static void init() {
		esources = new ArrayList<ISpaceSource>();
		esources.add(new EuropeanaSpaceSource());
		esources.add(new DPLASpaceSource());
		esources.add(new NLASpaceSource());
		esources.add(new DigitalNZSpaceSource());
//		esources.add(new EuropeanaFashionSpaceSource());
		esources.add(new YouTubeSpaceSource());
//		esources.add(new ElasticSource());
		esources.add(new RijksmuseumSpaceSource());
		esources.add(new DDBSpaceSource());
		esources.add(new BritishLibrarySpaceSource());
		esources.add(new FlickrSpaceSource.InternetArchiveSpaceSource());
		esources.add(new WithSpaceSource());
//		esources.add(new DBPediaSpaceSource());
		esources.add(new HistorypinSpaceSource());
		esources.add(new WithinASpaceSource());
		log.info("Initialization of sources list");
	}

	public static List<ISpaceSource> getESources() {
		if (esources == null) {
			init();
		}
		return esources;
	}
}
